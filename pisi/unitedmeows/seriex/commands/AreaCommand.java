package pisi.unitedmeows.seriex.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.async.Async;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.commands.arguments.enums.AreaPropertiesArgument;
import pisi.unitedmeows.seriex.managers.area.impl.Area;
import pisi.unitedmeows.seriex.managers.area.impl.AreaBase;
import pisi.unitedmeows.seriex.managers.area.pointer.BasePointer;
import pisi.unitedmeows.seriex.managers.area.pointer.PointerType;
import pisi.unitedmeows.seriex.managers.area.pointer.normal.impl.BooleanPointer;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.ItemPointer;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.LocationPointer;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.config.multi.impl.AreaConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.unsafe.UnsafeIO;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.Locale;
import java.util.Optional;

@Route(name = "area")
public class AreaCommand {

	@Execute(max = 0)
	public void listCommands(PlayerW sender) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		Seriex.get().msg_no_translation(sender, "Available operations: %s", "create, remove, ptr, set, get");
	}

	@Execute(route = "list", max = 0)
	@Async
	public void list(PlayerW sender) {
		Seriex seriex = Seriex.get();
		seriex.msg_no_translation(sender, "Available areas: %s",
					seriex.areaManager()
								.areaList
								.stream()
								.map(area -> area.name + "#" + area.category.name())
								.collect(seriex.collector())
		);
	}

	@Execute(route = "create", required = 2)
	public void create(PlayerW sender, @Arg String name, @Arg AreaBase areaBase) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;
		var s = Seriex.get();

		var area = Area.createArea(name, areaBase);
		if (area == null) {
			s.msg_no_translation(sender, "Could not create area...");
			return;
		}

		s.areaManager().addNewArea(area, areaBase);
		s.msg_no_translation(sender, "Created area '%s' with the base '%s'", area.name, areaBase.name());
	}

	@Execute(route = "remove", required = 1)
	public void remove(PlayerW sender, @Arg Area area) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var s = Seriex.get();

		AreaConfig areaConfig = s.fileManager().config(AreaConfig.class);
		var configFile = areaConfig.configFile(area.name);
		if (!configFile.exists()) {
			s.msg_no_translation(sender, "Area file for '%s' not found. (maybe deleted already?)");
			return;
		}

		boolean deleted = UnsafeIO.forceDelete(configFile);
		if (!deleted) {
			s.msg_no_translation(sender, "Area file for '%s' couldnt be deleted.", area.name);
		} else s.msg_no_translation(sender, "Area file for '%s' has been deleted!", area.name);
		s.msg_no_translation(sender, "Deleted area '%s'.", area.name);
		Optional<Area> areaOpt = s.areaManager().areaList.stream().filter(a -> a.name.equals(area.name)).findFirst();
		if (areaOpt.isEmpty()) {
			s.msg_no_translation(sender, "Area object for '%s' not found. (should not happen!)");
			return;
		}
		s.areaManager().areaList.remove(areaOpt.get());
	}

	@Execute(route = "get", required = 2)
	public void get(PlayerW sender, @Arg Area area, @Arg AreaPropertiesArgument property) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var s = Seriex.get();

		switch (property) {
			case BOUNDING_BOX -> s.msg_no_translation(sender, "BB of area '%s' => %s", area.name, area.limits.toString());
			case WARP_LOCATION -> s.msg_no_translation(sender, "Warp location of area '%s' => %s", area.name, area.warpLocation.toString());
		}
	}

	@Route(name = "set")
	static class SetCmd {

		// area set target1 x1 y1 z1 x2 y2 z2

		@Execute(route = "bb", aliases = { "limit", "limits", "boundingbox" }, required = 7)
		public void bb(PlayerW sender, @Arg Area area, @Arg AxisBB axisBB) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			area.limits = axisBB;
			area.saveConfig();
			Seriex.get().msg_no_translation(sender, "Bounding box of area '%s' is now '%s'", area.name, area.limits.toString());
		}

		@Execute(route = "warp", aliases = { "warploc", "tploc", "tp" }, required = 4)
		public void warp(PlayerW sender, @Arg Area area, @Arg Location location) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Location senderLocation = sender.hook().getLocation();
			location.setYaw(senderLocation.getYaw());
			location.setPitch(senderLocation.getPitch());

			area.warpLocation = location;
			area.saveConfig();
			Seriex.get().msg_no_translation(sender, "Warp location of area '%s' is now '%s'", area.name, area.warpLocation.toString());
		}
	}

	@Route(name = "pointer", aliases = { "*", "ptr" })
	static class PointerCMD {
		@Execute(max = 0)
		public void defaultCMD(PlayerW sender) {
			Seriex.get().msg_no_translation(sender, "Available ptr commands: list, set, get");
		}

		@Execute(route = "list", required = 1)
		public void list(PlayerW sender, @Arg Area area) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex s = Seriex.get();
			s.msg_no_translation(sender,
						"Available pointers for '%s' => %s",
						area.name,
						area.specialPointers.entrySet()
									.stream()
									.map(e -> e.getValue().type().name() + "@" + e.getKey())
									.collect(s.collector()));
		}

		@Execute(route = "get", required = 2)
		public void get(PlayerW sender, @Arg Area area, @Arg String ptrName) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex s = Seriex.get();
			BasePointer<?, ?, ?> pointerData = area.specialPointers.get(ptrName);
			String ptrDataString = pointerData == null
						? null
						: pointerData.type().name() + "@" + pointerData.toString();
			s.msg_no_translation(sender, "ptr* value of '%s' of area '%s' is %s",
						ptrName, area.name, ptrDataString);
		}


		// area pointer set
		// area pointer set name ITEM ptr_name
		// area pointer set name LOCATION ptr_name x y z
		// area pointer set name BOOLEAN ptr_name t/f

		@Route(name = "set")
		static class PointerCMDSet {
			@Execute
			public void listOp(PlayerW sender) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				Seriex.get().msg_no_translation(sender, "Available set operations: item, loc, bool");
			}

			private Optional<BasePointer<?, ?, ?>> check_ptr_availablity(PlayerW sender, Area area, String ptr_name) {

				BasePointer<?, ?, ?> pointerData = area.specialPointers.get(ptr_name);
				if (pointerData == null) {
					Seriex.get().msg_no_translation(sender, "No ptr* with the name '%s'.", ptr_name);
					return Optional.empty();
				}

				return Optional.of(pointerData);
			}

			@Execute(route = "item", required = 2)
			public void item(PlayerW sender, @Arg Area area, @Arg String ptrName) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var s = Seriex.get();

				var available_ptr = check_ptr_availablity(sender, area, ptrName);
				if (available_ptr.isEmpty()) {
					return;
				}

				var ptr = available_ptr.get();

				ItemStack itemInHand = sender.hook().getItemInHand();
				if (itemInHand == null) {
					s.msg_no_translation(sender, "To set an item pointer you must hold an item!");
					return;
				}
				ItemPointer newPointer = new ItemPointer(itemInHand);
				area.specialPointers.replace(ptrName, newPointer);
				area.update_ptr(newPointer, ptrName);
				s.msg_no_translation(sender, "Set ptr* '%s' to the item in your hand!", ptr.type().name() + "@" + ptrName);
				area.saveConfig();
			}

			@Execute(route = "loc", aliases = { "location" }, required = 5)
			public void location(PlayerW sender, @Arg Area area, @Arg String ptrName, @Arg Location location) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var s = Seriex.get();

				var available_ptr = check_ptr_availablity(sender, area, ptrName);
				if (available_ptr.isEmpty()) {
					return;
				}

				var ptr = available_ptr.get();

				LocationPointer newPointer = new LocationPointer(location);
				area.specialPointers.replace(ptrName, newPointer);
				area.update_ptr(newPointer, ptrName);
				s.msg_no_translation(sender, "Set ptr* '%s' to the location %s", ptr.type().name() + "@" + ptrName, location);
				area.saveConfig();
			}

			@Execute(route = "boolean", aliases = { "bool" }, required = 3)
			public void bool(PlayerW sender, @Arg Area area, @Arg String ptrName, @Arg Boolean value) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var s = Seriex.get();

				var available_ptr = check_ptr_availablity(sender, area, ptrName);
				if (available_ptr.isEmpty()) {
					return;
				}

				var ptr = available_ptr.get();

				BooleanPointer newPointer = new BooleanPointer(value);
				area.specialPointers.replace(ptrName, newPointer);
				area.update_ptr(newPointer, ptrName);
				s.msg_no_translation(sender, "Set ptr* '%s' to the boolean %s", ptr.type().name() + "@" + ptrName, value);
				area.saveConfig();
			}
		}
	}

}
