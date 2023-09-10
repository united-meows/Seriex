package pisi.unitedmeows.seriex.commands;

import java.util.*;
import java.util.stream.Collector;

import dev.rollczi.litecommands.argument.joiner.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.argument.option.Opt;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.root.RootRoute;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.commands.arguments.enums.EconomyOperation;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.Create;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes;

@RootRoute
public class OperatorCommands {

	@Execute(route = "whois", required = 1)
	public void who_is(PlayerW sender, @Arg StructPlayer databasePlayer) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var seriex = Seriex.get();

		seriex.msg_no_translation(sender, "Looking up player... (this may take a long time)");

		var database = seriex.database();

		var playerDiscord = database.getPlayerDiscord(databasePlayer.player_id);
		var discords = database.getPlayerDiscordAccounts(
					playerDiscord.snowflake);
		if (discords.isEmpty()) {
			seriex.msg_no_translation(sender, "Couldnt look up player!");
			return;
		}

		var playerIDList = discords.stream().map(struct -> struct.player_id).toList();
		seriex.msg_no_translation(sender, "Looking up alt accounts...");

		List<StructPlayer> alts = Create.create(new ArrayList<>(), list -> {
			playerIDList.forEach(id -> {
				var databasePlayerFromAlt = database.getPlayer(id);
				if (databasePlayerFromAlt == null) return;
				list.add(databasePlayerFromAlt);
			});
		});

		Set<String> ip_addresses = new HashSet<>();
		alts.forEach(alt -> {
			if (alt == null) return;

			var logins = database.getPlayerLogins(alt.player_id);
			logins.forEach(login -> ip_addresses.add(login.ip_address));
		});

