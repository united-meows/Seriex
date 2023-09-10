package pisi.unitedmeows.seriex.discord.utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.language.Language;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pisi.unitedmeows.seriex.discord.DiscordBot.*;

public class MessageHandler {
	private static final Color DISCORD_BOT_COLOR = new Color(8281781);

	private MessageHandler() {

	}

	public static MessageHandler create() {
		return new MessageHandler();
	}

	public void handleMessages(MessageReceivedEvent event) {
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);

		if (!event.isFromGuild()) return;

		if (serverChatMessage(event, discordConfig)) return;

		String contentRaw = event.getMessage().getContentRaw();

		if (!event.getMember().getUser().equals(event.getJDA().getSelfUser())) return;

		switch (contentRaw) {
			case INTERNAL_REGISTER_COMMAND -> this.registerCommand(event, discordConfig);
			case INTERNAL_RECOVERY_COMMAND -> this.recoveryCommand(event, discordConfig);
			case INTERNAL_SETTINGS_COMMAND -> this.settingsCommand(event,discordConfig);
			case INTERNAL_LANGUAGE_COMMAND -> event.getChannel()
						.sendMessage("```md\n# Select your languages.\n```")
						.queue(completeMessage -> Arrays.stream(Language.values()).forEach(lang -> completeMessage.addReaction(Emoji.fromFormatted(lang.unicode())).queue()));
		}
	}

	private void settingsCommand(MessageReceivedEvent event, DiscordConfig discordConfig) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setImage(discordConfig.BANNER.value());
		builder.addField("> ```ACCOUNT MANAGEMENT```\n", "", false);
		builder.addField("", "```less\n[=======================================]\n```", false);
		builder.addField("", """
									```md
									> You can manage your account here.
									# Changing 2FA preferences
									* Click the `Change 2FA preferences` button
									# View your collected information
									* Click the `View account information` button
									```""", false);
		builder.addField("", "```less\n[=======================================]\n```", false);
		builder.setColor(DISCORD_BOT_COLOR);
		List<ItemComponent> alo = new ArrayList<>();
		alo.add(Button.primary("change_2fa", "Change 2FA preferences"));
		alo.add(Button.primary("view_info", "View account information"));
		event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(alo).complete();
	}

	private void recoveryCommand(MessageReceivedEvent event, DiscordConfig discordConfig) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setImage(discordConfig.BANNER.value());
		builder.addField("> ```ACCOUNT RECOVERY```\n", "", false);
		builder.addField("", "```less\n[=======================================]\n```", false);
		builder.addField("", """
					```md
					# ============================================ #
					#  Recovering    in-game    account   password #
					#  (must be linked to current discord account) #
					# ============================================ #
					```\
					```md
					1. If you are not linked to your current discord account, recover your in-game account link first.
					2. Click the `Forgot my password` button.
					3. Input your in-game name, old password & recovery key.
					4. Input your new password.
					5. Input your 2FA authentication code if you have 2FA.
					```""", false);
		builder.addField("", "```less\n[=======================================]\n```", false);
		builder.addField("", """
					```md
					# ====================================== #
					#  Unregister in-game & discord accounts #
					# ====================================== #
					```\
					```md
					1. Click the `Unregister account` button.
					2. You should have received a recovery key in your direct messages with the bot when you registered.\s
					3. Input your in-game name, password & recovery key.
					4. If you have 2FA, input your 2FA authentication code.
					```""", false);
		builder.addField("", "```less\n[=======================================]\n```", false);
		builder.setColor(DISCORD_BOT_COLOR);
		List<ItemComponent> alo = new ArrayList<>();
		alo.add(Button.primary("password-recovery", "Forgot my password"));
		alo.add(Button.primary("unregister", "Unregister an account"));
		event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(alo).complete();
	}

	private void registerCommand(MessageReceivedEvent event, DiscordConfig discordConfig) {
		var serverConfig = (ServerConfig) Seriex.get().fileManager().config(ServerConfig.class);

		EmbedBuilder builder = new EmbedBuilder();
		builder.setImage(discordConfig.BANNER.value());
		builder.addField("> ```NOTICE```", "By registering, you accept " + "__**our TOS, Discord`s TOS and our rules**__.", true);
		StringBuilder ruleBuilder = new StringBuilder();
		String[] rules = {
					"Be respectful.",
					"No spamming.",
					"No doxing/ddosing other users.",
					"Any activity that might harm the server or the discord is being monitored by us and could lead to a ban.",
					"Only one account per discord account.",
					"",
					"#These rules might change at any time without notice.",
					"#It is your responsibility to check for them."
		};
		int[] index = {
					0
		};
		ruleBuilder.append("```md\n");
		Arrays.stream(rules).forEach(rule -> {
			if (!rule.isEmpty()) {
				index[0]++;
				if (rule.charAt(0) == '#') {
					ruleBuilder.append("> ").append(rule.substring(1)).append("\n");
				} else
					ruleBuilder.append(index[0]).append(". ").append(rule).append("\n");
			} else {
				ruleBuilder.append("\n");
			}
		});
		ruleBuilder.append("```");
		builder.addField("> ```RULES```", ruleBuilder.toString(), false);
		builder.addField("", String.format("After registering please head to <#%s> to select your language(s))!", event.getGuild().getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId()), false);
		builder.setColor(DISCORD_BOT_COLOR);
		builder.setTitle("Welcome to Seriex!");
		List<ItemComponent> alo = new ArrayList<>();
		alo.add(Button.primary("register", "Click to register!"));
		alo.add(Button.link(serverConfig.SERVER_WEBSITE.value() + "/tos.txt", "Seriex TOS"));
		alo.add(Button.link("https://discord.com/terms", "Discord TOS"));
		event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(alo).complete();
	}

	private boolean serverChatMessage(MessageReceivedEvent event, DiscordConfig discordConfig) {
		if (event.getChannel().getId().equals(discordConfig.ID_SERVER_CHAT.value()) && !event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
			String name = event.getAuthor().getName();
			String contentDisplay = event.getMessage().getContentDisplay().replace("\n", "");
			if (!contentDisplay.isEmpty())
				Bukkit.broadcastMessage(Seriex.colorizeString(String.format("&8[&9DIS&bCORD&8]&r %s &7&l> &7%s", name, contentDisplay)));
			return true;
		}

		return false;
	}
}
