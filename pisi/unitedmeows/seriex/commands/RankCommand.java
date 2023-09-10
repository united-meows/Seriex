package pisi.unitedmeows.seriex.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.argument.option.Opt;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.commands.arguments.enums.RankModifierType;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.Arrays;

import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.NAME;

@Route(name = "rank")
public class RankCommand {
	@Execute(route = "modify", required = 3)
	public void modifyRank(PlayerW sender, @Arg Ranks rank, @Arg RankModifierType modifierType, @Arg String permission) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var mask = modifierType.ordinal() + 1;
		Seriex seriex = Seriex.get();
		seriex.rankManager().modifyPermissions(rank, permission, mask);
		seriex.msg(sender, Messages.COMMAND_RANK_MODIFIED, modifierType.name(), permission, rank.internalName());
	}

	@Execute(route = "list", min = 0, max = 1)
	public void listRanks(PlayerW sender, @Opt Option<Ranks> rank) {
		if(sender.doesntHaveRank(Ranks.HELPER)) return;
		var seriex = Seriex.get();

		if(rank.isDefined()) {
			var wantedRank = rank.get();
			seriex.msg_no_translation(sender, "Permissions for rank '%s' => %s",
						wantedRank.internalName(), seriex.rankManager().rankData(wantedRank).permissions().stream().collect(seriex.collector()));
		} else {
			seriex.msg_no_translation(sender, "Available ranks: %s", Arrays.stream(Ranks.values()).map(Ranks::internalName).collect(seriex.collector()));
		}
	}

	@Execute(route = "give", required = 2)
	public void giveRank(PlayerW sender, @Arg PlayerW player, @Arg Ranks rank) {
		if(sender.doesntHaveRank(Ranks.MAINTAINER)) return;
		player.updateRank(rank);
		Seriex.get().msg(sender, Messages.COMMAND_RANK_CHANGED, player.attribute(NAME), rank.internalName());
	}
}
