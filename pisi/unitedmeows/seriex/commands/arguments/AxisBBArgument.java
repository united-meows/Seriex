package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.MultilevelArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import panda.std.Result;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;
import pisi.unitedmeows.seriex.util.Parser;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.WORLD;

@ArgumentName("minX minY minZ maxX maxY maxZ")
public class AxisBBArgument implements MultilevelArgument<AxisBB> {

	@Override
	public Result<AxisBB, Object> parseMultilevel(LiteInvocation invocation, String... arguments) {
		return Result.supplyThrowing(NumberFormatException.class, () -> {
			var player = CommandUtilities.from_invocation(invocation);

			var minX = Integer.parseInt(arguments[0]);
			var minY = Integer.parseInt(arguments[1]);
			var minZ = Integer.parseInt(arguments[2]);

			var maxX = Integer.parseInt(arguments[3]);
			var maxY = Integer.parseInt(arguments[4]);
			var maxZ = Integer.parseInt(arguments[5]);

			var world = player == null
						? ((ServerConfig) Seriex.get().fileManager().config(ServerConfig.class)).WORLD_NAME.value()
						: player.attribute(WORLD);

			return new AxisBB(world, minX, minY, minZ, maxX, maxY, maxZ);
		}).mapErr(ex -> "Invalid coordinates!");
	}
	@Override
	public boolean validate(LiteInvocation invocation, Suggestion suggestion) {
		for (String suggest : suggestion.multilevelList()) {
			if (Parser.parseInt(suggest, Integer.MIN_VALUE) == Integer.MIN_VALUE) { // -? - optional negative, \\d - digit, . - dot
				return false;
			}
		}

		return true;
	}

	@Override
	public int countMultilevel() {
		return 6;
	}
}
