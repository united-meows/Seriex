package pisi.unitedmeows.seriex.util.config.single.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pisi.unitedmeows.seriex.util.Create;
import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.safety.CommandSafety;

@Cfg(name = "BannedCommands")
public class BannedCommandsConfig extends SingleConfig {
	@ConfigField public ConfigValue<List<String>> MAINTAINER_ONLY_COMMANDS = new ConfigValue<>("maintainer_only",
				Create.create(new ArrayList<String>(), arraylist -> {
					// We disable group manager commands by default
					// because the plugin has its own permission manager already
					Set<String> groupManagerCommands = Set.of("mancheckw", "manclear", "mandemote", "mangadd", "mangaddi", "mangaddp", "mangaddv", "mangcheckp", "mangcheckv", "mangclearp", "mangdel", "mangdeli", "mangdelp", "mangdelv", "manglist",
								"manglistp", "manglistv", "manload", "manpromote", "mansave", "manselect", "mantogglesave", "mantogglevalidate", "manuadd", "manuaddp", "manuaddsub", "manuaddv", "manucheckp", "manucheckv", "manuclearp", "manudel",
								"manudelsub", "manudelp", "manudelv", "manulistp", "manulistv", "manwhois", "manworld", "manuaddtemp", "manudeltemp", "manudelalltemp", "manulisttemp");
					arraylist.addAll(groupManagerCommands);
				}));
	@ConfigField public ConfigValue<List<String>> CONSOLE_ONLY_COMMANDS = new ConfigValue<>("console_only",
				Create.create(new ArrayList<String>(), arraylist -> {
					arraylist.add("stop");
					arraylist.add("restart");
				}));

	public boolean isConsoleOnly(String command) {
		return CommandSafety.handleBannedCommand(command, CONSOLE_ONLY_COMMANDS.value());
	}

	public boolean isMaintainerOnly(String command) {
		return CommandSafety.handleBannedCommand(command, MAINTAINER_ONLY_COMMANDS.value());
	}
}
