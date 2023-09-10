package pisi.unitedmeows.seriex.managers.virtualplayers;

import static net.minecraft.server.v1_8_R3.EnumProtocolDirection.CLIENTBOUND;
import static org.bukkit.Material.AIR;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.ItemPointer;
import pisi.unitedmeows.seriex.managers.virtualplayers.misc.VirtualNetwork;
import pisi.unitedmeows.seriex.managers.virtualplayers.misc.VirtualPlayerEntity;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.AnimationModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.AnimationModifier.EntityAnimation;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.EquipmentModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.LocationModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier.MetadataFlag;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier.MetadataType;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.RotationModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.VisibilityModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.path.VirtualWaypoint;
import pisi.unitedmeows.seriex.managers.virtualplayers.profile.VirtualProfile;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.unsafe.UnsafeIO;

@SuppressWarnings("unused")
public class VirtualPlayer {

	private VirtualPlayerEntity player;
	private MinecraftServer nmsServer;
	private WorldServer nmsWorld;
	private org.bukkit.World bukkitWorld;

	private VirtualWaypoint waypoint;
	private VirtualProfile profile;

	private LocationModifier locationModifier;
	private RotationModifier rotationModifier;
	private EquipmentModifier equipmentModifier;
	private AnimationModifier animationModifier;
	private MetadataModifier metadataModifier;
	private VisibilityModifier visibilityModifier;

	private Map<Settings, Boolean> generalSettings;
	private Map<Imitate, Boolean> imitateSettings;

	private ItemStack[] armor;
	private ItemStack heldItem;

	private List<UUID> viewers;

	private Location baseLocation;
	private Boolean shouldImitate;
	private boolean updated;
	private int ticks;

	private boolean real;

	public static VirtualPlayer create(VirtualProfile virtualProfile, Location location, VirtualWaypoint waypoint, Pair<Settings, Boolean>[] settings, Pair<Imitate, Boolean>[] imitateSettings) {
		VirtualPlayer virtualPlayer = Seriex.get().virtualPlayerManager().get(virtualProfile.getName());
		if (virtualPlayer != null) {
			Seriex.get().logger().info("VirtualPlayer '{}' already exists!", virtualProfile.getName());
			return virtualPlayer;
		}
		return new VirtualPlayer(virtualProfile, location, waypoint, settings, imitateSettings);
	}

	private VirtualPlayer(VirtualProfile profile, Location location, VirtualWaypoint waypoint, Pair<Settings, Boolean>[] settings, Pair<Imitate, Boolean>... imitateSettings) {
		CraftServer craftServer = (CraftServer) Bukkit.getServer();
		this.nmsServer = craftServer.getServer();
		this.nmsWorld = ((CraftWorld) (this.bukkitWorld = (this.baseLocation = location).getWorld())).getHandle();
		PlayerInteractManager interactManager = new PlayerInteractManager(this.nmsWorld);
		this.profile = profile;

		this.player = new VirtualPlayerEntity(this.nmsServer, this.nmsWorld, profile.toGameProfile(), interactManager);
		this.player.playerConnection = new PlayerConnection(this.nmsServer, new VirtualNetwork(CLIENTBOUND), this.player);
		this.player.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

		this.waypoint = waypoint;
		this.viewers = new ArrayList<>();

		this.imitateSettings = new EnumMap<>(Imitate.class);
		this.generalSettings = new EnumMap<>(Settings.class);

		Arrays.stream(Imitate.values()).forEach(imitate -> this.imitateSettings.put(imitate, Boolean.FALSE));
		Arrays.stream(Settings.values()).forEach(setting -> this.generalSettings.put(setting, Boolean.FALSE));

		Arrays.stream(imitateSettings).forEach(pair -> this.imitateSettings.replace(pair.key(), pair.value()));
		Arrays.stream(settings).forEach(pair -> this.generalSettings.replace(pair.key(), pair.value()));

		this.locationModifier = new LocationModifier(this);
		this.rotationModifier = new RotationModifier(this);
		this.equipmentModifier = new EquipmentModifier(this);
		this.animationModifier = new AnimationModifier(this);
		this.metadataModifier = new MetadataModifier(this);
		this.visibilityModifier = new VisibilityModifier(this);

		this.visibilityModifier.queueSpawn().send();

		Seriex.get().runLater(visibilityModifier.queuePlayerListChange(EnumPlayerInfoAction.REMOVE_PLAYER)::send, 50);

		this.metadataModifier.setFlag(MetadataType.SKIN_FLAGS, MetadataFlag.ALL_SKIN_FLAGS, true);

		this.locationModifier.queueTeleport(location).send();

		Seriex.get().virtualPlayerManager().add(this);
	}

