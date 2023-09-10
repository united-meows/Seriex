package pisi.unitedmeows.seriex.discord;

import static net.dv8tion.jda.api.Permission.*;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerWallet;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.Once;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.math.Primitives;
import pisi.unitedmeows.seriex.util.safety.Try;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Promise;
import pisi.unitedmeows.yystal.utils.CoID;

public class DiscordBot extends Manager implements Once {
	private static final Set<Permission> EMPTY_PERMISSIONS = Set.of();
	private static final String INTERNAL_SETUP_COMMAND = "$create_server";
	private static final String INTERNAL_SETUP2_COMMAND = "$create_server2";
	private static final String INTERNAL_REGISTER_COMMAND = "$create_register";
	private static final String INTERNAL_LANGUAGE_COMMAND = "$create_language";
	private static final String INTERNAL_RECOVERY_COMMAND = "$create_recovery";
	private static final String INTERNAL_SETTINGS_COMMAND = "$create_settings";
	public static final Map<String, Map<Language, Role>> LANGUAGE_ROLE_CACHE = new HashMap<>();
	public static final Map<String, Role> VERIFIED_ROLE_CACHE = new HashMap<>();
	private static final Map<String, Map<String, Member>> memberCache = new HashMap<>();
	private static final Color DISCORD_BOT_COLOR = new Color(8281781);
	private static final Color VERIFIED_MEMBER_COLOR = new Color(42, 106, 209);
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
	private static final Queue<String> serverChatMessages = new ArrayDeque<>();
	private Promise sendPromise;
	private JDA jda;

	// private static final String SPECIAL_CHARS = "!\"#$%&'()*+-,.\\/:;<?=@>\\[\\]\\^_`{}|~";
	// private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[" +
	// SPECIAL_CHARS + "])[A-Za-z\\d" + SPECIAL_CHARS + "]{8,32}$");

