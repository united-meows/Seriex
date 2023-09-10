package pisi.unitedmeows.seriex.commands.arguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Doubles;
import org.bukkit.Location;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.MultilevelArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import panda.std.Result;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

@ArgumentName("x y z")
public class LocationArgument implements MultilevelArgument<Location> {

	private boolean isSpecialArgument(String arg) {
		return arg.length() == 1 && arg.charAt(0) == '~';
	}

	@Override
	public Result<Location, Object> parseMultilevel(LiteInvocation invocation, String... arguments) {
		return Result.supplyThrowing(NumberFormatException.class, () -> {
			var player = CommandUtilities.from_invocation(invocation);

			if (player == null)
				throw new NumberFormatException("bitch no player???");

			Location playerLocation = player.hook().getLocation();

			var x = parseDouble(playerLocation.getX(), arguments[0], true);
			var y = parseDouble(playerLocation.getY(), arguments[1], false);
			var z = parseDouble(playerLocation.getZ(), arguments[2], true);

			return new Location(player.hook().getWorld(), x, y, z);
		}).mapErr(ex -> "Invalid coordinates!");
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		var player = CommandUtilities.from_invocation(invocation);
		if (player == null)
			return Collections.emptyList();

		var playerLocation = player.hook().getLocation();
		return Arrays.asList(
					Suggestion.multilevel(String.valueOf(playerLocation.getBlockX()), String.valueOf(playerLocation.getBlockY()), String.valueOf(playerLocation.getBlockZ())),
					Suggestion.multilevel(String.valueOf(playerLocation.getBlockX() + 0.5), String.valueOf(playerLocation.getY()), String.valueOf(playerLocation.getBlockX() + 0.5)));
	}

	@Override
	public boolean validate(LiteInvocation invocation, Suggestion suggestion) {
		for (String suggest : suggestion.multilevelList()) {
			if (!suggest.matches("~?-?[\\d.]+")) { // -? - optional negative, \\d - digit, . - dot
				return false;
			}
		}

		return true;
	}

	@Override
	public int countMultilevel() {
		return 3;
	}

	public static double parseDouble(double base, String input, boolean centerBlock) throws NumberFormatException {
		var flag = input.startsWith("~");

		if (flag && Double.isNaN(base)) {
			throw new NumberFormatException("Invalid number: '" + input + "'");
		} else {
			var d0 = flag ? base : 0.0D;

			if (!flag || input.length() > 1) {
				var flag1 = input.contains(".");

				if (flag) {
					input = input.substring(1);
				}

				d0 += parseDouble(input);

				if (!flag1 && !flag && centerBlock) {
					d0 += 0.5D;
				}
			}

			var limit = 30_000_000;
			if (d0 < -limit) {
				throw new NumberFormatException("Too small coordinates!");
			}

			if (d0 > limit) {
				throw new NumberFormatException("Too big coordinates!");
			}

			return d0;
		}
	}

	public static double parseDouble(String input) throws NumberFormatException {
		try {
			var d0 = Double.parseDouble(input);

			if (!Doubles.isFinite(d0)) {
				throw new NumberFormatException("Invalid number: '" + input + "'");
			} else {
				return d0;
			}
		}
		catch (NumberFormatException var3) {
			throw new NumberFormatException("Invalid number: '" + input + "'");
		}
	}
}
