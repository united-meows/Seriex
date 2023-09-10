package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import org.bukkit.potion.PotionType;
import panda.std.Result;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.EditDistance;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@ArgumentName("rank")
public class RankArgument implements OneArgument<Ranks> {
	@Override
	public Result<Ranks, ?> parse(LiteInvocation invocation, String argument) {
		return CommandUtilities.findClosest(
					argument,
					() -> Ranks.of(argument),
					Arrays.asList(Ranks.values()),
					Ranks::internalName);
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(
					Arrays.stream(Ranks.values())
								.map(rank -> rank.internalName().toLowerCase(Locale.ENGLISH))
								.toList());
	}
}
