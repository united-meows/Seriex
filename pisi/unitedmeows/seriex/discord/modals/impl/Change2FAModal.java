package pisi.unitedmeows.seriex.discord.modals.impl;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.discord.modals.IModal;
import pisi.unitedmeows.seriex.util.math.Hashing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Change2FAModal implements IModal {
	@Override
	public String buttonName() {
		return "change_2fa";
	}

	@Override
	public String modalName() {
		return buttonName();
	}

	@Override
	public Modal createdModal() {
		TextInput usernameInput = TextInput
					.create("username", "Username", TextInputStyle.SHORT)
					.setPlaceholder("Your in-game username to change 2FA for.")
					.setRequired(true)
					.setRequiredRange(3, 16)
					.build();
		TextInput password = TextInput
					.create("password", "Password", TextInputStyle.SHORT)
					.setPlaceholder("Your password.")
					.setRequired(true)
					.setRequiredRange(8, 32)
					.build();

		return Modal.create(modalName(), "2FA Change")
					.addActionRows(
								ActionRow.of(usernameInput),
								ActionRow.of(password)
					)
					.build();
	}

	@Override
	public void modalInteraction(ModalInteractionEvent event, DiscordBot discordBot) {
		var username = event.getInteraction().getValue("username").getAsString();
		var password = event.getInteraction().getValue("password").getAsString();

		Consumer<String> reply = string -> event.reply(string).setEphemeral(true).complete();

		StructPlayer player = Seriex.get().database().getPlayer(username);
		if (player == null) {
			reply.accept(String.format("Couldn't find the player named `%s`, try again!", username));
			return;
		}

		StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(player.player_id);
		if (discord.snowflake != event.getUser().getIdLong()) {
			reply.accept(String.format("The player named `%s` is not linked to your discord account.", username));
			return;
		}

		if (!Objects.equals(Hashing.hashedString(player.salt + password), player.password)) {
			reply.accept(String.format("Wrong password for the player `%s`.", username));
			return;
		}

		List<String> defaultValue = new ArrayList<>();
		defaultValue.add("none");
		SelectMenu twoFA = SelectMenu
					.create("2FA-" + username)
					.setPlaceholder("Choose 2FA method")
					.setRequiredRange(1, 1)
					.setDefaultValues(defaultValue)
					.addOption("Discord 2FA", "discord")
					.addOption("None", "none").build();

		event.reply(String.format("Please select your 2FA method for the account %s.", username))
					.addActionRow(twoFA)
					.setEphemeral(true)
					.complete();
	}
}
