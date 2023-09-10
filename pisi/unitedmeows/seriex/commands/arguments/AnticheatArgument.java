package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import org.bukkit.GameMode;
import panda.std.Result;
import pisi.unitedmeows.seriex.anticheat.Anticheat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ArgumentName("anticheat")
public class AnticheatArgument implements OneArgument<Anticheat> {

	@Override
	public Result<Anticheat, ?> parse(LiteInvocation invocation, String argument) {
		var anticheat = Anticheat.tryToGetFromName(argument);

		if (anticheat == null)
			return Result.error("Invalid anticheat");

		return Result.ok(anticheat);
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(
				Arrays.stream(Anticheat.values())
							.map(anticheat -> anticheat.databaseName)
							.toList()
		);
	}
}
