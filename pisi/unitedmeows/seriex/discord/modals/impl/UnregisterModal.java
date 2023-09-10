package pisi.unitedmeows.seriex.discord.modals.impl;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.discord.modals.IModal;
import pisi.unitedmeows.seriex.util.math.Hashing;

import java.util.Objects;
import java.util.function.Consumer;

public class UnregisterModal implements IModal {
	@Override
	public String buttonName() {
		return "unregister";
	}

	@Override
	public String modalName() {
		return buttonName();
	}

	@Override
	public Modal createdModal() {
		TextInput usernameInput = TextInput
					.create("username", "Username", TextInputStyle.SHORT)
					.setPlaceholder("Your in-game username to unregister.")
					.setRequired(true)
					.setRequiredRange(3, 16)
					.build();
		TextInput recoveryKey = TextInput
					.create("recovery_key", "Recovery key", TextInputStyle.SHORT)
					.setPlaceholder("Your accounts recovery key.")
					.setRequired(true)
					.build();
		TextInput password = TextInput
					.create("password", "Password", TextInputStyle.SHORT)
					.setPlaceholder("Your password.")
					.setRequired(true)
					.setRequiredRange(8, 32)
					.build();
		return Modal
					.create(modalName(), "Unregister")
					.addActionRows(
								ActionRow.of(recoveryKey),
								ActionRow.of(usernameInput),
								ActionRow.of(password))
					.build();
	}

	@Override
	public void modalInteraction(ModalInteractionEvent event, DiscordBot discordBot) {
		var username = event.getValue("username").getAsString();
		var recovery_key = event.getValue("recovery_key").getAsString();
		var password = event.getValue("password").getAsString();

		Consumer<String> reply = string -> event.reply(string).setEphemeral(true).complete();
		
		var player = Seriex.get().database().getPlayer(username);
		if (player == null) {
			reply.accept(String.format("Couldn't find the player named `%s`, try again!", username));
			return;
		}
		
		if (!Objects.equals(player.recovery_key, recovery_key)) {
			reply.accept(String.format("Recovery keys dont match for the player `%s`, try again!", username));
			return;
		}

		if (!Objects.equals(Hashing.hashedString(player.salt + password), player.password)) {
			reply.accept(String.format("Wrong password for the player `%s`.", username));
			return;
		}

		var removed = Seriex.get().database().removePlayer(player.player_id);
		if (!removed)
			reply.accept("Something went wrong.");
		else reply.accept("Unregistered account `%s`".formatted(username));
	}
}
