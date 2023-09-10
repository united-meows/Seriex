package pisi.unitedmeows.seriex.managers.virtualplayers;

import static com.comphenix.protocol.PacketType.Play.Client.USE_ENTITY;
import static com.comphenix.protocol.events.ListenerPriority.HIGHEST;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.util.concurrent.AtomicDouble;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.ItemPointer;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer.Imitate;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer.Settings;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.AnimationModifier.EntityAnimation;
import pisi.unitedmeows.seriex.managers.virtualplayers.path.VirtualWaypoint;
import pisi.unitedmeows.seriex.managers.virtualplayers.profile.VirtualProfile;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class VirtualPlayerManager extends Manager {
	private Map<String, VirtualPlayer> virtualPlayers;
	private double viewDistance;

	@Override
	public void start(Seriex seriex) {
		this.virtualPlayers = new HashMap<>();
		this.loadFromFiles();
		new BukkitRunnable() {
			@Override
			public void run() {
				for (VirtualPlayer virtualPlayer : virtualPlayers.values()) {
					virtualPlayer.tick();
				}
				VirtualPlayerManager.this.handleVisibility();
			}
		}.runTaskTimer(seriex.plugin(), 0, 1L);
		this.viewDistance = Math.min(50.0 * 50.0, Math.pow(Bukkit.getViewDistance() << 4, 2));
	}

	public void handleVisibility() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			virtualPlayers.forEach((string, virtual) -> {
				Location pLocation = player.getLocation();
				Location vLocation = virtual.bukkitPlayer().getLocation();
				boolean notInSameWorld = !vLocation.getWorld().equals(pLocation.getWorld());
				if (notInSameWorld && virtual.canSee(player)) {
					virtual.hide(player);
					return;
				}
				boolean chunkNotLoaded = !vLocation.getWorld().isChunkInUse(vLocation.getBlockX() >> 4, vLocation.getBlockZ() >> 4);
				double distance = notInSameWorld ? 100000 : vLocation.distanceSquared(pLocation);
				boolean inRange = distance <= this.viewDistance;
				if ((chunkNotLoaded || !inRange) && virtual.canSee(player)) {
					virtual.hide(player);
					return;
				}
				if (inRange && !virtual.canSee(player)) {
					virtual.show(player);
				}
			});
		});
	}

	public void loadFromFiles() {
		FileManager fileManager = Seriex.get().fileManager();
		File parentFile = new File(fileManager.pluginDirectory(), "virtualplayers");
		if (!parentFile.exists()) {
			parentFile.mkdir();
			return;
		}
		for (File file : parentFile.listFiles()) {
			try (CommentedFileConfig cfg = CommentedFileConfig.of(file)) {
				cfg.load();

				String name = cfg.get("virtualprofile.name");
				String skin = cfg.get("virtualprofile.texture");

				String locWorld = cfg.get("location.world");
				double locX = cfg.get("location.x");
				double locY = cfg.get("location.y");
				double locZ = cfg.get("location.z");

				Config cfg_s = cfg.get("setting");
				Map<Settings, Boolean> settings = new EnumMap<>(Settings.class);
				cfg_s.valueMap().forEach((path, value) -> settings.put(Settings.valueOf(path), (Boolean) value));

				Map<Imitate, Boolean> imitate = new EnumMap<>(Imitate.class);
				Config cfg_i = cfg.get("imitate");
				cfg_i.valueMap().forEach((path, value) -> imitate.put(Imitate.valueOf(path), (Boolean) value));

				List<Vector> waypointsList = new ArrayList<>();
				Config waypoints = cfg.get("virtualwaypoint");
				AtomicDouble speed = new AtomicDouble(0.2);
				AtomicBoolean looping = new AtomicBoolean(false);
				if (waypoints != null) {
					looping.set(cfg.get("virtualwaypoint.loop"));
					speed.set(cfg.get("virtualwaypoint.speed"));
					waypoints.valueMap().forEach((index, $) -> {
						if (index.contains("loop") || index.contains("speed"))
							return;
						String base = "virtualwaypoint." + index + ".";
						double x = cfg.get(base + "x");
						double y = cfg.get(base + "y");
						double z = cfg.get(base + "z");
						waypointsList.add(new Vector(x, y, z));
					});
				}

				Function<String, ItemPointer> cfg2ptr = cfg_name -> {
					Config config = cfg.get(cfg_name);
					return config == null ? null : new ItemPointer(config.valueMap());
				};

				ItemPointer heldItem = cfg2ptr.apply("held_item");
				ItemPointer helmet = cfg2ptr.apply("helmet");
				ItemPointer chestplate = cfg2ptr.apply("chestplate");
				ItemPointer leggings = cfg2ptr.apply("leggings");
				ItemPointer boots = cfg2ptr.apply("boots");

				VirtualProfile virtualProfile = VirtualProfile.create(name, skin);
				VirtualWaypoint virtualWaypoint = VirtualWaypoint.create(waypointsList)
							.loop(looping.get())
							.speed(speed.get());
				World bukkitWorld = Bukkit.getWorld(locWorld);
				if (bukkitWorld == null) {
					bukkitWorld = new WorldCreator(locWorld)
								.environment(World.Environment.NORMAL)
								.generateStructures(false)
								.createWorld();
					if (bukkitWorld == null) {
						Seriex.get().logger().error("Couldnt load world (VirtualPlayer) '{}'.", locWorld);
					}
				}
				Location baseLocation = new Location(bukkitWorld, locX, locY, locZ);
				add(VirtualPlayer.create(
							virtualProfile,
							baseLocation,
							virtualWaypoint,
							settings.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toArray(Pair[]::new),
							imitate.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toArray(Pair[]::new))
							.loadEquipment(heldItem, helmet, chestplate, leggings, boots));
			}
			catch (Exception e) {
				e.printStackTrace();
				Seriex.get().logger().error("Couldnt load VirtualPlayer {}", file.getName());
			}
		}
	}

	public void add(VirtualPlayer virtualPlayer) {
		virtualPlayers.put(virtualPlayer.virtualProfile().getName(), virtualPlayer);
	}

	public VirtualPlayer get(String name) {
		return virtualPlayers.get(name);
	}

	public VirtualPlayer remove(VirtualPlayer virtualPlayer) {
		return virtualPlayers.remove(virtualPlayer.virtualProfile().getName());
	}

	public VirtualPlayer fromEntityID(int searchedID) {
		return virtualPlayers
					.values()
					.stream()
					.filter(player -> player.entityID() == searchedID)
					.findFirst()
					.orElseGet(() -> null);
	}

	public Map<String, VirtualPlayer> getVirtualPlayers() { return virtualPlayers; }

	@Override
	public void cleanup() throws SeriexException {
		new ArrayList<>(virtualPlayers.values()).forEach(vPlayer -> vPlayer.visibilityModifier().queueRemove().send());
	}

	public static PacketAdapter createAdapter() {
		return new PacketAdapter(Seriex.get().plugin(), HIGHEST, USE_ENTITY) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				Integer entityID = packet.getIntegers().read(0);
				EntityUseAction action = packet.getEntityUseActions().read(0);
				if (action == EntityUseAction.ATTACK) {
					VirtualPlayer hitVirtualPlayer = Seriex.get().virtualPlayerManager().fromEntityID(entityID);
					if (hitVirtualPlayer != null && hitVirtualPlayer.imitateSettings().get(Imitate.SWING)) {
						hitVirtualPlayer.animationModifier().queue(EntityAnimation.SWING).send(event.getPlayer());
					}
				}
			}
		};
	}
}
