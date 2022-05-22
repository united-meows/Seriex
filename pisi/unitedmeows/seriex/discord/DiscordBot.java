package pisi.unitedmeows.seriex.discord;

import java.awt.Color;
import java.util.*;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Languages;

public class DiscordBot {
	private static final Color DISCORD_BOT_COLOR = new Color(8281781);
	private static final Color VERIFIED_MEMBER_COLOR = new Color(42, 106, 209);
	private static final Map<String, Map<Languages, Role>> roleCache = new HashMap<>();
	private static final Map<String, Role> verifiedRole = new HashMap<>();

	public static void main(String[] args) throws LoginException {
		JDABuilder builder = JDABuilder.createDefault(System.getProperty("token"));
		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
		builder.setBulkDeleteSplittingEnabled(false);
		builder.setCompression(Compression.NONE);
		builder.setActivity(Activity.watching("merhaba!"));
		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onReady(ReadyEvent event) {
				Seriex.logger().debug("SeriexBot is ready!");
				event.getJDA().getGuilds().forEach((Guild guild) -> {
					Map<Languages, Role> map = new HashMap<>();
					verified_role: {
						List<Role> verified = guild.getRolesByName("verified", false);
						if (verified == null || verified.isEmpty()) {
							Seriex.logger().info("Created verified role verified for the guild %s!", guild.getName());
							guild.createRole().setColor(VERIFIED_MEMBER_COLOR).setMentionable(true).setName("verified").queue();
						} else {
							Seriex.logger().info("Created cache for the role verified in the guild %s!", guild.getName());
							roleCache.put(guild.getId(), map);
						}
					}
					language_roles: {
						Arrays.stream(Languages.values()).forEach(language -> {
							List<Role> rolesByName = guild.getRolesByName(language.name(), false);
							if (rolesByName == null || rolesByName.isEmpty()) {
								Seriex.logger().info("Created language role %s for the guild %s!", language.name(), guild.getName());
								guild.createRole().setColor(-1).setMentionable(false).setName(language.name()).queue();
							} else {
								Seriex.logger().info("Created cache for the role %s in the guild %s!", language.name(), guild.getName());
								Role value = rolesByName.stream().findFirst().get();
								map.put(language, value);
								roleCache.put(guild.getId(), map);
							}
						});
					}
				});
				super.onReady(event);
			}

			@Override
			public void onMessageReceived(MessageReceivedEvent event) {
				if ("$create_register".equals(event.getMessage().getContentRaw())) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setImage("https://cdn.discordapp.com/attachments/977943419366289410/977956507633221722/efsane_banner.png?size=4096");
					builder.addField("> ```NOTICE```", "By registering, you accept " + "__**our TOS, Discord`s TOS and our rules**__.", true);
					StringBuilder ruleBuilder = new StringBuilder();
					// @DISABLE_FORMATTING
					String[] rules = {
								"Be respectful.",
								"No spamming.",
								"No doxing/ddosing other users.",
								"",
								"These rules might change at any time without notice.",
								"It is your responsibility to check for them."
					};
					// @ENABLE_FORMATTING
					Arrays.stream(rules).forEach(rule -> {
						boolean last = Objects.equals(rule, rules[rules.length - 1]);
						ruleBuilder.append("> " + rule + (last ? "" : "\n"));
					});
					builder.addField("> ```RULES```", ruleBuilder.toString(), false);
					builder.addField("",
								String.format("After registering please head to <#%s> to select your language(s))!", event.getGuild().getTextChannelsByName("language-selection", false).get(0).getId()),
								false);
					builder.setColor(DISCORD_BOT_COLOR);
					builder.setTitle("Welcome to Seriex!");
					List<ItemComponent> alo = new ArrayList<>();
					alo.add(Button.primary("register", "Click to register!"));
					alo.add(Button.link("https://seriex.software/tos.txt", "Seriex TOS"));
					alo.add(Button.link("https://discord.com/terms", "Discord TOS"));
					event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(alo).queue();
				}
				if ("$create_language".equals(event.getMessage().getContentRaw())) {
					event.getChannel().sendMessage("> Select your language(s)").queue(completeMessage -> {
						Arrays.stream(Languages.values()).forEach(lang -> completeMessage.addReaction(lang.unicode()).queue());
					});
				}
				super.onMessageReceived(event);
			}

