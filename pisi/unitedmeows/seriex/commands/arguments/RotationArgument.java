package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.MultilevelArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import org.bukkit.Location;
import panda.std.Result;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;
import pisi.unitedmeows.seriex.util.math.Rotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ArgumentName("yaw pitch")
public class RotationArgument implements MultilevelArgument<Rotation> {

	@Override
	public Result<Rotation, Object> parseMultilevel(LiteInvocation invocation, String... arguments) {
		return Result.supplyThrowing(NumberFormatException.class, () -> {

			var yaw = Float.parseFloat(arguments[0]);
			var pitch = Float.parseFloat(arguments[1]);

			return Rotation.create(yaw, pitch);
		}).mapErr(ex -> "Invalid rotation!");
	}
 
	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		var player = CommandUtilities.from_invocation(invocation);
		if (player == null)
			return Collections.emptyList();

		var playerLocation = player.hook().getLocation();
		return Arrays.asList(Suggestion.multilevel(
					String.valueOf(playerLocation.getYaw()),
					String.valueOf(playerLocation.getPitch())
		));
	}

	@Override
	public boolean validate(LiteInvocation invocation, Suggestion suggestion) {
		for (String suggest : suggestion.multilevelList()) {
			if (!suggest.matches("-?[\\d.]+")) { // -? - optional negative, \\d - digit, . - dot
				return false;
			}
		}

		return true;
	}

	@Override
	public int countMultilevel() {
		return 2;
	}
}
