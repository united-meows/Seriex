package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import org.bukkit.potion.PotionType;
import panda.std.Result;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;
import pisi.unitedmeows.seriex.managers.rank.Ranks;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@ArgumentName("potionType")
public class PotionTypeArgument implements OneArgument<PotionType> {
	@Override
	public Result<PotionType, ?> parse(LiteInvocation invocation, String argument) {
		return CommandUtilities.findClosest(
					argument,
					() -> PotionType.valueOf(argument.toUpperCase(Locale.ENGLISH)),
					Arrays.asList(PotionType.values()),
					PotionType::name);
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(
					Arrays.stream(PotionType.values())
								.map(Enum::name)
								.map(s -> s.toLowerCase(Locale.ENGLISH))
								.toList());
	}
}