			@Override
			public void onMessageReactionAdd(MessageReactionAddEvent event) {
				if (Objects.equals(event.getChannel().getId(), event.getGuild().getTextChannelsByName("language-selection", false).get(0).getId())) {
					String emoteName = event.getReactionEmote().getName();
					Languages foundLang = null;
					for (Languages language : Languages.values()) {
						if (Objects.equals(language.unicode(), emoteName)) {
							foundLang = language;
							break;
						}
					}
					if (foundLang != null) {
						Map<Languages, Role> map = roleCache.get(event.getGuild().getId());
						Role role = map.get(foundLang);
						event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUserId()), role).queue();
						Seriex.logger().info("Added role %s to the member %s!", role.getName(), event.getMember().getUser().getAsTag());
					}
				}
				super.onMessageReactionAdd(event);
			}

			@Override
			public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
				if (Objects.equals(event.getChannel().getId(), event.getGuild().getTextChannelsByName("language-selection", false).get(0).getId())) {
					String emoteName = event.getReactionEmote().getName();
					Languages foundLang = null;
					for (Languages language : Languages.values()) {
						if (Objects.equals(language.unicode(), emoteName)) {
							foundLang = language;
							break;
						}
					}
					if (foundLang != null) {
						Map<Languages, Role> map = roleCache.get(event.getGuild().getId());
						Role role = map.get(foundLang);
						String userId = event.getUserId();
						UserSnowflake fromId = UserSnowflake.fromId(userId);
						event.getGuild().removeRoleFromMember(fromId, role).queue();
						try {
							Seriex.logger().info("Removed role %s from the member %s!", role.getName(), event.getMember().getUser().getAsTag());
						}
						catch (Exception e) {
							Seriex.logger().info("Removed role %s from the member %s (id)!", role.getName(), userId);
						}
					}
				}
				super.onMessageReactionRemove(event);
			}

			@Override
			public void onButtonInteraction(ButtonInteractionEvent event) {
				if ("register".equals(event.getComponentId())) {
					// @DISABLE_FORMATTING
					TextInput subject = TextInput
								.create("username", "Username", TextInputStyle.SHORT)
								.setPlaceholder("Your in-game username.")
								.setRequiredRange(3, 16)
								.build();
					TextInput body =
								TextInput
								.create("password", "Password", TextInputStyle.SHORT)
								.setPlaceholder("Your password. (At least 8, maximum of 16 characters.)")
								.setMinLength(8)
								.setMaxLength(16).build();
					Modal modal = Modal
								.create("verify_panel", "Verification")
								.addActionRows(ActionRow.of(subject), ActionRow.of(body))
								.build();
					event.replyModal(modal).queue();
					String userId = event.getMember().getId();
					event.getGuild().addRoleToMember(UserSnowflake.fromId(userId),
								verifiedRole.get(event.getGuild().getId()));
					// @ENABLE_FORMATTING
				}
				super.onButtonInteraction(event);
			}

			@Override
			public void onModalInteraction(ModalInteractionEvent event) {
				if ("verify_panel".equals(event.getModalId())) {
					Optional<ModalMapping> optional_username = event.getInteraction().getValues().stream().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent()) {
						String username = optional_username.get().getAsString();
						event.reply(String.format("Registered as %s!", username)).setEphemeral(true).queue();
						// max ghost zekası
						event.getGuild().getTextChannelsByName("register_logs", false).get(0).sendMessage(
									String.format("%s#%s (%s) registered as %s", event.getMember().getEffectiveName(), event.getMember().getUser().getDiscriminator(), event.getMember().getId(), username))
									.queue();
					} else {
						event.reply("Couldn`t register, try again!").setEphemeral(true).queue();
					}
				}
				super.onModalInteraction(event);
			}
		});
		builder.build();
	}

	public DiscordBot(String token) {}

	public void onEnable() {}
}
