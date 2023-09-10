package pisi.unitedmeows.seriex.commands;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.argument.option.Opt;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.A;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.AnimationModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier.MetadataFlag;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier.MetadataType;
import pisi.unitedmeows.seriex.managers.virtualplayers.path.VirtualWaypoint;
import pisi.unitedmeows.seriex.managers.virtualplayers.profile.VirtualProfile;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.math.Rotation;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.List;
import java.util.function.BiConsumer;

@Route(name = "virtualplayer", aliases = { "vp" })
public class VirtualPlayerCommand {
	@Execute(route = "list", max = 0)
	public void list(PlayerW sender) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var srx = Seriex.get();
		srx.msg_no_translation(sender, "Available virtual players: %s",
					Seriex.get().virtualPlayerManager().getVirtualPlayers().keySet().stream()
								.collect(srx.collector()));
	}

	@Execute(route = "create", required = 1)
	public void create(PlayerW sender, @Arg String name) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var srx = Seriex.get();
		VirtualPlayer.create(
					VirtualProfile.create(name),
					sender.hook().getLocation(),
					VirtualWaypoint.create(null),
					new Pair[0],
					new Pair[0]);
		srx.msg_no_translation(sender,
					"Created virtual player %s!",
					name);
	}

	@Execute(route = "remove", required = 1)
	public void remove(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var name = virtualPlayer.virtualProfile().getName();
		virtualPlayer.visibilityModifier().queueRemove().send();
		virtualPlayer.delete();
		Seriex.get().msg_no_translation(sender, "Removed virtual player with the name '%s'", name);
	}

	@Route(name = "tp")
	static class TP {
		@Execute(required = 1)
		public void defaultCMD(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex.get().msg_no_translation(sender, "Available TP operations: here, there, to");
		}

		@Execute(route = "to", required = 6)
		public void to(PlayerW sender, @Arg VirtualPlayer virtualPlayer,
					@Arg Location location, @Arg Rotation rotation) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var teleportTo = rotation.location(location);

			virtualPlayer.updateBaseLocation(teleportTo);
			virtualPlayer.save();
		}

		@Execute(route = "here", required = 1)
		public void here(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			virtualPlayer.updateBaseLocation(sender.hook().getLocation());
			virtualPlayer.save();
		}

		@Execute(route = "there", required = 1)
		public void there(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			sender.teleportToLocation(virtualPlayer.bukkitPlayer().getLocation());
		}
	}

	@Route(name = "set")
	static class SetCMD {
		@Execute(max = 0)
		public void list(PlayerW sender) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex.get().msg_no_translation(sender, "Available operations: %s", "location, profile, waypoint, setting, imitate");
		}

		@Execute(route = "profile", min = 2, max = 3)
		public void profile(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg String displayName, @Opt Option<String> skinNameOpt) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var skinName = skinNameOpt.orElseGet(displayName);
			virtualPlayer.visibilityModifier().queueRemove().send();
			virtualPlayer.delete();

			var newProfile = VirtualProfile.create(displayName, skinName);
			if (newProfile == null) {
				Seriex.get().msg_no_translation(sender, "Could not update virtual profile for '%s'", displayName);
				return;
			}

			var updated = VirtualPlayer.create(
						newProfile,
						virtualPlayer.baseLocation(), virtualPlayer.virtualWaypoint(),
						virtualPlayer.generalSettings().entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toArray(Pair[]::new),
						virtualPlayer.imitateSettings().entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toArray(Pair[]::new));
			updated.save();

			Seriex.get().msg_no_translation(sender, "Updated virtual profile for '%s'", displayName);
		}

		@Execute(route = "imitate", required = 3)
		public void imitate(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg VirtualPlayer.Imitate imitate, @Arg Boolean value) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			virtualPlayer.updated();
			virtualPlayer.imitateSettings().replace(imitate, value);
			Seriex.get().msg_no_translation(sender, "Updated value of imitation '%s' => '%s'", imitate.name(), virtualPlayer.imitateSettings().get(imitate));
			virtualPlayer.save();
		}

		@Execute(route = "location", min = 4, max = 6)
		public void location(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg Location location, @Opt Option<Rotation> rotation) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Location senderLocation = sender.hook().getLocation();
			if (rotation.isPresent()) {
				var rotationValue = rotation.get();
				location.setYaw(rotationValue.yaw());
				location.setPitch(rotationValue.pitch());
			} else {
				location.setYaw(senderLocation.getYaw());
				location.setPitch(senderLocation.getPitch());
			}

			virtualPlayer.updateBaseLocation(location);
			Seriex.get().msg_no_translation(sender, "Updated location of '%s' to [%s, %s, %s, %s, %s]",
						virtualPlayer.virtualProfile().getName(),
						location.getX(), location.getY(), location.getZ(),
						location.getYaw(), location.getPitch());
			virtualPlayer.save();
		}

		@Execute(route = "setting", required = 3)
		public void setting(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg VirtualPlayer.Settings setting, @Arg Boolean value) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			virtualPlayer.generalSettings().replace(setting, value);
			Seriex.get().msg_no_translation(sender,
						"Updated value of setting '%s' => '%s'",
						setting.name(),
						virtualPlayer.generalSettings().get(setting));
			virtualPlayer.save();
		}

		@Route(name = "waypoint")
		static class Waypoint {
			@Execute(max = 0)
			public void list(PlayerW sender) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var s = Seriex.get();
				s.msg_no_translation(sender, "virtualplayer set waypoint name index x y z || to update an index");
				s.msg_no_translation(sender, "virtualplayer set waypoint name index remove || to remove a waypoint");
				s.msg_no_translation(sender, "virtualplayer set waypoint name here || to add a waypoint");
				s.msg_no_translation(sender, "virtualplayer set waypoint name loop || to loop waypoints");
				s.msg_no_translation(sender, "virtualplayer set waypoint name clear || to clear all waypoints");
				s.msg_no_translation(sender, "virtualplayer set waypoint name speed value || to set speed");
				s.msg_no_translation(sender, "See available waypoints with 'virtualplayer get waypoint'");
			}

			private boolean checkIndex(PlayerW sender, VirtualPlayer virtualPlayer, Integer index) {
				var s = Seriex.get();
				if (!virtualPlayer.virtualWaypoint().hasWaypoints()) {
					s.msg_no_translation(sender, "%s doesn't have any waypoints.", virtualPlayer.virtualProfile().getName());
					return true;
				}
				if (index < 0 || index >= virtualPlayer.virtualWaypoint().getWaypoints().size()) {
					s.msg_no_translation(sender, "Index out of bounds [%s,%s]", index, virtualPlayer.virtualWaypoint().getWaypoints().size());
					return true;
				}

				return false;
			}

			@Execute(route = "index", required = 5)
			public void index(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg Integer index, @Arg Location location) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				if (checkIndex(sender, virtualPlayer, index))
					return;

				virtualPlayer.virtualWaypoint().getWaypoints().set(index, location.toVector());
				Seriex.get().msg_no_translation(sender, "Updated waypoint %s for '%s'", index, virtualPlayer.virtualProfile().getName());
				virtualPlayer.save();
			}

			@Execute(route = "index", required = 3)
			public void index(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg Integer index, @Arg String operation) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var s = Seriex.get();
				if (!operation.equalsIgnoreCase("remove")) {
					s.msg_no_translation(sender, "Wrong waypoint operation.");
					return;
				}

				if (checkIndex(sender, virtualPlayer, index))
					return;

				var removedVector = virtualPlayer.virtualWaypoint().getWaypoints().get(index);
				virtualPlayer.virtualWaypoint().getWaypoints().remove(removedVector);
				Seriex.get().msg_no_translation(sender, "Updated waypoint %s for '%s'", index, virtualPlayer.virtualProfile().getName());
				virtualPlayer.save();
			}

			@Execute(route = "here", required = 1)
			public void here(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				Vector vector = sender.hook().getLocation().toVector();
				virtualPlayer.locationModifier().updateF();
				virtualPlayer.virtualWaypoint().add(vector);
				Seriex.get().msg_no_translation(sender, "Added waypoint [%s, %s, %s]", vector.getX(), vector.getY(), vector.getZ());
				virtualPlayer.save();
			}

			@Execute(route = "clear", required = 1)
			public void clear(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var s = Seriex.get();
				if (!virtualPlayer.virtualWaypoint().hasWaypoints()) {
					s.msg_no_translation(sender, "'%s' doesn't have any waypoints.", virtualPlayer.virtualProfile().getName());
					return;
				}
				int size = virtualPlayer.virtualWaypoint().getWaypoints().size();
				virtualPlayer.virtualWaypoint().cleanup();
				s.msg_no_translation(sender, "Cleared %s waypoints", size);
				virtualPlayer.save();
			}

			@Execute(route = "loop", required = 1)
			public void loop(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				virtualPlayer.virtualWaypoint().loop(!virtualPlayer.virtualWaypoint().isLooping());
				Seriex.get().msg_no_translation(sender, "Looping waypoint is now '%s'", virtualPlayer.virtualWaypoint().isLooping());
				virtualPlayer.save();
			}

			@Execute(route = "speed", required = 2)
			public void speed(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg Double speed) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				virtualPlayer.virtualWaypoint().speed(speed);
				Seriex.get().msg_no_translation(sender, "Waypoint speed is now '%s'", virtualPlayer.virtualWaypoint().getSpeed());
				virtualPlayer.save();
			}
		}
	}

	@Route(name = "get")
	static class GetCMD {
		@Execute(max = 0)
		public void list(PlayerW sender) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex.get().msg_no_translation(sender, "Available operations: %s", "location, profile, waypoint, setting, imitate");
		}

		@Execute(route = "location", required = 1)
		public void location(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var s = Seriex.get();
			Location baseLocation = virtualPlayer.baseLocation();
			s.msg_no_translation(sender, "Current base location: [%s, %s, %s, %s, %s]",
						baseLocation.getX(),
						baseLocation.getY(),
						baseLocation.getZ(),
						baseLocation.getYaw(),
						baseLocation.getPitch());
			Location bukkitLocation = virtualPlayer.bukkitPlayer().getLocation();
			s.msg_no_translation(sender, "Current bukkit location: [%s, %s, %s, %s, %s]",
						bukkitLocation.getX(),
						bukkitLocation.getY(),
						bukkitLocation.getZ(),
						bukkitLocation.getYaw(),
						bukkitLocation.getPitch());
		}

		@Execute(route = "profile", required = 1)
		public void profile(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var s = Seriex.get();
			VirtualProfile virtualProfile = virtualPlayer.virtualProfile();
			s.msg_no_translation(sender, "Display name - %s", virtualProfile.getName());
			s.msg_no_translation(sender, "Skin name - %s", virtualProfile.getTextureName());
		}

		@Execute(route = "waypoint", required = 1)
		public void waypoint(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var s = Seriex.get();
			List<Vector> waypoints = virtualPlayer.virtualWaypoint().getWaypoints();
			if (!virtualPlayer.virtualWaypoint().hasWaypoints() || waypoints.isEmpty()) {
				s.msg_no_translation(sender, "No waypoints.");
				return;
			}
			s.msg_no_translation(sender, "Current waypoints:");
			for (int i = 0; i < waypoints.size(); i++) {
				Vector vector = waypoints.get(i);
				s.msg_no_translation(sender, "Waypoint #%s [%s, %s, %s]", i, vector.getX(), vector.getY(), vector.getZ());
			}
		}

		@Execute(route = "setting", required = 2)
		public void setting(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg VirtualPlayer.Settings setting) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex.get().msg_no_translation(sender,
						"Value of setting '%s' => '%s'",
						setting.name(),
						virtualPlayer.generalSettings().get(setting));
		}

		@Execute(route = "imitate", required = 2)
		public void imitate(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg VirtualPlayer.Imitate imitate) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			Seriex.get().msg_no_translation(sender,
						"Value of imitation '%s' => '%s'", imitate.name(),
						virtualPlayer.imitateSettings().get(imitate));
		}
	}

	@Route(name = "queue")
	static class Queue {

		@Execute(max = 0)
		public void list(PlayerW sender) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var srx = Seriex.get();
			srx.msg_no_translation(sender, "Available queue-able operations: animation, equipment, location, metadata, rotation");
		}

		@Execute(route = "animation", required = 2)
		public void animation(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg AnimationModifier.EntityAnimation animation) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			var srx = Seriex.get();
			srx.msg_no_translation(sender, "Played animation '%s'", animation.name());
			virtualPlayer.animationModifier().queue(animation).send();
		}

		@Route(name = "location")
		static class LocationCMD {
			@Execute(route = "movement", min = 4, max = 6)
			public void movement(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg Location location, @Opt Option<Rotation> rotation) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var loc = rotation.isPresent() ? rotation.get().location(location) : location;
				virtualPlayer.locationModifier().queueMovement(loc);
				Seriex.get().msg_no_translation(sender, "Moved virtual player to %s", loc.toString());
			}

			@Execute(route = "teleport", min = 4, max = 6)
			public void teleport(PlayerW sender, @Arg VirtualPlayer virtualPlayer, @Arg Location location, @Opt Option<Rotation> rotation) {
				if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

				var loc = rotation.isPresent() ? rotation.get().location(location) : location;
				virtualPlayer.locationModifier().queueTeleport(loc);
				Seriex.get().msg_no_translation(sender, "Moved virtual player to %s", loc.toString());
			}
		}

		@Execute(route = "equipment", required = 1)
		public void equipment(PlayerW sender, @Arg VirtualPlayer virtualPlayer) {
			if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

			BiConsumer<ItemSlot, ItemStack> consumer = (slot, stack) -> {
				var srx = Seriex.get();
				if (stack != null) {
					srx.msg_no_translation(sender, "Equipped your %s to the virtual player!", slot.name());
				} else {
					srx.msg_no_translation(sender, "Un-equipped %s from the virtual player!", slot.name());
				}
				virtualPlayer.equipmentModifier().equip(slot, stack).send();
				virtualPlayer.equipmentModifier().update().send();
			};

			PlayerInventory inventory = sender.hook().getInventory();
			consumer.accept(ItemSlot.HEAD, inventory.getHelmet());
			consumer.accept(ItemSlot.CHEST, inventory.getChestplate());
			consumer.accept(ItemSlot.LEGS, inventory.getLeggings());
			consumer.accept(ItemSlot.FEET, inventory.getBoots());
			consumer.accept(ItemSlot.MAINHAND, sender.hook().getItemInHand());
		}

		@Execute(route = "metadata", required = 3)
		public void metadata(PlayerW playerW, @Arg VirtualPlayer virtualPlayer, @Arg MetadataFlag flag, @Arg Boolean flagValue) {
			if (playerW.doesntHaveRank(Ranks.MAINTAINER)) return;

			virtualPlayer.metadataModifier().setFlag(MetadataType.PLAYER_FLAGS, flag, flagValue);
			Seriex.get().msg_no_translation(playerW, "Set flag '%s' to '%s'", flag, flagValue);
		}

		@Execute(route = "rotation", required = 3)
		public void rotation(PlayerW playerW, @Arg VirtualPlayer virtualPlayer, @Arg Rotation rotation) {
			if (playerW.doesntHaveRank(Ranks.MAINTAINER)) return;

			virtualPlayer.rotationModifier().queueRotate(rotation.yaw(), rotation.pitch()).send();
			Seriex.get().msg_no_translation(playerW, "Applied rotation '%s, %s' to '%s' for virtual player", rotation.yaw(), rotation.pitch());
		}

	}
}
