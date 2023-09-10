package pisi.unitedmeows.seriex.anticheat;

import static pisi.unitedmeows.seriex.anticheat.AnticheatCache.allPossibleNamesCache;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.api.events.EventFlag;
import pisi.unitedmeows.seriex.api.events.EventFlag.CheckRecord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerSettings;
import pisi.unitedmeows.seriex.util.Permissions;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public enum Anticheat {
	NCP_FC("ncp3137" , "NCP 3.13.7" , "NoCheatPlus" , new AnticheatImplementation() {
		fr.neatmonster.nocheatplus.hooks.NCPHook hook = new fr.neatmonster.nocheatplus.hooks.NCPHook() {

			@Override
			public String getHookName() { return "NCP 3.13.7 Test Hook"; }

			@Override
			public String getHookVersion() { return "1.0"; }

			@Override
			public boolean onCheckFailure(fr.neatmonster.nocheatplus.checks.CheckType type, Player player, fr.neatmonster.nocheatplus.checks.access.IViolationInfo v) {
				StringBuilder allParameters = new StringBuilder();
				for (fr.neatmonster.nocheatplus.actions.ParameterName parameter : fr.neatmonster.nocheatplus.actions.ParameterName.values()) {
					if (parameter == fr.neatmonster.nocheatplus.actions.ParameterName.IP) continue;
					String value = v.getParameter(parameter);
					if (value == null
								|| "<???>".equals(value)
								|| ("[" + parameter.getText() + "]").equals(value)
								|| value.isBlank()
								|| value.isEmpty())
						continue;
					allParameters.append(parameter.name());
					allParameters.append(" => ");
					allParameters.append(value);
					allParameters.append(" ");
				}

				PlayerW user = Seriex.get().dataManager().user(player);
				EventFlag flagEvent = new EventFlag(NCP_FC, user,
							new CheckRecord(type.name(), false, v.hasCancel(), v.getTotalVl() + "/" + v.getAddedVl(),
										getHookName()));
				user.fireEvent(flagEvent);
				// if the event is cancelled, this hook will cancel the violation
				return flagEvent.cancel();
			}
		};

		@Override
		void setup() {
			JavaPlugin seriexPlugin = Seriex.get().plugin();
			seriexPlugin.getServer().getPluginManager().registerEvents(this, seriexPlugin);
			fr.neatmonster.nocheatplus.hooks.NCPHookManager.addHook(fr.neatmonster.nocheatplus.checks.CheckType.ALL, hook);
		}

		@Override
		void enable() {

		}

		@Override
		void disable() {

		}
	}),
	NCP_MINEZ("ncp3111" , "NCP 3.11.1" , "NoCheatPlusMinez" , new AnticheatImplementation() {
		minez.neatmonster.nocheatplus.hooks.NCPHook hook = new minez.neatmonster.nocheatplus.hooks.NCPHook() {

			@Override
			public String getHookName() { return "NCP 3.11.1 Test Hook"; }

			@Override
			public String getHookVersion() { return "1.0"; }

			@Override
			public boolean onCheckFailure(minez.neatmonster.nocheatplus.checks.CheckType type, Player player, minez.neatmonster.nocheatplus.checks.access.IViolationInfo v) {
				StringBuilder allParameters = new StringBuilder();
				for (minez.neatmonster.nocheatplus.actions.ParameterName parameter : minez.neatmonster.nocheatplus.actions.ParameterName.values()) {
					if (parameter == minez.neatmonster.nocheatplus.actions.ParameterName.IP) continue;
					String value = v.getParameter(parameter);
					if (value == null
								|| "<???>".equals(value)
								|| value.isBlank()
								|| value.isEmpty()
								|| value.charAt(0) == '[')
						continue;
					allParameters.append(parameter.name());
					allParameters.append(" => ");
					allParameters.append(value);
					allParameters.append(" ");
				}

				PlayerW user = Seriex.get().dataManager().user(player);
				EventFlag flagEvent = new EventFlag(NCP_MINEZ, user,
							new CheckRecord(type.name(), false, v.hasCancel(), v.getTotalVl() + "/" + v.getAddedVl(),
										getHookName()));
				user.fireEvent(flagEvent);
				// if the event is cancelled, this hook will cancel the violation
				return flagEvent.cancel();
			}
		};

		@Override
		void setup() {
			JavaPlugin seriexPlugin = Seriex.get().plugin();
			seriexPlugin.getServer().getPluginManager().registerEvents(this, seriexPlugin);
			minez.neatmonster.nocheatplus.hooks.NCPHookManager.addHook(minez.neatmonster.nocheatplus.checks.CheckType.ALL, hook);
		}

		@Override
		void enable() {

		}

		@Override
		void disable() {

		}
	}),
	NCP_LATEST("ncp3161" , "NCP 3.16.1" , "NoCheatPlusLatest" , new AnticheatImplementation() {
		latest.neatmonster.nocheatplus.hooks.NCPHook hook = new latest.neatmonster.nocheatplus.hooks.NCPHook() {

			@Override
			public String getHookName() { return "NCP 3.16.1 Test Hook"; }

			@Override
			public String getHookVersion() { return "1.0"; }

			@Override
			public boolean onCheckFailure(latest.neatmonster.nocheatplus.checks.CheckType type, Player player, latest.neatmonster.nocheatplus.checks.access.IViolationInfo v) {
				StringBuilder allParameters = new StringBuilder();
				for (latest.neatmonster.nocheatplus.actions.ParameterName parameter : latest.neatmonster.nocheatplus.actions.ParameterName.values()) {
					if (parameter == latest.neatmonster.nocheatplus.actions.ParameterName.IP) continue;
					String value = v.getParameter(parameter);
					if (value == null
								|| "<???>".equals(value)
								|| ("[" + parameter.getText() + "]").equals(value)
								|| value.isBlank()
								|| value.isEmpty())
						continue;
					allParameters.append(parameter.name());
					allParameters.append(" => ");
					allParameters.append(value);
					allParameters.append(" ");
				}
				PlayerW user = Seriex.get().dataManager().user(player);
				EventFlag flagEvent = new EventFlag(NCP_LATEST, user,
							new CheckRecord(type.name(), false, v.willCancel(), v.getTotalVl() + "/" + v.getAddedVl(),
										getHookName()));
				user.fireEvent(flagEvent);
				// if the event is cancelled, this hook will cancel the violation
				return flagEvent.cancel();
			}
		};

		@Override
		void setup() {
			JavaPlugin seriexPlugin = Seriex.get().plugin();
			seriexPlugin.getServer().getPluginManager().registerEvents(this, seriexPlugin);
			latest.neatmonster.nocheatplus.hooks.NCPHookManager.addHook(latest.neatmonster.nocheatplus.checks.CheckType.ALL, hook);
		}

		@Override
		void enable() {

		}

		@Override
		void disable() {

		}
	}),
	VANILLA("vanilla" , "Vanilla" , null , new AnticheatImplementation() {
		@Override
		void setup() {
			// no setup
		}

		@Override
		void enable() {
			// no enable, we will only disable other anticheats
		}

		@Override
		void disable() {
			// no disable, we will already disable using Anticheat#switchTo(PlayerW, Anticheat)
		}
	});

	// pluginName is only null for Vanilla
	public final String databaseName, displayName, pluginName;
	public final List<String> dependantAnticheats;
	public final AnticheatImplementation implementation;

	Anticheat(
				String databaseName,
				String displayName,
				String pluginName,
				AnticheatImplementation implementation,
				String... depends) {
		this.databaseName = databaseName;
		this.displayName = displayName;
		this.pluginName = pluginName;
		this.implementation = implementation;
		this.dependantAnticheats = Collections.unmodifiableList(Arrays.asList(depends));
		if (pluginName != null && Seriex.get().plugin().getServer().getPluginManager().getPlugin(pluginName) == null) {
			Seriex.get().logger().error("Couldnt register anti-cheat: {}, no plugin found with the name {}.", displayName, pluginName);
			return;
		}
		Seriex.get().logger().info("Registered Anticheat {}", displayName);
		Seriex.get().antiCheats().add(this);

		this.implementation.setup();

		allPossibleNamesCache.put(databaseName, this);
		allPossibleNamesCache.put(displayName, this);
		allPossibleNamesCache.put(pluginName, this);

	}

	public static Anticheat tryToGetFromName(String nameToSearch) {
		Anticheat anticheat = allPossibleNamesCache.get(nameToSearch);
		if (anticheat == null) {
			Seriex.get().logger().error("No anticheat with the name '{}'", nameToSearch);
			anticheat = NCP_MINEZ;
		}
		return anticheat;
	}

	public void convert(final PlayerW player) {
		boolean noRestrictions = player.hasPermission(Permissions.NO_ANTICHEAT_RESTRICTIONS);
		if(this == VANILLA && !noRestrictions) {
			Seriex.get().msg(player.hook(), Messages.COMMAND_ANTICHEAT_NOT_ALLOWED, this.name());
			return;
		}

		if (player.playerState() != PlayerState.SPAWN) {
			Seriex.get().msg(player.hook(), Messages.ANTICHEAT_CANT_SWITCH);
			return;
		}
		if (player.anticheat() != null) // first time login
			player.anticheat().implementation.disable();
		player.unsafe_anticheat(this);
		player.anticheat().implementation.enable();
		StructPlayerSettings playerSettings = player.playerSettings();
		playerSettings.anticheat = databaseName;
		playerSettings.update();
	}

	private abstract static class AnticheatImplementation implements Listener {
		abstract void setup();

		abstract void enable();

		abstract void disable();
	}

	public static void initializeClass() {
		// ehu bi eni dow lee
	}
}
