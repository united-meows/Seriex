package pisi.unitedmeows.seriex.commands.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.rollczi.litecommands.command.LiteInvocation;
import panda.std.Result;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
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

		if (supplied == null) {
			int lowest = Integer.MAX_VALUE;
			T closest = null;

			for (T available : values) {
				var converted = function.apply(available);
				int damerauLevenshtein = damerauLevenshtein(
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

	public static int damerauLevenshtein(CharSequence source, CharSequence target) {
		if (source == null || target == null) throw new IllegalArgumentException("Parameter must not be null");
		int sourceLength = source.length();
		int targetLength = target.length();
		if (sourceLength == 0) return targetLength;
		if (targetLength == 0) return sourceLength;
		int[][] dist = new int[sourceLength + 1][targetLength + 1];
		for (int i = 0; i < sourceLength + 1; i++) {
			dist[i][0] = i;
		}
		for (int j = 0; j < targetLength + 1; j++) {
			dist[0][j] = j;
		}
		for (int i = 1; i < sourceLength + 1; i++) {
			for (int j = 1; j < targetLength + 1; j++) {
				int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
				dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
				if (i > 1 && j > 1 && source.charAt(i - 1) == target.charAt(j - 2) && source.charAt(i - 2) == target.charAt(j - 1)) {
					dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + 1);
				}
			}
		}
		return dist[sourceLength][targetLength];
	}
}