		Collector<CharSequence, ?, String> collector = seriex.collector();
		seriex.msg_no_translation(sender, "Found accounts: %s", alts.stream().map(alt -> alt.username).collect(collector));
		seriex.msg_no_translation(sender, "Found ip adresses: %s", ip_addresses.stream().collect(collector));
		var userById = seriex.discordBot().JDA().getUserById(playerDiscord.snowflake);
		var showedDiscordInformation = userById == null
					? String.valueOf(playerDiscord.snowflake)
					: userById.getName() + "#" + userById.getDiscriminator();
		seriex.msg_no_translation(sender, "Found discord account: %s", showedDiscordInformation);
	}

	@Execute(route = "maintenance", min = 0, max = 1)
	public void maintenance(PlayerW sender, @Opt Option<Boolean> other) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;
		var seriex = Seriex.get();
		boolean state = other.orElseGet(!seriex.maintenance());
		seriex.maintenance(state);
		seriex.msg_state(sender, Messages.COMMAND_MAINTENANCE, state);
	}

	@Execute(route = "gamemode", aliases = {
				"gm"
	}, min = 1, max = 2)
	public void gamemode(PlayerW sender, @Arg GameMode gameMode, @Opt Option<PlayerW> other) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var seriex = Seriex.get();
		other.orElseGet(sender).hook().setGameMode(gameMode);
		seriex.msg(sender, Messages.COMMAND_GM, gameMode.name());
	}

	private enum InvseeType {
		NORMAL,
		ENDER_CHEST,
		ARMOR
	}

	@Execute(route = "invsee", required = 2)
	public void invsee(PlayerW sender, @Arg PlayerW other, @Arg InvseeType type) {
		if (sender.doesntHaveRank(Ranks.MAINTAINER)) return;

		var seriex = Seriex.get();

		var hooked = other.hook();
		var inventory = switch (type) {
			case NORMAL -> hooked.getInventory();
			case ARMOR -> {
				var armorInventory = seriex.plugin().getServer().createInventory(hooked, 9, "Armor");
				armorInventory.setContents(hooked.getInventory().getArmorContents());
				yield armorInventory;
			}
			case ENDER_CHEST -> hooked.getEnderChest();
		};

		if (inventory != null) {
			sender.hook().closeInventory();
			sender.hook().openInventory(inventory);
			sender.invsee(true);
			Seriex.get().msg_no_translation(sender, "Opening '%s' of player '%s'.", type, other.attribute(Attributes.NAME));
		}
	}

	@Execute(route = "economy", min = 2, max = 3)
	public void economy(PlayerW sender, @Arg EconomyOperation economyOperation, @Arg StructPlayer structPlayer, @Opt Option<Integer> coinAmount) {
		var seriex = Seriex.get();
		var player_id = structPlayer.player_id;
		var wallet = seriex.database().getPlayerWallet(player_id);

		if (economyOperation == EconomyOperation.BALANCE) {
			seriex.msg_no_translation(sender, "Player '%s' has '%s' pawcoins. (wallet %s)", structPlayer.username, wallet.coins, wallet.player_wallet);
			return;
		}

		if (sender.doesntHaveRank(Ranks.MAINTAINER))
			return;

		if (!coinAmount.isDefined()) {
			seriex.msg_no_translation(sender, "You forgot to add coinAmount as the last argument");
			return;
		}

		int previous = wallet.coins;
		var modifier = coinAmount.get();

		if (economyOperation == EconomyOperation.GIVE) {
			// why does intellij say it can be replaced with == :DDDD
			if (wallet.coins + modifier == Integer.MAX_VALUE)
				wallet.coins = Integer.MAX_VALUE;

			wallet.coins += modifier;
		} else {
			wallet.coins -= modifier;

			if (wallet.coins < 0)
				wallet.coins = 0;
		}
		wallet.update();
		seriex.msg_no_translation(sender, "Player '%s' now has '%s' pawcoins. (from '%s' pawcoins)", structPlayer.username, wallet.coins, previous);
	}

	@Execute(route = "kick", min = 1)
	public void kick(PlayerW sender, @Arg PlayerW kicked, @Joiner String message) {
		if (sender.doesntHaveRank(Ranks.HELPER))
			return;

		Seriex.get().kick_no_translation(kicked.hook(), message);
	}

	@Execute(route = "ban", min = 1)
	public void ban(PlayerW sender, @Arg StructPlayer bannedPlayer, @Joiner String message) {
		if (sender.doesntHaveRank(Ranks.HELPER))
			return;

		var srx = Seriex.get();
		if (bannedPlayer.banned) {
			srx.msg(sender, Messages.COMMAND_BAN_PLAYER_ALREADY_BANNED, bannedPlayer.username);
			return;
		}

		var bukkitPlayer = Bukkit.getPlayer(bannedPlayer.username);
		var banMessage = srx.I18n().getMessage(
					Messages.COMMAND_BAN_PLAYER_DISCORD_MESSAGE,
					Language.fromCode(srx.database().getPlayerSettings(bannedPlayer.player_id).selectedLanguage, Language.ENGLISH),
					bannedPlayer.username,
					message);

		if (bukkitPlayer != null)
			srx.kick_no_translation(bukkitPlayer, banMessage);

		bannedPlayer.banned = true;
		bannedPlayer.update();
		srx.msg(sender, Messages.COMMAND_BAN_PLAYER_BANNED, bannedPlayer.username);
		srx.discordBot().sendDirectMessage(
					srx.database().getPlayerDiscord(bannedPlayer.player_id).snowflake,
					banMessage
		);
	}

	@Execute(route = "unban", aliases = { "pardon" }, min = 1)
	public void unban(PlayerW sender, @Arg StructPlayer pardonedPlayer, @Joiner String message) {
		if (sender.doesntHaveRank(Ranks.HELPER))
			return;

		var srx = Seriex.get();
		if (!pardonedPlayer.banned) {
			srx.msg(sender, Messages.COMMAND_BAN_PLAYER_ALREADY_NOT_BANNED, pardonedPlayer.username);
			return;
		}

		var unbanMessage = srx.I18n().getMessage(
					Messages.COMMAND_UNBAN_PLAYER_DISCORD_MESSAGE,
					Language.fromCode(srx.database().getPlayerSettings(pardonedPlayer.player_id).selectedLanguage, Language.ENGLISH),
					pardonedPlayer.username,
					message);

		pardonedPlayer.banned = false;
		pardonedPlayer.update();
		srx.msg(sender, Messages.COMMAND_BAN_PLAYER_UNBANNED, pardonedPlayer.username);
		srx.discordBot().sendDirectMessage(
					srx.database().getPlayerDiscord(pardonedPlayer.player_id).snowflake,
					unbanMessage
		);
	}
}
