package pisi.unitedmeows.seriex.commands;

import dev.derklaro.reflexion.Reflexion;
import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.argument.option.Opt;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.commands.arguments.enums.MovementHookType;
import pisi.unitedmeows.seriex.commands.arguments.enums.PlayerDatabaseType;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.NAME;

@Route(name = "lookup")
public class LookupCommand {
	@Execute(route = "database", min = 2, max = 3)
	public void database(PlayerW sender, @Arg StructPlayer structPlayer, @Arg PlayerDatabaseType infoType, @Opt Option<String> fieldName) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER))
			return;

		var seriex = Seriex.get();

		Consumer<IStruct> fieldConsumer = struct -> {
			String field = fieldName.orNull();
			if (field == null) {
				seriex.msg_no_translation(sender, "Please enter a field_name for the third argument. (available fields: %s)",
							Arrays.stream(struct.getClass().getDeclaredFields()).map(Field::getName).collect(seriex.collector()));
				return;
			}
			Reflexion.on(struct.getClass()).findField(field).ifPresentOrElse(accessor -> {
				seriex.msg_no_translation(sender, "player='%s', '%s'='%s'", structPlayer.username, field, accessor.getValue(struct).getOrElse(null));
			}, () -> {
				seriex.msg_no_translation(sender, "No field named '%s'", field);
			});
		};

		var database = seriex.database();

		switch (infoType) {
			case PLAYER -> fieldConsumer.accept(database.getPlayer(structPlayer.player_id));
			case DISCORD -> fieldConsumer.accept(database.getPlayerDiscord(structPlayer.player_id));
			case LOGINS -> seriex.msg_no_translation(sender, "Not supported yet :D"); // fieldConsumer.accept(database.getPlayerLogins(structPlayer.player_id));
			case SETTINGS -> fieldConsumer.accept(database.getPlayerSettings(structPlayer.player_id));
			case WALLET -> fieldConsumer.accept(database.getPlayerWallet(structPlayer.player_id));
		}
	}

	@Execute(route = "local", min = 1, max = 2)
	public void local(PlayerW sender, @Arg PlayerW playerW, @Opt Option<String> fieldName) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER))
			return;

		var seriex = Seriex.get();

		Consumer<PlayerW> playerConsumer = lookedUpPlayer -> {
			String field = fieldName.orNull();
			if (field == null) {
				seriex.msg_no_translation(sender, "Please enter a field_name for the third argument. (available fields: %s)", Arrays.stream(lookedUpPlayer.getClass().getDeclaredFields()).map(Field::getName).collect(seriex.collector()));
				return;
			}
			Reflexion.on(lookedUpPlayer.getClass()).findField(field).ifPresentOrElse(accessor -> {
				seriex.msg_no_translation(sender, "player='%s', '%s'='%s'", playerW.attribute(NAME), field, accessor.getValue(lookedUpPlayer).getOrElse(null));
			}, () -> {
				seriex.msg_no_translation(sender, "No field named '%s'", field);
			});
		};

		playerConsumer.accept(playerW);
	}

	@Execute(route = "movement", required = 2)
	public void movement(PlayerW sender, @Arg PlayerW playerW, @Arg MovementHookType hookType) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER))
			return;

		var seriex = Seriex.get();
		if(hookType == MovementHookType.HOOK) {
			seriex.playerLogger().hookToPlayer(sender, playerW);
		} else if(hookType == MovementHookType.UNHOOK) {
			seriex.playerLogger().unhookToPlayer(sender, playerW);
		} else throw SeriexException.create("Unsupported hook operation " + hookType.name());
	}

	@Execute(route = "dump", required = 1)
	public void movement(PlayerW sender, @Arg PlayerW toDump) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER))
			return;

		Seriex.get().msg_no_translation(sender, toDump.toString());
	}
}
