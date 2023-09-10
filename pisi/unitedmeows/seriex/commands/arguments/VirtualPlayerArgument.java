package pisi.unitedmeows.seriex.commands.arguments;

import dev.rollczi.litecommands.argument.ArgumentName;
import dev.rollczi.litecommands.argument.simple.OneArgument;
import dev.rollczi.litecommands.command.LiteInvocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import org.bukkit.entity.HumanEntity;
import panda.std.Option;
import panda.std.Result;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;

import java.util.List;

@ArgumentName("virtualplayer")
public class VirtualPlayerArgument implements OneArgument<VirtualPlayer> {

	@Override
	public Result<VirtualPlayer, ?> parse(LiteInvocation invocation, String argument) {
		var vp = Seriex.get().virtualPlayerManager().get(argument);

		if(vp == null)
			return Result.error("No virtual player found");

		return Result.ok(vp);
	}

	@Override
	public List<Suggestion> suggest(LiteInvocation invocation) {
		return Suggestion.of(Seriex.get()
					.virtualPlayerManager()
					.getVirtualPlayers()
					.values()
					.stream()
					.map(vp -> vp.virtualProfile().getName())
					.toList());
	}
}