	public void delete() {
		FileManager fileManager = Seriex.get().fileManager();
		File parentFile = new File(fileManager.pluginDirectory(), "virtualplayers");
		if (!parentFile.exists())
			parentFile.mkdir();
		File playerFile = new File(parentFile, this.profile.getName() + ".seriex");
		boolean deleted = UnsafeIO.forceDelete(playerFile);
		if (!deleted) {
			Seriex.get().logger().error("Couldnt delete file for virtual player {}", this.profile.getName());
		} else {
			Seriex.get().logger().info("Deleted virtual player {}", this.profile.getName());
		}
	}

	public void save() {
		FileManager fileManager = Seriex.get().fileManager();
		File parentFile = new File(fileManager.pluginDirectory(), "virtualplayers");
		if (!parentFile.exists())
			parentFile.mkdir();
		File playerFile = new File(parentFile, this.profile.getName() + ".seriex");
		boolean created = UnsafeIO.createNewFile(playerFile);
		if (!playerFile.exists() && !created) {
			Seriex.get().logger().error("Couldnt create file for virtual player {}", this.profile.getName());
			return;
		}

		try (CommentedFileConfig cfg = CommentedFileConfig.builder(playerFile).sync().autoreload().autosave().build()) {
			VirtualProfile virtualProfile = virtualProfile();
			cfg.set("virtualprofile.name", virtualProfile.getName());
			cfg.set("virtualprofile.texture", virtualProfile.getTextureName());

			cfg.set("location.world", this.baseLocation.getWorld().getName());
			cfg.set("location.x", Double.valueOf(this.baseLocation.getX()));
			cfg.set("location.y", Double.valueOf(this.baseLocation.getY()));
			cfg.set("location.z", Double.valueOf(this.baseLocation.getZ()));

			this.generalSettings.forEach((setting, value) -> cfg.set("setting." + setting.name(), value));
			this.imitateSettings.forEach((imitate, value) -> cfg.set("imitate." + imitate.name(), value));

			cfg.set("virtualwaypoint.loop", virtualWaypoint().isLooping());
			cfg.set("virtualwaypoint.speed", virtualWaypoint().getSpeed());

			if (virtualWaypoint().hasWaypoints()) {
				List<Vector> waypoints = virtualWaypoint().getWaypoints();
				for (int i = 0; i < waypoints.size(); i++) {
					Vector vector = waypoints.get(i);
					String base = "virtualwaypoint." + i + ".";
					cfg.set(base + "x", Double.valueOf(vector.getX()));
					cfg.set(base + "y", Double.valueOf(vector.getY()));
					cfg.set(base + "z", Double.valueOf(vector.getZ()));
				}
			}

			if (heldItem != null && heldItem.getType() != AIR) cfg.set("held_item", new ItemPointer(heldItem).ptr2cfg());

			if (armor != null) {
				if (armor[0] != null && armor[0].getType() != AIR) cfg.set("helmet", new ItemPointer(armor[0]).ptr2cfg());
				if (armor[1] != null && armor[1].getType() != AIR) cfg.set("chestplate", new ItemPointer(armor[1]).ptr2cfg());
				if (armor[2] != null && armor[2].getType() != AIR) cfg.set("leggings", new ItemPointer(armor[2]).ptr2cfg());
				if (armor[3] != null && armor[3].getType() != AIR) cfg.set("boots", new ItemPointer(armor[3]).ptr2cfg());
			}
		}
	}

