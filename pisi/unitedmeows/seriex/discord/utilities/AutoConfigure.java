package pisi.unitedmeows.seriex.discord.utilities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.language.Language;

import java.util.*;
import java.util.function.Consumer;

import static net.dv8tion.jda.api.Permission.*;
import static net.dv8tion.jda.api.Permission.VIEW_AUDIT_LOGS;
import static pisi.unitedmeows.seriex.discord.DiscordBot.*;

// TODO
public class AutoConfigure {
	private final DiscordBot discordBot;

	private AutoConfigure(DiscordBot discordBot) {
		this.discordBot = discordBot;
	}

	public static AutoConfigure create(DiscordBot discordBot) {
		return new AutoConfigure(discordBot);
	}

	private boolean handleConfiguration(MessageReceivedEvent event) {
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);
		String contentRaw = event.getMessage().getContentRaw();

		if (event.getMember().isOwner() && "$setup".equals(contentRaw)) {
			configureGuild(discordBot.JDA(), discordConfig, event.getGuild());
			return true;
		}
		if (event.getMember().isOwner() && "$setup2".equals(contentRaw)) {
			event.getGuild().getChannels().stream().filter(guild -> !"general".equals(guild.getName()))
						.forEach(guild -> guild.delete().queue());
			configureGuild(discordBot.JDA(), discordConfig, event.getGuild());
			return true;
		}

		return false;
	}

	private void configureGuild(JDA jda, DiscordConfig discordConfig, Guild guild) {
		if (Boolean.FALSE.equals(discordConfig.AUTO_CONFIGURE.value())) {
			return;
		}
		if (Boolean.TRUE.equals(discordConfig.AUTO_CONFIGURE_MULT.value()) && guild == null) {
			Seriex.get().logger().error("Auto configure (multi) countered a npe...");
			return;
		}

		discordConfig.ID_GUILD.value(guild.getId());
		/* configuration */
		String channelPrefix = "・";
		createCategoryWithTextChannelsIfItDoesntExist(guild, "01", sortedSetOf("register", "language-selection", "recovery", "settings"), channelPrefix,
					channel -> {
						String cmd = null;
						String name = channel.getName();
						if (name.contains("register")) cmd = INTERNAL_REGISTER_COMMAND;
						else if (name.contains("language")) cmd = INTERNAL_LANGUAGE_COMMAND;
						else if (name.contains("recovery")) cmd = INTERNAL_RECOVERY_COMMAND;
						else if (name.contains("settings")) cmd = INTERNAL_SETTINGS_COMMAND;

						if (INTERNAL_LANGUAGE_COMMAND.equals(cmd))
							discordConfig.ID_LANGUAGE_CHANNEL.value(channel.getId());

						if (cmd == null) Seriex.get().logger().error("Cant create channel because internal command is null!");
						else channel.sendMessage(cmd).queue();
					});
		createCategoryWithTextChannelsIfItDoesntExist(guild, "02", sortedSetOf("announcements", "commits", "updates"), channelPrefix, channel -> {});
		createCategoryWithTextChannelsIfItDoesntExist(guild, "03", createChannelNames("general-"), channelPrefix, channel -> {});
		createCategoryWithTextChannelsIfItDoesntExist(guild, "04", sortedSetOf("server-chat", "ban-logs"), channelPrefix, channel -> {
			if (channel.getName().contains("server-chat"))
				discordConfig.ID_SERVER_CHAT.value(channel.getId());
		});
		createCategoryWithVoiceChatIfItDoesntExist(guild, "05", createChannelNames("vc-"), channelPrefix, channel -> {});
		createCategoryWithTextChannelsIfItDoesntExist(guild, "2173", sortedSetOf("register-logs"), channelPrefix, channel -> {
			discordConfig.ID_REGISTER_LOGS.value(channel.getId());
			channel.getManager().putRolePermissionOverride(
						guild.getPublicRole().getIdLong(),
						Arrays.asList(EMPTY_PERMISSIONS),
						Set.of(MESSAGE_SEND, MESSAGE_HISTORY, VIEW_CHANNEL, CREATE_INSTANT_INVITE, MESSAGE_ADD_REACTION, CREATE_PUBLIC_THREADS, CREATE_PRIVATE_THREADS, VIEW_AUDIT_LOGS)).complete();
		});

	}


	private Set<String> sortedSetOf(String... elements) {
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, elements);
		return set;
	}

	private Set<String> createChannelNames(String prefix, String... additionalChannels) {
		Set<String> set = new LinkedHashSet<>();
		Arrays.stream(Language.values()).forEach((Language lang) -> set.add(prefix + lang.languageCode().toLowerCase(lang.locale())));
		Collections.addAll(set, additionalChannels);
		return set;
	}

	private void createCategoryWithTextChannelsIfItDoesntExist(Guild guild, String categoryName, Set<String> channels, String channelPrefix, Consumer<TextChannel> textConsumer) {
		List<Category> otherCategories = guild.getCategoriesByName(categoryName, true);
		if (!otherCategories.isEmpty())
			otherCategories.forEach(category -> category.delete().complete());
		Category serverCategory = guild.createCategory(categoryName).complete();
		for (String channelName : channels) {
			TextChannel textChannel = serverCategory.createTextChannel(channelPrefix + channelName).complete();
			if (textConsumer != null)
				textConsumer.accept(textChannel);
		}
	}

	private void createCategoryWithVoiceChatIfItDoesntExist(Guild guild, String categoryName, Set<String> channels, String channelPrefix, Consumer<VoiceChannel> voiceConsumer) {
		List<Category> otherCategories = guild.getCategoriesByName(categoryName, true);
		if (!otherCategories.isEmpty())
			otherCategories.forEach(category -> category.delete().complete());
		Category serverCategory = guild.createCategory(categoryName).complete();
		for (String channelName : channels) {
			VoiceChannel voiceChannel = serverCategory.createVoiceChannel(channelPrefix + channelName).complete();
			if (voiceConsumer != null)
				voiceConsumer.accept(voiceChannel);
		}
	}
}
