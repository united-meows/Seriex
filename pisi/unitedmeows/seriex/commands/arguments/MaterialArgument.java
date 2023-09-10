package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import org.bukkit.Material;
import panda.std.Result;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@ArgumentName("material")
public class MaterialArgument implements OneArgument<Material> {
	@Override
	public Result<Material, ?> parse(LiteInvocation invocation, String argument) {
		return CommandUtilities.findClosest(
					argument,
					() -> Material.getMaterial(argument.toUpperCase(Locale.ENGLISH)),
					Arrays.asList(Material.values()),
					material -> material.name().toUpperCase(Locale.ENGLISH)
		);
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(
					Arrays.stream(Material.values())
								.map(Enum::name)
								.map(s -> s.toLowerCase(Locale.ENGLISH))
								.toList());
	}
}