	public void tick() {
		ticks++;
		boolean traveling = waypoint.handleMovement(this);

		if (updated) {
			shouldImitate = null;
			updated = false;
		}

		if (shouldImitate == null)
			shouldImitate = imitateSettings.values().stream().anyMatch(b -> b);

		if (shouldImitate) {
			int radius = 8;
			Collection<Entity> nearbyEntities = bukkitWorld.getNearbyEntities(location(), radius, radius, radius);
			for (Entity entity : nearbyEntities) {
				if (entity instanceof Player bukkitPlayer) {
					if (bukkitPlayer == bukkitPlayer())
						continue;

					if (imitateSettings.get(Imitate.LOOK) && !traveling)
						rotationModifier.queueLookAt(bukkitPlayer.getLocation()).send(bukkitPlayer);
					if (imitateSettings.get(Imitate.ON_FIRE))
						metadataModifier.setFlag(MetadataType.PLAYER_FLAGS, MetadataFlag.ON_FIRE, bukkitPlayer.getFireTicks() > 0);
					if (imitateSettings.get(Imitate.SNEAKING))
						metadataModifier.setFlag(MetadataType.PLAYER_FLAGS, MetadataFlag.SNEAKING, bukkitPlayer.isSneaking());
					if (imitateSettings.get(Imitate.USING_ITEM)) {
						DataWatcher dataWatcher = ((CraftPlayer) bukkitPlayer).getHandle().getDataWatcher();
						int fixedPlayerFlags = dataWatcher.getByte(MetadataType.PLAYER_FLAGS.index) & 0xFF;
						byte isPlayerUsingItem = (byte) (fixedPlayerFlags & 1 << MetadataFlag.USING_ITEM.offset);
						metadataModifier.setFlag(MetadataType.PLAYER_FLAGS, MetadataFlag.USING_ITEM, isPlayerUsingItem != 0 || bukkitPlayer.isBlocking());
					}
				}
			}
		}
	}

	public boolean fakeDamage(boolean sound) {
		if (!setting(Settings.DAMAGE) || bukkitPlayer().getNoDamageTicks() > bukkitPlayer().getMaximumNoDamageTicks() / 2)
			return false;

		animationModifier().queue(EntityAnimation.TAKE_DAMAGE).send();
		float pitch = (float) ((Math.random() - Math.random()) * 0.2 + 1.0);
		Location currentLocation = bukkitPlayer().getLocation();
		if (sound)
			currentLocation.getWorld().playSound(currentLocation, Sound.HURT_FLESH, 1F, pitch);
		bukkitPlayer().setNoDamageTicks(bukkitPlayer().getMaximumNoDamageTicks());
		return true;
	}

	public VirtualPlayer loadEquipment(ItemPointer heldItem, ItemPointer helmet, ItemPointer chestplate, ItemPointer leggings, ItemPointer boots) {
		armor(helmet == null ? null : helmet.data(), ItemSlot.HEAD);
		armor(chestplate == null ? null : chestplate.data(), ItemSlot.CHEST);
		armor(leggings == null ? null : leggings.data(), ItemSlot.LEGS);
		armor(boots == null ? null : boots.data(), ItemSlot.FEET);
		heldItem(heldItem == null ? null : heldItem.data());

		if (helmet != null) equipmentModifier().equip(ItemSlot.HEAD, helmet.data());
		if (chestplate != null) equipmentModifier().equip(ItemSlot.CHEST, chestplate.data());
		if (leggings != null) equipmentModifier().equip(ItemSlot.LEGS, leggings.data());
		if (boots != null) equipmentModifier().equip(ItemSlot.FEET, boots.data());
		if (heldItem != null) equipmentModifier().equip(ItemSlot.MAINHAND, heldItem.data());

		equipmentModifier.update().send();
		return this;
	}

	protected void show(Player player) {
		this.viewers.add(player.getUniqueId());
		visibilityModifier.queuePlayerListChange(EnumPlayerInfoAction.ADD_PLAYER).send(player);
		Seriex.get().runLater(() -> {
			equipmentModifier.update().send(player);
			rotationModifier.queueRotate(baseLocation.getYaw(), baseLocation.getPitch()).send(player);
		}, 30);
	}

	protected void hide(Player player) {
		this.viewers.remove(player.getUniqueId());
	}

	public Vector pos() {
		return new Vector(this.player.locX, this.player.locY, this.player.locZ);
	}

	public VirtualWaypoint virtualWaypoint() {
		return this.waypoint;
	}

	public void virtualWaypoint(VirtualWaypoint new_) {
		this.waypoint = new_;
	}

