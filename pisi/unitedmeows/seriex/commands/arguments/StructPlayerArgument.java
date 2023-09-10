package pisi.unitedmeows.seriex.commands.arguments;

import java.util.List;

import org.bukkit.entity.HumanEntity;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import panda.std.Option;
import panda.std.Result;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;

@ArgumentName("username")
public class StructPlayerArgument implements OneArgument<StructPlayer> {

	@Override
	public Result<StructPlayer, ?> parse(LiteInvocation invocation, String argument) {
		return Option.of(Seriex.get().database().getPlayer(argument)).toResult("Player not found.");
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Seriex.get().plugin().getServer().getOnlinePlayers().stream()
					.map(HumanEntity::getName)
					.map(Suggestion::of)
					.toList();
	}
}