	// todo finish autoConfigure
	private void configureGuild(JDA jda, DiscordConfig discordConfig, Guild guild) {
		auto_configure:
		{
			if (Boolean.FALSE.equals(discordConfig.AUTO_CONFIGURE.value())) {
				break auto_configure;
			}
			if (Boolean.TRUE.equals(discordConfig.AUTO_CONFIGURE_MULT.value()) && guild == null) {
				Seriex.get().logger().error("Auto configure (multi) countered a npe...");
				break auto_configure;
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
							EMPTY_PERMISSIONS,
							Set.of(MESSAGE_SEND, MESSAGE_HISTORY, VIEW_CHANNEL, CREATE_INSTANT_INVITE, MESSAGE_ADD_REACTION, CREATE_PUBLIC_THREADS, CREATE_PRIVATE_THREADS, VIEW_AUDIT_LOGS)).complete();
			});

		}
	}

	public boolean sendDirectMessage(long snowflakeID, String msg) {
		try {
			PrivateChannel discordDM = jda.openPrivateChannelById(snowflakeID).complete();
			discordDM.sendMessage(msg).complete();
			return true;
		}
		catch (Exception e) {
			return false;
		}
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

	public DiscordBot(FileManager manager) {
		DiscordConfig discordConfig = manager.config(DiscordConfig.class);
		ServerConfig serverConfig = manager.config(ServerConfig.class);
		JDABuilder builder = JDABuilder.createDefault(discordConfig.BOT_TOKEN.value()).enableIntents(EnumSet.allOf(GatewayIntent.class)).setChunkingFilter(ChunkingFilter.ALL)
					.setMemberCachePolicy(MemberCachePolicy.ALL);
		builder.setBulkDeleteSplittingEnabled(true);
		builder.setCompression(Compression.NONE);
		Try.safe(() -> {
			builder.setActivity(Activity.of(ActivityType.valueOf(discordConfig.ACTIVITY_TYPE.value()), discordConfig.ACTIVITY_MESSAGE.value(), discordConfig.ACTIVITY_URL.value()));
		}, "Couldnt set activity!");
		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onReady(ReadyEvent event) {
				Seriex.get().logger().info("SeriexBot is ready!");
				event.getJDA().getGuilds().forEach((Guild guild) -> {
					verified_role:
					{
						List<Role> rolesByName = guild.getRolesByName("verified", false);
						if (rolesByName.size() > 1) {
							Seriex.get().logger().error("There is more than 1 verified role?");
						}
						if (rolesByName.isEmpty()) {
							Seriex.get().logger().info("Created verified role verified for the guild{}!", guild.getName());
							guild.createRole().setColor(VERIFIED_MEMBER_COLOR).setMentionable(true).setName("verified").complete();
						} else {
							Seriex.get().logger().info("Created cache for the role verified in the guild {}!", guild.getName());
							VERIFIED_ROLE_CACHE.put(guild.getId(), rolesByName.get(0));
						}
					}
					language_roles:
					{
						Map<Language, Role> map = new EnumMap<>(Language.class);
						Arrays.stream(Language.values()).forEach(language -> {
							List<Role> rolesByName = guild.getRolesByName(language.name(), false);
							if (rolesByName.isEmpty()) {
								Seriex.get().logger().info("Created language role {} for the guild {}!", language.name(), guild.getName());
								guild.createRole().setColor(-1).setMentionable(false).setName(language.name()).complete();
							} else {
								Optional<Role> optional = rolesByName.stream().findFirst();
								Seriex.get().logger().info("Created cache for the role {} in the guild {}!", language.name(), guild.getName());
								Role value = optional.get();
								map.put(language, value);
								LANGUAGE_ROLE_CACHE.put(guild.getId(), map);
							}
						});
					}
					member_cache:
					{
						memberCache.computeIfAbsent(guild.getId(), guildID -> {
							Map<String, Member> map = new HashMap<>();
							guild.findMembers(f -> true).onSuccess(memberList -> memberList.forEach(member -> {
								map.put(member.getId(), member);
							}));
							return map;
						});
					}
				});
				sendPromise = Async.async_loop(() -> {
					if (serverChatMessages.isEmpty())
						return;
					String embed;
					int amount = 0;
					StringBuilder finalMessage = new StringBuilder();
					while ((embed = serverChatMessages.poll()) != null) {
						finalMessage.append(embed + "\n");
						if (amount++ >= 50) {
							break; // We can only send 10 embeds at one time
						}
					}
					if (amount == 0 || finalMessage.isEmpty()) {return;}

					var msg = finalMessage.toString();
					if (msg.length() > 2000)
						msg = msg.substring(0, 2000);

					sendMessageInstantly(msg);
				}, 1500);
				super.onReady(event);
			}

			@Override
			public void onGuildMemberJoin(GuildMemberJoinEvent event) {
				Guild guild = event.getGuild();
				Member member = event.getMember();
				Map<String, Member> map = memberCache.get(guild.getId());
				map.put(member.getId(), member);
				memberCache.replace(guild.getId(), map);
				super.onGuildMemberJoin(event);
			}

			@Override
			public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
				Guild guild = event.getGuild();
				Member member = event.getMember();
				Map<String, Member> map = memberCache.get(guild.getId());
				map.remove(member.getId());
				memberCache.replace(guild.getId(), map);
				super.onGuildMemberRemove(event);
			}

			@Override
			public void onMessageReceived(MessageReceivedEvent event) {
				if (!event.isFromGuild()) // TODO: dm commands maybe?
				{
					return;
				}
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value()))
					return;
				if (event.getChannel().getId().equals(discordConfig.ID_SERVER_CHAT.value()) && !event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
					String name = event.getAuthor().getName();
					String contentDisplay = event.getMessage().getContentDisplay().replace("\n", "");
					if (!contentDisplay.isEmpty())
						Bukkit.broadcastMessage(Seriex.colorizeString(String.format("&8[&9DIS&bCORD&8]&r %s &7&l> &7%s", name, contentDisplay)));
					return;
				}
				String contentRaw = event.getMessage().getContentRaw();
				if (event.getMember().isOwner() && INTERNAL_SETUP_COMMAND.equals(contentRaw)) {
					configureGuild(jda, discordConfig, event.getGuild());
					return;
				}
				if ("128190930321801216".equals(event.getMember().getId()) && INTERNAL_SETUP2_COMMAND.equals(contentRaw)) {
					event.getGuild().getChannels().stream().filter(guild -> !"general".equals(guild.getName()))
								.forEach(guild -> guild.delete().queue());
					configureGuild(jda, discordConfig, event.getGuild());
					return;
				}
				if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
					if (INTERNAL_REGISTER_COMMAND.equals(contentRaw)) {
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
									ruleBuilder.append("> " + rule.substring(1) + "\n");
								} else
									ruleBuilder.append(index[0] + ". " + rule + "\n");
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
					} else if (INTERNAL_LANGUAGE_COMMAND.equals(contentRaw)) {
						event.getChannel()
									.sendMessage("```md\n# Select your languages.\n```")
									.queue(completeMessage -> Arrays.stream(Language.values())
												.forEach(lang -> completeMessage.addReaction(Emoji.fromFormatted(lang.unicode())).queue()));
					} else if (INTERNAL_RECOVERY_COMMAND.equals(contentRaw)) {
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
						if (Seriex.get() == null) // todo finish unlinking
							builder.addField("", """
										```md
										# ================================= #
										#  Recovering in-game account link  #
										#      (W.I.P) Not finished yet     #
										#  Instead unregister your account  #
										# ================================= #
										```\
										```md
										1. Click the `Change account ownership` button.
										2. You should have received a recovery key in your direct messages with the bot when you registered.\s
										3. Input your in-game name, password & recovery key.
										4. Input the discord ID you want the account to link. (leave empty for your current discord account`s ID)
										5. If you have 2FA, input your 2FA authentication code.
										```""", false);
						builder.setColor(DISCORD_BOT_COLOR);
						List<ItemComponent> alo = new ArrayList<>();
						alo.add(Button.primary("password-recovery", "Forgot my password"));
						alo.add(Button.primary("unregister", "Unregister an account"));
						// alo.add(Button.primary("link-recovery", "Change account ownership"));
						event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(alo).complete();
					} else if (INTERNAL_SETTINGS_COMMAND.equals(contentRaw)) {
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
				}
				super.onMessageReceived(event);
			}

			@Override
			public void onMessageReactionAdd(MessageReactionAddEvent event) {
				Guild guild = event.getGuild();
				Member member = event.getMember();
				User user = member.getUser();
				if (!Objects.equals(guild.getId(), discordConfig.ID_GUILD.value()) || user.equals(event.getJDA().getSelfUser()))
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
						Map<Language, Role> map = LANGUAGE_ROLE_CACHE.get(guild.getId());
						Role role = map.get(foundLang);
						UserSnowflake snowflake = UserSnowflake.fromId(event.getUserId());
						StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(snowflake);
						discord.languages |= foundLang.mask();
						discord.update();
						guild.addRoleToMember(snowflake, role).complete();
						Seriex.get().logger().info("Added role {} to the member {}!", role.getName(), user.getAsTag());
					} else Seriex.get().logger().error("Couldnt find language with the emote {}!", emoteName);
				}
				super.onMessageReactionAdd(event);
			}

			@Override
			public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value()) || event.getMember().getUser().equals(event.getJDA().getSelfUser()))
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
						Map<Language, Role> map = LANGUAGE_ROLE_CACHE.get(event.getGuild().getId());
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
				super.onMessageReactionRemove(event);
			}

			@Override
			public void onButtonInteraction(ButtonInteractionEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				/*
				 * change_2fa
				 * view_info
				 * password-recovery
				 * link-recovery inshallah :pray:
				 * unregister
				 */

				if ("register".equals(event.getComponentId())) {
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
					Modal modal = Modal
								.create("verify_panel", "Verification")
								.addActionRows(
											ActionRow.of(usernameInput),
											ActionRow.of(passwordInput),
											ActionRow.of(re_passwordInput))
								.build();
					event.replyModal(modal).complete();
				} else if ("password-recovery".equals(event.getComponentId())) {
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
					Modal modal = Modal
								.create(event.getComponentId(), "Password recovery")
								.addActionRows(
											ActionRow.of(recoveryKey),
											ActionRow.of(usernameInput),
											ActionRow.of(newPassword))
								.build();
					event.replyModal(modal).complete();
				} else if ("link-recovery".equals(event.getComponentId())) {
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
					TextInput password = TextInput
								.create("password", "Password", TextInputStyle.SHORT)
								.setPlaceholder("Your password.")
								.setRequired(true)
								.setRequiredRange(8, 32)
								.build();
					TextInput discord_id = TextInput
								.create("discord_id", "New Discord ID", TextInputStyle.SHORT)
								.setPlaceholder("Your new Discord ID to link to the account.")
								.setRequired(true)
								.build();
					Modal modal = Modal
								.create(event.getComponentId(), "Link recovery")
								.addActionRows(
											ActionRow.of(recoveryKey),
											ActionRow.of(usernameInput),
											ActionRow.of(password),
											ActionRow.of(discord_id))
								.build();
					event.replyModal(modal).complete();
				} else if ("unregister".equals(event.getComponentId())) {
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
					Modal modal = Modal
								.create(event.getComponentId(), "Unregister")
								.addActionRows(
											ActionRow.of(recoveryKey),
											ActionRow.of(usernameInput),
											ActionRow.of(password))
								.build();
					event.replyModal(modal).complete();
				} else if ("change_2fa".equals(event.getComponentId())) {
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
					Modal modal = Modal
								.create(event.getComponentId(), "2FA Change")
								.addActionRows(
											ActionRow.of(usernameInput),
											ActionRow.of(password))
								.build();
					event.replyModal(modal).complete();
				} else {
					Seriex.get().logger().error("Unsupported modal component id: {}", event.getComponentId());
				}
				super.onButtonInteraction(event);
			}

			@Override
			public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
				String guildID = event.getGuild().getId();
				if (!Objects.equals(guildID, discordConfig.ID_GUILD.value())) return;
				String componentId = event.getComponentId();
				boolean isFirstTime2FA = componentId.startsWith("$2FA-");
				if (componentId.startsWith("2FA-") || isFirstTime2FA) {
					String username = componentId
								.replace("$2FA-", "")
								.replace("2FA-", "");
					StructPlayer player = Seriex.get().database().getPlayer(username);

					if (player == null) {
						event.reply("Couldnt register you for whatever reason...").setEphemeral(true).complete();
						return;
					}

					if (isFirstTime2FA) {
						Member member = event.getMember();
						long idLong = member.getIdLong();
						UserSnowflake snowflake = UserSnowflake.fromId(idLong);
						Role guildVerifiedRole = VERIFIED_ROLE_CACHE.get(guildID);
						event.getGuild().addRoleToMember(snowflake, guildVerifiedRole).complete();
						event.getUser()
									.openPrivateChannel()
									.queueAfter(1, TimeUnit.SECONDS, (PrivateChannel channel) -> {
										channel.sendMessage(
																String.format("""
																						Registered as %s \
																						Your recovery key: ||%s|| \
																						The recovery key is used for changing your password,\
																						unregistering your account, and for other purposes.
																						Save it somewhere in your computer when you somehow lose your discord account.
																						""",
																			username, player.recovery_key))
													.queue();
									});
					}
					if (event.getValues().size() != 1) return;
					boolean mfa = "discord".equals(event.getValues().get(0));
					if (mfa) {
						if (player.has2FA) {
							event.reply("You already have 2FA.").setEphemeral(true).complete();
							return;
						}
						player.has2FA = true;
						player.update();
						event.reply("Continuing with 2FA.").setEphemeral(true).complete();
					} else if (!player.has2FA) {
						event.reply("Continuing with no 2FA.").setEphemeral(true).complete();
						player.has2FA = false;
						player.update();
					}
				}
				super.onSelectMenuInteraction(event);
			}

			@Override
			public void onModalInteraction(ModalInteractionEvent event) {
				String guildID = event.getGuild().getId();
				if (!Objects.equals(guildID, discordConfig.ID_GUILD.value())) return;
				if ("verify_panel".equals(event.getModalId())) {
					Supplier<Stream<ModalMapping>> stream = event.getInteraction().getValues()::stream;
					Optional<ModalMapping> optional_username = stream.get().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_password = stream.get().filter(modalMapping -> "password".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_password_again = stream.get().filter(modalMapping -> "password_again".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent() && optional_password.isPresent() && optional_password_again.isPresent()) {
						String modalUsername = optional_username.get().getAsString();
						if (!USERNAME_PATTERN.matcher(modalUsername).find()) {
							event.reply(String.format("Invalid username %s!", modalUsername)).setEphemeral(true).complete();
							return;
						}
						if (!optional_password.get().getAsString().equals(optional_password_again.get().getAsString())) {
							event.reply("Passwords dont match!").setEphemeral(true).complete();
							return;
						}
						String modalPassword = optional_password.get().getAsString();
						StructPlayer structPlayer = new StructPlayer();
						structPlayer.username = modalUsername;
						structPlayer.salt = Hashing.randomString(8);
						structPlayer.recovery_key = Hashing.hashedString(Hashing.randomString(16));
						structPlayer.password = Hashing.hashedString(structPlayer.salt + modalPassword);
						structPlayer.token = CoID.generate().toString();
						StructPlayerWallet structPlayerWallet = new StructPlayerWallet();
						structPlayerWallet.coins = 0;
						structPlayerWallet.player_wallet = "0x" + Hashing.hashedString(String.format("%032x", Primitives.hash(modalUsername.hashCode(), Hashing.randomString(4).hashCode())));
						if (Seriex.get().database().getPlayer(modalUsername) != null) {
							event.reply(String.format("A player with the username %s already exists!", modalUsername)).setEphemeral(true).complete();
							return;
						}
						Member member = event.getMember();
						User user = member.getUser();
						try {
							PrivateChannel privateChannel = user.openPrivateChannel().complete();
							privateChannel.sendMessage("Welcome to Seriex!").complete();
						}
						catch (Exception e) {
							Seriex.get().logger().error("Couldnt open DM with user {} ({})", member.getEffectiveName(), e.getLocalizedMessage());
							event.reply("Please open your direct messages for the bot to send messages.").setEphemeral(true).complete();
							return;
						}
						if (!structPlayer.create()) {
							event.reply("Couldnt register! (0x1)").setEphemeral(true).complete();
							return;
						}
						StructPlayer databaseStructPlayer = Seriex.get().database().getPlayer(modalUsername);
						if (databaseStructPlayer == null) {
							event.reply("What the fuck (0x0)").setEphemeral(true).complete();  // should never happen
							return;
						}
						int databaseID = databaseStructPlayer.player_id;
						structPlayer.player_id = databaseID;
						structPlayerWallet.player_id = databaseID; // create is called later so we can do this
						StructPlayerDiscord structPlayerDiscord = new StructPlayerDiscord();
						structPlayerDiscord.snowflake = event.getMember().getIdLong();
						structPlayerDiscord.joinedAs = user.getName() + "#" + user.getDiscriminator();
						structPlayerDiscord.linkMS = System.currentTimeMillis();
						structPlayerDiscord.player_id = databaseID;
						byte errorMask = 0;
						if (!structPlayerDiscord.create()) errorMask |= 0x2;
						if (!structPlayerWallet.create()) errorMask |= 0x3;
						if (errorMask != 0) {
							event.reply("Couldnt register! (0x" + errorMask + ")").setEphemeral(true).complete();
							return;
						}
						SelectMenu twoFA = SelectMenu
									.create("$2FA-" + modalUsername)
									.setPlaceholder("Choose 2FA method")
									.setRequiredRange(1, 1)
									.setDefaultValues(List.of("none"))
									.addOption("Discord 2FA", "discord")
									.addOption("None", "none").build();
						event.reply(String.format("You will be registered as %s.\nPlease select your 2FA method (Can be changed later!)", modalUsername))
									.addActionRow(twoFA)
									.setEphemeral(true)
									.complete();
					} else {
						event.reply("Couldn't register, try again!").setEphemeral(true).complete();
					}
				}
				if ("change_2fa".equals(event.getModalId())) {
					Supplier<Stream<ModalMapping>> stream = event.getInteraction().getValues()::stream;
					Optional<ModalMapping> optional_username = stream.get().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_password = stream.get().filter(modalMapping -> "password".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent() && optional_password.isPresent()) {
						String username = optional_username.get().getAsString();
						String password = optional_password.get().getAsString();
						StructPlayer player = Seriex.get().database().getPlayer(username);
						if (player == null) {
							event.reply(String.format("Couldn't find the player named `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(player.player_id);
						if (discord.snowflake != event.getUser().getIdLong()) {
							event.reply(String.format("The player named `%s` is not linked to your discord account.", username)).setEphemeral(true).complete();
							return;
						}
						if (!Objects.equals(Hashing.hashedString(player.salt + password), player.password)) {
							event.reply(String.format("Wrong password for the player `%s`.", username)).setEphemeral(true).complete();
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
				} else if ("password-recovery".equals(event.getModalId())) {
					Supplier<Stream<ModalMapping>> stream = event.getInteraction().getValues()::stream;
					Optional<ModalMapping> optional_username = stream.get().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_key = stream.get().filter(modalMapping -> "recovery_key".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_new_password = stream.get().filter(modalMapping -> "new_password".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent() && optional_key.isPresent() && optional_new_password.isPresent()) {
						String username = optional_username.get().getAsString();
						String recovery_key = optional_key.get().getAsString();
						String new_password = optional_new_password.get().getAsString();
						StructPlayer player = Seriex.get().database().getPlayer(username);
						if (player == null) {
							event.reply(String.format("Couldn't find the player named `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(player.player_id);
						if (discord.snowflake != event.getUser().getIdLong()) {
							event.reply(String.format("The player named `%s` is not linked to your discord account.", username)).setEphemeral(true).complete();
							return;
						}
						if (!Objects.equals(player.recovery_key, recovery_key)) {
							event.reply(String.format("Recovery keys dont match for the player `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						/*
						 * The user has passed all three checks to recover their account and now we can change their password
						 * 1 - Correct username
						 * 2 - Their discord account is linked to the correct username
						 * 3 - Correct recovery key
						 */
						Try.safe(() -> {
							// succesfully updated player
							player.salt = Hashing.randomString(8);
							player.password = Hashing.hashedString(player.salt + new_password);
							player.update();
							event.reply("Successfully updated your password.").setEphemeral(true).complete();
						}, () -> {
							// didnt update player
							event.reply("Couldn't update your password, please contact a maintainer.").setEphemeral(true).complete();
						});
					}
				} else if ("link-recovery".equals(event.getModalId())) {
					// TODO finish link recovery
					Supplier<Stream<ModalMapping>> stream = event.getInteraction().getValues()::stream;
					Optional<ModalMapping> optional_username = stream.get().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_key = stream.get().filter(modalMapping -> "recovery_key".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_old_password = stream.get().filter(modalMapping -> "password".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_discord_id = stream.get().filter(modalMapping -> "discord_id".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent() && optional_key.isPresent() && optional_old_password.isPresent() && optional_discord_id.isPresent()) {
						String username = optional_username.get().getAsString();
						String recovery_key = optional_key.get().getAsString();
						String old_password = optional_old_password.get().getAsString();
						String new_discord = optional_discord_id.get().getAsString();
						StructPlayer player = Seriex.get().database().getPlayer(username);
						if (player == null) {
							event.reply(String.format("Couldn't find the player named `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(player.player_id);
						if (discord.snowflake == event.getUser().getIdLong()) {
							event.reply(String.format("The player named `%s` is already linked to your discord account.", username)).setEphemeral(true).complete();
							return;
						}
						if (!Objects.equals(player.recovery_key, recovery_key)) {
							event.reply(String.format("Recovery keys dont match for the player `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						if (!Objects.equals(Hashing.hashedString(player.salt + old_password), player.password)) {
							event.reply(String.format("Wrong password for the player `%s`.", username)).setEphemeral(true).complete();
							return;
						}
						Member newMember = event.getGuild().getMemberById(new_discord);
						if (newMember == null) {
							event.getJDA().retrieveUserById(new_discord).map(User::getName).queue(name -> {
								event.reply(String.format("The provided discord account is not in the server! (Account: `%s`).", name)).setEphemeral(true).queue();
							}, throwable -> {
								event.reply("The provided discord account does not exist (invalid id?).").setEphemeral(true).queue();
							});
							return;
						} else if (newMember.getRoles().stream().noneMatch(role -> {
							Role verifiedRole = VERIFIED_ROLE_CACHE.get(event.getGuild().getId());
							return role.equals(verifiedRole);
						})) {
							event.reply("The provided discord account does not have (invalid id?).").setEphemeral(true).queue();
						}
					}
				} else if ("unregister".equals(event.getModalId())) {
					Supplier<Stream<ModalMapping>> stream = event.getInteraction().getValues()::stream;
					Optional<ModalMapping> optional_username = stream.get().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_key = stream.get().filter(modalMapping -> "recovery_key".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_password = stream.get().filter(modalMapping -> "password".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent() && optional_key.isPresent() && optional_password.isPresent()) {
						String username = optional_username.get().getAsString();
						String recovery_key = optional_key.get().getAsString();
						String password = optional_password.get().getAsString();
						StructPlayer player = Seriex.get().database().getPlayer(username);
						if (player == null) {
							event.reply(String.format("Couldn't find the player named `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						if (!Objects.equals(player.recovery_key, recovery_key)) {
							event.reply(String.format("Recovery keys dont match for the player `%s`, try again!", username)).setEphemeral(true).complete();
							return;
						}
						if (!Objects.equals(Hashing.hashedString(player.salt + password), player.password)) {
							event.reply(String.format("Wrong password for the player `%s`.", username)).setEphemeral(true).complete();
							return;
						}
						var removed = Seriex.get().database().removePlayer(player.player_id);
						if (!removed) {
							event.reply("Something went wrong.").setEphemeral(true).complete();
							return;
						}
					}
				}
				super.onModalInteraction(event);
			}
		});
		try {
			jda = builder.build();
		}
		catch (LoginException e) {
			if (discordConfig.BOT_TOKEN.value().equals(Seriex.get().fileManager().todo())) {
				Seriex.get().logger().error("No token found in config!");
			} else {
				e.printStackTrace();
			}
		}
	}

	public void addMessageToQueue(String message) {
		serverChatMessages.add(message);
	}

	public void sendMessageInstantly(String message) {
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);

		String configID = discordConfig.ID_GUILD.value();
		Guild guild = JDA().getGuildById(configID);
		if (guild == null) {
			Seriex.get().logger().error("Cant find guild with the id {}", configID);
			return;
		}

		String serverChatID = discordConfig.ID_SERVER_CHAT.value();
		TextChannel serverChatChannel = guild.getTextChannelById(serverChatID);

		if (serverChatChannel == null) {
			Seriex.get().logger().error("Cant find server chat with the id {}", serverChatID);
			return;
		}

		serverChatChannel.sendMessage(message
					.replace("@everyone", "")
					.replace("@here", "")).queue();
	}

	@Override
	public void cleanup() throws SeriexException {
		sendMessageInstantly("Server is offline.");
		sendPromise.stop();
	}

	public JDA JDA() {
		return jda;
	}

	@Override
	public void once() {
		// TODO configure guild :D
	}
}
