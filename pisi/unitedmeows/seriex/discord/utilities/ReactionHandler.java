package pisi.unitedmeows.seriex.discord.utilities;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.safety.Try;

import java.util.Map;
import java.util.Objects;

public class ReactionHandler {
	private ReactionHandler() {

	}

	public static ReactionHandler create() {
		return new ReactionHandler();
	}

	private boolean isUnappropiateGuild(Guild guild, boolean selfUser) {
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);

		if (!Objects.equals(guild.getId(), discordConfig.ID_GUILD.value()) || selfUser)
			return true;

		return false;
	}

	public void onReactionAdded(MessageReactionAddEvent event, DiscordCache cache) {
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);

		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();

		if(isUnappropiateGuild(guild, user.equals(event.getJDA().getSelfUser())))
			return;

		if (Objects.equals(event.getChannel().getId(), guild.getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId())) {
			String emoteName = event.getReaction().getEmoji().getName();
			Language foundLang = null;
			Language[] values = Language.values();
			for (Language language : values) {
				if (Objects.equals(language.unicode(), emoteName)) {
					foundLang = language;
					break;
				}
			}
			if (foundLang != null) {
				Map<Language, Role> map = cache.languageRoles().get(guild.getId());
				Role role = map.get(foundLang);
				UserSnowflake snowflake = UserSnowflake.fromId(event.getUserId());
				StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(snowflake);
				discord.languages |= foundLang.mask();
				discord.update();
				guild.addRoleToMember(snowflake, role).complete();
				Seriex.get().logger().info("Added role {} to the member {}!", role.getName(), user.getAsTag());
			} else Seriex.get().logger().error("Couldnt find language with the emote {}!", emoteName);
		}
	}

	public void onReactionRemoved(MessageReactionRemoveEvent event, DiscordCache cache) {
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);

		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();

		if(isUnappropiateGuild(guild, user.equals(event.getJDA().getSelfUser())))
			return;

		if (Objects.equals(event.getChannel().getId(), event.getGuild().getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId())) {
			String emoteName = event.getReaction().getEmoji().getName();
			Language foundLang = null;
			Language[] values = Language.values();
			for (Language language : values) {
				if (Objects.equals(language.unicode(), emoteName)) {
					foundLang = language;
					break;
				}
			}
			if (foundLang != null) {
				Map<Language, Role> map = cache.languageRoles().get(event.getGuild().getId());
				Role role = map.get(foundLang);
				String userId = event.getUserId();
				UserSnowflake fromId = UserSnowflake.fromId(userId);
				StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(fromId);
				/*
				 * 0b1 0000 0001
				 * 0b1 1111 1110 / ~0b1 0000 0001
				 * -------------- &
				 * 0b0 0000 0000
				 */
				discord.languages &= ~foundLang.mask();
				discord.update();
				event.getGuild().removeRoleFromMember(fromId, role).complete();
				Try.safe(() -> Seriex.get().logger().info("Removed role {} from the member {}!", role.getName(), event.getMember().getUser().getAsTag()),
							/* on exception */
							() -> Seriex.get().logger().info("Removed role {} from the member {} (id)!", role.getName(), userId));
			} else Seriex.get().logger().error("Couldnt find language with the emote {}!", emoteName);
		}
	}
}
