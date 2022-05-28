package pisi.unitedmeows.seriex.managers.area;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.reflections.Reflections;

import com.electronwill.nightconfig.core.CommentedConfig;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.areas.Area;
import pisi.unitedmeows.seriex.managers.area.areas.util.ImplementArea;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.AreaConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.utils.Pair;

public class AreaManager extends Manager implements Listener {
	public List<Area> areaList = new GlueList<>();
	public Map<String, Pair<ImplementArea, Class<? extends Area>>> classMap = new HashMap<>();

	@Override
	public void start(Seriex seriex) {
		Reflections sorry = new Reflections("pisi.unitedmeows.seriex.managers.area.areas.impl");
		Set<Class<? extends Area>> areaClasses = sorry.getSubTypesOf(Area.class);
		areaClasses.stream().forEach(areaClass -> {
			try {
				boolean annotationPresent = areaClass.isAnnotationPresent(ImplementArea.class);
				if (!annotationPresent) {
					seriex.logger().fatal("Skipping class " + areaClass.getName() + " no annotation found.");
				}
				ImplementArea annotation = areaClass.getAnnotation(ImplementArea.class);
				classMap.put(annotation.name(), new Pair<>(annotation, areaClass));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		FileManager fileManager = seriex.get().fileManager();
		AreaConfig areaConfig = (AreaConfig) fileManager.getConfig(fileManager.AREAS);
		areaConfig.getConfigs().forEach((String configName, Pair<File, CommentedConfig> pair) -> {
			try {
				CommentedConfig config = pair.item2();
				String baseClassName = config.get("base");
				if ("".equals(baseClassName)) return;
				Pair<ImplementArea, Class<? extends Area>> pair2 = classMap.get(baseClassName);
				Class<? extends Area> baseClass = pair2.item2();
				ImplementArea annotation = pair2.item1();
				Area clazz = baseClass.getConstructor(areaConfig.getClass(), config.getClass(), annotation.category().getClass()).newInstance(areaConfig, config, annotation.category());
				areaList.add(clazz);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		areaList.forEach(Area::enable);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		for (int i = 0; i < areaList.size(); i++) {
			Area area = areaList.get(i);
			boolean prevInside = area.limits().isLocInside(event.getFrom());
			boolean nowInside = area.limits().isLocInside(event.getTo());
			Player player = event.getPlayer();
			if (!prevInside && nowInside) {
				area.enter(player);
				area.playersInArea().add(player);
			} else if (prevInside && !nowInside) {
				area.leave(player);
				area.playersInArea().remove(player);
			}
		}
	}

	@Override
	public void cleanup() throws SeriexException {
		areaList.forEach(Area::disable);
	}
}
