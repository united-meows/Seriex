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
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

@ArgumentName("target")
public class PlayerWArgument implements OneArgument<PlayerW> {

	@Override
	public Result<PlayerW, ?> parse(LiteInvocation invocation, String argument) {
		var bukkitPlayer = Seriex.get().plugin().getServer().getPlayerExact(argument);
		if(bukkitPlayer == null) return Result.error("Player not found");

		var playerW = Seriex.get().dataManager().user(bukkitPlayer);
		if(playerW == null) return Result.error("PlayerW not found?");

		return Result.ok(playerW);
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Seriex.get().plugin().getServer().getOnlinePlayers().stream()
					.map(HumanEntity::getName)
					.map(Suggestion::of)
					.toList();
	}
} 
