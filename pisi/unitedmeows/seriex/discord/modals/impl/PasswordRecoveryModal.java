package pisi.unitedmeows.seriex.discord.modals.impl;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.discord.modals.IModal;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.safety.Try;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PasswordRecoveryModal implements IModal {
	@Override
	public String buttonName() {
		return "password-recovery";
	}

	@Override
	public String modalName() {
		return buttonName();
	}

	@Override
	public Modal createdModal() {
		TextInput usernameInput = TextInput
					.create("username", "Username", TextInputStyle.SHORT)
					.setPlaceholder("Your in-game username.")
					.setRequired(true)
					.setRequiredRange(3, 16)
					.build();
		TextInput recoveryKey = TextInput
					.create("recovery_key", "Recovery key", TextInputStyle.SHORT)
					.setPlaceholder("Your accounts recovery key.")
					.setRequired(true)
					.build();
		TextInput newPassword = TextInput
					.create("new_password", "New password", TextInputStyle.SHORT)
					.setPlaceholder("Your new password.")
					.setRequired(true)
					.setRequiredRange(8, 32)
					.build();
		return Modal
					.create(modalName(), "Password recovery")
					.addActionRows(
								ActionRow.of(recoveryKey),
								ActionRow.of(usernameInput),
								ActionRow.of(newPassword))
					.build();
	}

	@Override
	public void modalInteraction(ModalInteractionEvent event, DiscordBot discordBot) {
		var username = event.getValue("username").getAsString();
		var recovery_key = event.getValue("recovery_key").getAsString();
		var new_password = event.getValue("new_password").getAsString();

		Consumer<String> reply = string -> event.reply(string).setEphemeral(true).complete();

		StructPlayer player = Seriex.get().database().getPlayer(username);
		if (player == null) {
			reply.accept("Couldn't find the player named `%s`, try again!".formatted(username));
			return;
		}

		StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(player.player_id);
		if (discord.snowflake != event.getUser().getIdLong()) {
			reply.accept("The player named `%s` is not linked to your discord account.".formatted(username));
			return;
		}

		if (!Objects.equals(player.recovery_key, recovery_key)) {
			reply.accept("Recovery keys dont match for the player `%s`, try again!".formatted(username));
			return;
		}

		Try.safe(() -> {
			player.salt = Hashing.randomString(8);
			player.password = Hashing.hashedString(player.salt + new_password);
			player.update();
			reply.accept("Successfully updated your password.");
		}, () -> reply.accept("Couldn't update your password, please contact a maintainer."));
	}
}
