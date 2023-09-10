package pisi.unitedmeows.seriex.commands.arguments;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import panda.std.Result;

@ArgumentName("gamemode")
public class GamemodeArgument implements OneArgument<GameMode> {

	private static final Map<String, GameMode> GAME_MODE_ARGUMENTS = new HashMap<>();

	static {
		for (GameMode value : GameMode.values()) {
			GAME_MODE_ARGUMENTS.put(value.name().toLowerCase(), value);
			GAME_MODE_ARGUMENTS.put(String.valueOf(value.getValue()), value);
			if (value != GameMode.SPECTATOR)
				GAME_MODE_ARGUMENTS.put(String.valueOf(value.name().toLowerCase().charAt(0)), value);
			else GAME_MODE_ARGUMENTS.put("sp", value);
		}
	}

	@Override
	public Result<GameMode, ?> parse(LiteInvocation invocation, String argument) {
		var gameMode = GAME_MODE_ARGUMENTS.get(argument.toLowerCase());

		if (gameMode == null)
			return Result.error("Invalid gamemode");

		return Result.ok(gameMode);
	}
}
