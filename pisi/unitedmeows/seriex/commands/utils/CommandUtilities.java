package pisi.unitedmeows.seriex.commands.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.rollczi.litecommands.command.LiteInvocation;
import panda.std.Result;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.EditDistance;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public class CommandUtilities {

	public static PlayerW from_invocation(LiteInvocation inv) {
		// If the sender is BukkitSender, this getHandle should return
		// CommandSender, which we safely cast to Player -> PlayerW
		var sender = inv.sender().getHandle();
		if (sender instanceof CommandSender commandSender && commandSender instanceof Player player)
			return Seriex.get().dataManager().user(player);

		return null;
	}


	public static <T> Result<T, ?> findClosest(
				String argument,
				Supplier<T> supplier,
				List<T> values,
				Function<T, String> function) {
		var supplied = supplier.get();

		if(supplied == null) {
			int lowest = Integer.MAX_VALUE;
			T closest = null;

			for (T available : values) {
				var converted = function.apply(available);
				int damerauLevenshtein = EditDistance.damerauLevenshtein(
							argument.toUpperCase(Locale.ENGLISH),
							converted.toUpperCase(Locale.ENGLISH)
				);

				if (damerauLevenshtein > lowest)
					continue;

				lowest = damerauLevenshtein;
				closest = available;
			}

			if (closest == null)
				return Result.error("Invalid argument");

			var converted = function.apply(closest);
			return Result.error("Invalid argument (did you mean '%s' ?)".formatted(converted));
		}

		return Result.ok(supplied);
	}
}
