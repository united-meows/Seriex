package pisi.unitedmeows.seriex.discord.modals.impl;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerWallet;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.discord.modals.IModal;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.math.Primitives;
import pisi.unitedmeows.yystal.utils.CoID;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

public class RegisterModal implements IModal {
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
	private static final String REGISTER_DM = """
				Registered as %s \
				Your recovery key: ||%s|| \
				The recovery key is used for changing your password,\
				unregistering your account, and for other purposes.
				Save it somewhere in your computer when you somehow lose your discord account.
				""";

	@Override
	public String buttonName() {
		return "register";
	}

	@Override
	public String modalName() {
		return "verify_panel";
	}

	@Override
	public Modal createdModal() {
		TextInput usernameInput = TextInput
					.create("username", "Username", TextInputStyle.SHORT)
					.setPlaceholder("Your in-game username.")
					.setRequired(true)
					.setRequiredRange(3, 16)
					.build();
		TextInput passwordInput = TextInput
					.create("password", "Password", TextInputStyle.SHORT)
					.setPlaceholder("Your password.")
					.setRequired(true)
					.setRequiredRange(8, 32)
					.build();
		TextInput re_passwordInput = TextInput
					.create("password_again", "Password confirmation", TextInputStyle.SHORT)
					.setPlaceholder("Your password again.")
					.setRequired(true)
					.setRequiredRange(8, 32)
					.build();
		return Modal
					.create(modalName(), "Verification")
					.addActionRows(
								ActionRow.of(usernameInput),
								ActionRow.of(passwordInput),
								ActionRow.of(re_passwordInput))
					.build();
	}

	private boolean invalidInputs(ModalInteractionEvent event, String username, String password, String passwordAgain) {
		if (!USERNAME_PATTERN.matcher(username).find()) {
			event.reply("Invalid username %s!".formatted(username)).setEphemeral(true).complete();
			return true;
		}

		if (!password.equals(passwordAgain)) {
			event.reply("Passwords dont match!").setEphemeral(true).complete();
			return true;
		}

		return false;
	}

	private StructPlayer createPlayer(String username, String password) {
		StructPlayer structPlayer = new StructPlayer();
		structPlayer.username = username;
		structPlayer.salt = Hashing.randomString(8);
		structPlayer.recovery_key = Hashing.hashedString(Hashing.randomString(16));
		structPlayer.password = Hashing.hashedString(structPlayer.salt + password);
		structPlayer.token = CoID.generate().toString();
		structPlayer.has2FA = false;
		return structPlayer;
	}

	private StructPlayerWallet createWallet(String username) {
		StructPlayerWallet structPlayerWallet = new StructPlayerWallet();
		structPlayerWallet.coins = 0;
		structPlayerWallet.player_wallet = "0x" + Hashing.hashedString(String.format("%032x", Primitives.hash(username.hashCode(), Hashing.randomString(4).hashCode())));
		return structPlayerWallet;
	}

	private StructPlayerDiscord createDiscord(Member member) {
		var user = member.getUser();
		var discord = new StructPlayerDiscord();
		discord.snowflake = member.getIdLong();
		discord.joinedAs = user.getName() + "#" + user.getDiscriminator();
		discord.linkMS = System.currentTimeMillis();
		return discord;
	}

	private boolean sendDM(ModalInteractionEvent event, Member member) {
		User user = member.getUser();
		try {
			PrivateChannel privateChannel = user.openPrivateChannel().complete();
			privateChannel.sendMessage("Welcome to Seriex!").complete();
			return false;
		}
		catch (Exception e) {
			Seriex.get().logger().error("Couldnt open DM with user {} ({})", member.getEffectiveName(), e.getLocalizedMessage());
			event.reply("Please open your direct messages for the bot to send messages.").setEphemeral(true).complete();
			return true;
		}
	}

	private boolean usernameTaken(ModalInteractionEvent event, String username) {
		if (Seriex.get().database().getPlayer(username) != null) {
			event.reply(String.format("A player with the username %s already exists!", username)).setEphemeral(true).complete();
			return true;
		}

		return false;
	}

	private void handleDatabase(ModalInteractionEvent event, StructPlayer player, StructPlayerWallet wallet, StructPlayerDiscord discord) {
		IntConsumer errorMessage = integer -> event.reply("Couldnt register! (0x%d)".formatted(integer)).setEphemeral(true).complete();


		if (!player.create()) {
			errorMessage.accept(0);
			return;
		}

		var databasePlayer = Seriex.get().database().getPlayer(player.username);
		if (databasePlayer == null) {
			errorMessage.accept(1);
			return;
		}

		int databaseID = databasePlayer.player_id;

		player.player_id = databaseID;
		wallet.player_id = databaseID;
		discord.player_id = databaseID;

		if (!discord.create()) {
			errorMessage.accept(2);
			return;
		}

		if (!wallet.create()) {
			errorMessage.accept(3);
		}
	}

	@Override
	public void modalInteraction(ModalInteractionEvent event, DiscordBot bot) {
		String guildID = Objects.requireNonNull(event.getGuild()).getId();

		var username = Objects.requireNonNull(event.getInteraction().getValue("username")).getAsString();
		var password = Objects.requireNonNull(event.getInteraction().getValue("password")).getAsString();
		var passwordAgain = Objects.requireNonNull(event.getInteraction().getValue("password_again")).getAsString();

		if (invalidInputs(event, username, password, passwordAgain)) return;
		if (usernameTaken(event, username)) return;

		var member = Objects.requireNonNull(event.getMember());

		var player = createPlayer(username, password);
		var wallet = createWallet(username);
		var discord = createDiscord(member);

		if (sendDM(event, member)) return;

		handleDatabase(event, player, wallet, discord);

		long idLong = member.getIdLong();
		UserSnowflake snowflake = UserSnowflake.fromId(idLong);
		Role guildVerifiedRole = bot.discordCache.verifiedRoles().get(guildID);
		event.getGuild().addRoleToMember(snowflake, guildVerifiedRole).complete();
		event.getUser()
					.openPrivateChannel()
					.queueAfter(1, TimeUnit.SECONDS, (PrivateChannel channel) -> {
						String formattedString = String.format(REGISTER_DM, username, player.recovery_key);
						channel.sendMessage(formattedString).queue();
					});

	}
}
