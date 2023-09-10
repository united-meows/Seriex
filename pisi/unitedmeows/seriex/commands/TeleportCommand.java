package pisi.unitedmeows.seriex.commands;

import org.bukkit.Location;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

@Route(name = "teleport" , aliases = "tp")
public class TeleportCommand {

	@Execute(required = 3)
	public void to(PlayerW sender, @Arg Location location) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		sender.hook().teleport(location);
	}

	@Execute(required = 1)
	public void toPlayer(PlayerW sender, @Arg PlayerW to) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		sender.teleport(to);
	}

	@Execute(required = 2)
	public void targetToPlayer(PlayerW sender, @Arg PlayerW target, @Arg PlayerW to) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		target.teleport(to);
	}
}
