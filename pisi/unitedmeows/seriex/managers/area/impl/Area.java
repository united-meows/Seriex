package pisi.unitedmeows.seriex.managers.area.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.electronwill.nightconfig.core.Config;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.pointer.BasePointer;
import pisi.unitedmeows.seriex.managers.area.pointer.normal.impl.BooleanPointer;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.ItemPointer;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.LocationPointer;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.multi.impl.AreaConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.unsafe.UnsafeIO;

public abstract class Area {
	public Map<String, BasePointer<?, ?, ?>> specialPointers;
	public AreaCategory category;
	public Location warpLocation;
	public Set<UUID> players;
	public boolean autoJoin;
	public AxisBB limits;
	public String name;

	private final List<PointerChangeListener> ptr_listeners;

	public static Area createArea(String cfgName, AreaBase base) {
		try {
			FileManager fileManager = Seriex.get().fileManager();
			AreaConfig areaConfig = fileManager.config(AreaConfig.class);
			Class<? extends Area> klass = Seriex.get().areaManager().baseMap.get(base);
			File configFile = new File(areaConfig.configDirectory(), cfgName + fileManager.EXTENSION);
			boolean created = UnsafeIO.createNewFile(configFile);
			if (created) {
				areaConfig.initializeSingleCfg(areaConfig.configDirectory(), cfgName, areaConfig);
				String world_name = (String) areaConfig.get(cfgName, areaConfig.world_name);
				if (world_name.isEmpty()) { // should be empty, if not create config lol
					ServerConfig serverConfig = fileManager.config(ServerConfig.class);
					areaConfig.set(cfgName, areaConfig.world_name, serverConfig.WORLD_NAME.value());
				}
				areaConfig.set(cfgName, areaConfig.area_base, base.name());
				return klass.getConstructor(String.class).newInstance(cfgName);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Seriex.get().logger().error("Couldnt create new Area '{}'", cfgName);
		return null;
	}

	protected Area(String cfgName) {
		AreaConfig areaConfig = Seriex.get().fileManager().config(AreaConfig.class);
		this.specialPointers = new HashMap<>();
		this.ptr_listeners = new ArrayList<>();
		this.players = new HashSet<>();
		this.name = cfgName;
		this.warpLocation = new Location(
					Bukkit.getWorld((String) areaConfig.get(cfgName, areaConfig.world_name)),
					((Integer) areaConfig.get(cfgName, areaConfig.warpX)).doubleValue(),
					((Integer) areaConfig.get(cfgName, areaConfig.warpY)).doubleValue(),
					((Integer) areaConfig.get(cfgName, areaConfig.warpZ)).doubleValue());
		this.limits = new AxisBB(
					areaConfig.get(cfgName, areaConfig.world_name),
					areaConfig.get(cfgName, areaConfig.minX),
					areaConfig.get(cfgName, areaConfig.minY),
					areaConfig.get(cfgName, areaConfig.minZ),
					areaConfig.get(cfgName, areaConfig.maxX),
					areaConfig.get(cfgName, areaConfig.maxY),
					areaConfig.get(cfgName, areaConfig.maxZ));
	}

	public void start() {}

	public void stop() {}

	public void enter(Player player) {}

	public void leave(Player player) {}

	public boolean move(Player player) {
		return false;
	}

	public void tick() {}

	public boolean attack(Player damager, LivingEntity damaged, EntityDamageByEntityEvent rawEvent) {
		return false;
	}

	public boolean block_place(Player placedBy, Block placed) {
		return false;
	}

	public void join(Player player) {}

	public void disconnect(Player player) {}

	public boolean sneak(Player player, boolean sneaking) {
		return false;
	}

	protected abstract boolean isConfigured();

	public void handleEnter(Player player) {
		boolean added = players.add(player.getUniqueId());
		if (added)
			enter(player);
	}

	public void handleLeave(Player player) {
		boolean removed = players.remove(player.getUniqueId());
		if (removed)
			leave(player);
	}

	public boolean isInside(Player player) {
		return players.contains(player.getUniqueId());
	}

	public boolean isReallyConfigured() {
		return this.limits != null
					&& limits.minX != 0 && limits.minY != 0 && limits.minZ != 0
					&& limits.maxX != 0 && limits.maxY != 0 && limits.maxZ != 0
					&& warpLocation != null
					&& warpLocation.getBlockX() != 0 && warpLocation.getBlockY() != 0 && warpLocation.getBlockZ() != 0 && isConfigured();
	}

	public void saveConfig() {
		savePointers();
		FileManager fileManager = Seriex.get().fileManager();
		AreaConfig areaConfig = fileManager.config(AreaConfig.class);
		ServerConfig serverConfig = fileManager.config(ServerConfig.class);
		areaConfig.set(this.name, areaConfig.world_name, serverConfig.WORLD_NAME.value());

		areaConfig.set(this.name, areaConfig.warpX, warpLocation.getBlockX());
		areaConfig.set(this.name, areaConfig.warpY, warpLocation.getBlockY());
		areaConfig.set(this.name, areaConfig.warpZ, warpLocation.getBlockZ());

		Location minCoords = limits.getMinCoords();
		areaConfig.set(this.name, areaConfig.minX, minCoords.getBlockX());
		areaConfig.set(this.name, areaConfig.minY, minCoords.getBlockY());
		areaConfig.set(this.name, areaConfig.minZ, minCoords.getBlockZ());

		Location maxCoords = limits.getMaxCoords();
		areaConfig.set(this.name, areaConfig.maxX, maxCoords.getBlockX());
		areaConfig.set(this.name, areaConfig.maxY, maxCoords.getBlockY());
		areaConfig.set(this.name, areaConfig.maxZ, maxCoords.getBlockZ());
	}

	public void savePointers() {
		AreaConfig areaConfig = Seriex.get().fileManager().config(AreaConfig.class);

		for (var entry : specialPointers.entrySet()) {
			String ptr_name = entry.getKey();
			BasePointer<?, ?, ?> ptr_data = entry.getValue();
			areaConfig.set(name, "pointer." + ptr_name, ptr_data.ptr2cfg());
			areaConfig.set(name, "pointer_type." + ptr_name, ptr_data.type().name());
		}
	}

	public void loadPointers() {
		AreaConfig areaConfig = Seriex.get().fileManager().config(AreaConfig.class);
		Config pointerDataCfg = areaConfig.get(name, "pointer");
		if (pointerDataCfg == null) {
			Seriex.get().logger().info("There are no pointers to load for area '{}'.", name);
			return;
		}
		Config pointerTypeCfg = areaConfig.get(name, "pointer_type");
		pointerDataCfg.valueMap().forEach((String ptrName, Object serializedCfgObject) -> {
			String type = pointerTypeCfg.get(ptrName);

			Supplier<Map<String, Object>> serialized_handler = () -> {
				Config serializedData = (Config) serializedCfgObject;
				return serializedData.valueMap();
			};

			if (type == null) {
				Seriex.get().logger().error("Type is null for ptr* {}", ptrName);
				return;
			}

			switch (type) {
				case "ITEM" -> specialPointers.put(ptrName, new ItemPointer(serialized_handler.get()));
				case "LOCATION" -> specialPointers.put(ptrName, new LocationPointer(serialized_handler.get()));
				case "BOOLEAN" -> specialPointers.put(ptrName, new BooleanPointer((Boolean) serializedCfgObject));
				default -> Seriex.get().logger().error("Unsupported ptr* type: {}", type);
			}
		});
	}

	protected void add_ptr(String ptrName, BasePointer<?, ?, ?> ptrData) {
		specialPointers.putIfAbsent(ptrName, ptrData);
	}

	protected <X> X get_ptr(String ptrName) {
		return (X) specialPointers.get(ptrName);
	}

	protected void check_ptr_update(PointerChangeListener listener) {
		this.ptr_listeners.add(listener);
	}

	public void update_ptr(BasePointer<?, ?, ?> newPtr, String updated_name) {
		ptr_listeners.forEach(listener -> listener.update(newPtr, updated_name));
	}

	public enum AreaCategory {
		COMBAT,
		MOVEMENT,
		PLAYER,
		MISC,
	}

	public interface PointerChangeListener {
		void update(BasePointer<?, ?, ?> new_ptr, String updated_ptr_name);
	}

}
