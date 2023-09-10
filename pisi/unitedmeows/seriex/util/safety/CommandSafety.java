package pisi.unitedmeows.seriex.util.safety;

import java.util.List;

import pisi.unitedmeows.seriex.Seriex;

public class CommandSafety {
	private static String fixCommand(String command) {
		return command.startsWith("/") ? command.substring(1) : command;
	}

	public static boolean handleBannedCommand(String command0, List<String> blockList) {
		// /groupmanager:manuaddp "lol:qweqwe"
		// will be groupmanager:manuaddp "lol:qweqwe" because we stripped the slash
		String command = fixCommand(command0);
		if (!command.contains(":"))
			return blockList.contains(command);
		String[] split = command.split(":"); // /groupmanager manuaddp "lol qweqwe"
		if (split.length >= 2) {
			String afterColon = split[1]; // should return manuaddp
			return blockList.contains(afterColon); // if we contain this command => blocked
		} else {
			Seriex.get().logger().error("Is this command syntax correct? '{}'", command);
			return true;
		}
	}
}
