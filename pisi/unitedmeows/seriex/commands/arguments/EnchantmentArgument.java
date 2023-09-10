package pisi.unitedmeows.seriex.commands.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import panda.std.Result;
import pisi.unitedmeows.seriex.commands.utils.CommandUtilities;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.language.MinecraftLocale;

import static pisi.unitedmeows.seriex.util.language.MinecraftLocale.ENCHANTMENT_MAP;

@ArgumentName("enchantment")
public class EnchantmentArgument implements OneArgument<Enchantment> {
	@Override
	public Result<Enchantment, ?> parse(LiteInvocation invocation, String argument) {
		return CommandUtilities.findClosest(
					argument,
					() -> MinecraftLocale.getByName(argument),
					ENCHANTMENT_MAP.values().stream().toList(),
					enchantment -> ENCHANTMENT_MAP.inverse().get(enchantment));
	}
	
	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(ENCHANTMENT_MAP.keySet());
	}
}