	public boolean setting(Settings setting) {
		return this.generalSettings.get(setting);
	}

	public Player bukkitPlayer() {
		return entityPlayer().getBukkitEntity();
	}

	public Location location() {
		return new Location(this.bukkitWorld, this.player.locX, this.player.locY, this.player.locZ, this.player.yaw, this.player.pitch);
	}

	public boolean canSee(Player player) {
		return viewers.contains(player.getUniqueId());
	}

	public void updateBaseLocation(Location location) {
		this.baseLocation = location;
		this.player.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		this.locationModifier.queueTeleport(location).send();
	}

	public Location baseLocation() {
		return this.baseLocation;
	}

	public EntityPlayer entityPlayer() {
		return this.player;
	}

	public org.bukkit.World bukkitWorld() {
		return this.bukkitWorld;
	}

	public int entityID() {
		return entityPlayer().getBukkitEntity().getEntityId();
	}

	public VirtualProfile virtualProfile() {
		return this.profile;
	}

	public LocationModifier locationModifier() {
		return this.locationModifier;
	}

	public RotationModifier rotationModifier() {
		return this.rotationModifier;
	}

	public EquipmentModifier equipmentModifier() {
		return this.equipmentModifier;
	}

	public AnimationModifier animationModifier() {
		return this.animationModifier;
	}

	public MetadataModifier metadataModifier() {
		return this.metadataModifier;
	}

	public VisibilityModifier visibilityModifier() {
		return this.visibilityModifier;
	}

	public List<EntityPlayer> packetReceivers() {
		return this.nmsServer.getPlayerList().players;
	}

	public Map<Settings, Boolean> generalSettings() {
		return this.generalSettings;
	}

	public Map<Imitate, Boolean> imitateSettings() {
		return this.imitateSettings;
	}

	public ItemStack heldItem() {
		return heldItem;
	}

	public ItemStack heldItem(ItemStack heldItem) {
		this.heldItem = heldItem;

		return heldItem;
	}

	public ItemStack[] armor() {
		return armor;
	}

	public ItemStack armor(ItemStack armor, ItemSlot slot) {
		if (armor == null)
			return null;

		if (this.armor == null)
			this.armor = new ItemStack[4];

		if (slot == ItemSlot.HEAD) this.armor[0] = armor;
		else if (slot == ItemSlot.CHEST) this.armor[1] = armor;
		else if (slot == ItemSlot.LEGS) this.armor[2] = armor;
		else if (slot == ItemSlot.FEET) this.armor[3] = armor;
		else Seriex.get().logger().error("Tried to equip an item to the {} slot", slot.name());

		return armor;
	}

	public List<UUID> viewers() {
		return viewers;
	}

	public void sendPacket(EntityPlayer onlinePlayer, Packet<?> packet) {
		onlinePlayer.playerConnection.sendPacket(packet);
	}

	public void updated() {
		this.updated = true;
	}

	/*
		@DISABLE_FORMATTING

		private Map<String, EntityPlayer> playersByName() {
			Field field = UnsafeReflect.getField(PlayerList.class, "playersByName");
			UnsafeReflect.setAccessible(field);
			return (Map<String, EntityPlayer>) UnsafeReflect.getField(field, this.nmsServer.getPlayerList());
		}

		private Map<UUID, EntityPlayer> uuid2player() {
			Field field = UnsafeReflect.getField(PlayerList.class, "j");
			UnsafeReflect.setAccessible(field);
			return (Map<UUID, EntityPlayer>) UnsafeReflect.getField(field, this.nmsServer.getPlayerList());
		}
  
   	@ENABLE_FORMATTING
  */

	public enum Settings {
		AREA,
		DAMAGE;

		public static Settings fromString(String name) {
			for (Settings enumValue : Settings.values()) {
				if (enumValue.name().equalsIgnoreCase(name)) {return enumValue;}
			}
			return null;
		}
	}

	public enum Imitate {
		SWING,
		LOOK,
		ON_FIRE,
		SNEAKING,
		USING_ITEM;

		public static Imitate fromString(String name) {
			for (Imitate enumValue : Imitate.values()) {
				if (enumValue.name().equalsIgnoreCase(name)) {return enumValue;}
			}
			return null;
		}
	}
}
