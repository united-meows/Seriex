package pisi.unitedmeows.seriex.managers.area;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.impl.Area;
import pisi.unitedmeows.seriex.managers.area.impl.Area.AreaCategory;
import pisi.unitedmeows.seriex.managers.area.impl.AreaBase;
import pisi.unitedmeows.seriex.managers.area.impl.AreaData;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.multi.impl.AreaConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class AreaManager extends Manager implements Listener {
	public Map<AreaBase, Class<? extends Area>> baseMap;
	public Map<Class<? extends Area>, AreaData> areaDataMap;

	public List<Area> areaList;

	@Override
	public void start(Seriex seriex) {
		this.baseMap = new HashMap<>();
		this.areaDataMap = new HashMap<>();
		this.areaList = new ArrayList<>();
		try (ScanResult result = new ClassGraph().enableAllInfo().acceptPackages("pisi.unitedmeows.seriex.managers.area.impl").scan(4)) {
			result.getAllClasses().filter(f -> f.extendsSuperclass(Area.class)).forEach(info -> {
				Class<? extends Area> areaClass = (Class<? extends Area>) info.loadClass();
				boolean annotationPresent = areaClass.isAnnotationPresent(AreaData.class);
				if (!annotationPresent)
					seriex.logger().error("Skipping class {} no annotation found.", areaClass.getName());
				AreaData annotation = areaClass.getAnnotation(AreaData.class);
				this.baseMap.put(annotation.base(), areaClass);
				this.areaDataMap.put(areaClass, annotation);
			});
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Area area : areaList) {
					area.tick();
				}
			}
		}.runTaskTimer(seriex.plugin(), 0, 1L);
	}

	public void addNewArea(Area area, AreaBase base) {
		Class<? extends Area> klass = baseMap.get(base);
		AreaData areaData = areaDataMap.get(klass);
		area.category = AreaCategory.valueOf(getPackageOfClass(klass).split("impl.")[1].toUpperCase(Locale.ENGLISH));
		area.autoJoin = areaData.autoJoin();
		areaList.add(area);
		area.start();
	}

	@Override
	public void post(Seriex seriex) {
		try {
			AreaConfig areaConfig = seriex.fileManager().config(AreaConfig.class);
			File parentDirectory = areaConfig.configDirectory();
			File[] files = parentDirectory.listFiles();
			for (File cfgFile : files) {
				String fileName = cfgFile.getName();
				if (fileName.endsWith(FileManager.EXTENSION)) {
					String cfgName = fileName.replace(FileManager.EXTENSION, "");
					areaConfig.initializeSingleCfg(areaConfig.configDirectory(), cfgName, areaConfig);
					String areaBase = areaConfig.get(cfgName, areaConfig.area_base);
					Class<? extends Area> klass = baseMap.get(AreaBase.valueOf(areaBase));
					Area area = klass.getConstructor(String.class).newInstance(cfgName);
					AreaData areaData = areaDataMap.get(klass);
					area.category = AreaCategory.valueOf(getPackageOfClass(klass).split("impl.")[1].toUpperCase(Locale.ENGLISH));
					area.autoJoin = areaData.autoJoin();
					areaList.add(area);
				}
			}
			areaList.forEach(Area::loadPointers);
			areaList.forEach(Area::start);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		areaList.forEach(area -> {
			boolean inside = area.limits.intersectsWith(event.getTo());
			Player player = event.getPlayer();

			if (area.autoJoin) {
				boolean contains = area.isInside(player);
				if (inside && !contains) {
					area.handleEnter(player);
				} else if (!inside && contains) {
					area.handleLeave(player);
				}
			}

			if (area.isInside(player)) {
				boolean cancel = area.move(player);
				if (cancel) event.setCancelled(true);
			}
		});
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		areaList.forEach(area -> {
			if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity damaged) {
				boolean cancel = area.attack(player, damaged, event);
				if (cancel) event.setCancelled(true);
			}
		});
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		areaList.forEach(area -> {
			Player player = event.getPlayer();
			boolean cancel = area.block_place(player, event.getBlock());
			if (cancel) event.setCancelled(true);
		});
	}

	@EventHandler
	public void onPlayerSneakToggleEvent(PlayerToggleSneakEvent event) {
		areaList.forEach(area -> {
			Player player = event.getPlayer();
			boolean cancel = area.sneak(player, event.isSneaking());
			if (cancel) event.setCancelled(true);
		});
	}

	@Override
	public void cleanup() throws SeriexException {
		if (areaList == null)
			return;

		areaList.forEach(Area::saveConfig);
		areaList.forEach(Area::stop);
	}

	private String getPackageOfClass(Class<?> klass) {
		String fullName = klass.getName();
		String simpleName = klass.getSimpleName();
		return fullName.replace("." + simpleName, "");
	}
}
