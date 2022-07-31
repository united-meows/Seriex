package pisi.unitedmeows.seriex.discord;

import static pisi.unitedmeows.seriex.Seriex.logger;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.ReadyEvent;
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
import pisi.unitedmeows.seriex.auth.gauth.GAuth;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerWallet;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.Once;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.math.Primitives;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Promise;

public class DiscordBot extends Manager implements Once {
	public static final Map<String, Map<Language, Role>> roleCache = new HashMap<>();
	public static final Map<String, Role> verifiedRole = new HashMap<>();
	private static final Color DISCORD_BOT_COLOR = new Color(8281781);
	private static final Color VERIFIED_MEMBER_COLOR = new Color(42, 106, 209);
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
	private static final Queue<MessageEmbed> serverChatMessages = new ArrayDeque<>();
	private JDA jda;
	private Promise sendPromise;

	private void configureGuild(JDA jda, DiscordConfig discordConfig, Guild guild) {
		// todo finish
		auto_configure: {
			if (!discordConfig.AUTO_CONFIGURE.value()) {
				break auto_configure;
			}
			if (discordConfig.AUTO_CONFIGURE_MULT.value() && guild == null) {
				Seriex.logger().fatal("Auto configure (multi) countered a npe...");
				break auto_configure;
			}
			String guildID = discordConfig.ID_GUILD.value();
			if (guildID == null || guildID.isEmpty()) {
				Seriex.logger().fatal("Discord Guild ID is empty!");
				break auto_configure;
			}
			if ("999999999999999999".length() < guildID.length()) {
				Seriex.logger().fatal("Discord Guild ID has an invalid length!");
				break auto_configure;
			}
			Guild guildById = jda.getGuildById(guildID);
			guildById.createCategory("server").complete();
			guildById.createTextChannel("language-selection").complete();
			guildById.createTextChannel("verify").complete();
			guildById.createTextChannel("register-logs").complete();
			guildById.createTextChannel("server-chat").complete();
			Arrays.stream(Language.values()).forEach(lang -> {
				guildById.createTextChannel("general-" + lang.languageCode().toLowerCase(lang.locale())).complete();
			});
		}
	}

	public DiscordBot(FileManager manager) {
		DiscordConfig discordConfig = (DiscordConfig) manager.getConfig(manager.DISCORD);
		ServerConfig serverConfig = (ServerConfig) manager.getConfig(manager.SERVER);
		JDABuilder builder = JDABuilder.createDefault(discordConfig.BOT_TOKEN.value()).enableIntents(EnumSet.allOf(GatewayIntent.class)).setChunkingFilter(ChunkingFilter.ALL)
					.setMemberCachePolicy(MemberCachePolicy.ALL);
		builder.setBulkDeleteSplittingEnabled(true);
		builder.setCompression(Compression.NONE);
		try {
			builder.setActivity(Activity.of(discordConfig.ACTIVITY_TYPE.value(), discordConfig.ACTIVITY_MESSAGE.value(), discordConfig.ACTIVITY_URL.value()));
		}
		catch (Exception e) {
			Seriex.logger().fatal("Couldnt set activity!");
		}
		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onReady(ReadyEvent event) {
				logger().debug("SeriexBot is ready!");
				event.getJDA().getGuilds().forEach((Guild guild) -> {
					Map<Language, Role> map = new EnumMap<>(Language.class);
					verified_role: {
						List<Role> rolesByName = guild.getRolesByName("verified", false);
						if (rolesByName.size() > 1) {
							logger().fatal("There is more than 1 verified roles?");
						}
						Role verified = rolesByName.get(0);
						if (verified == null) {
							logger().info("Created verified role verified for the guild %s!", guild.getName());
							guild.createRole().setColor(VERIFIED_MEMBER_COLOR).setMentionable(true).setName("verified").queue();
						} else {
							logger().info("Created cache for the role verified in the guild %s!", guild.getName());
							verifiedRole.put(guild.getId(), verified);
						}
					}
					language_roles: {
						Arrays.stream(Language.values()).forEach(language -> {
							List<Role> rolesByName = guild.getRolesByName(language.name(), false);
							if (rolesByName == null || rolesByName.isEmpty()) {
								logger().info("Created language role %s for the guild %s!", language.name(), guild.getName());
								guild.createRole().setColor(-1).setMentionable(false).setName(language.name()).queue();
							} else {
								Optional<Role> optional = rolesByName.stream().findFirst();
								if (optional.isPresent()) {
									logger().info("Created cache for the role %s in the guild %s!", language.name(), guild.getName());
									Role value = optional.get();
									map.put(language, value);
									roleCache.put(guild.getId(), map);
								} else {
									logger().info("Couldn't create cache for the role %s in the guild %s!", language.name(), guild.getName());
								}
							}
						});
					}
					auto_configure: {}
				});
				sendPromise = Async.async_loop(() -> {
					if (serverChatMessages.isEmpty()) return;
					MessageEmbed embed = serverChatMessages.poll();
					if (embed == null) return;
					String configID = discordConfig.ID_GUILD.value();
					Guild guild = event.getJDA().getGuildById(configID);
					String serverChatID = discordConfig.ID_SERVER_CHAT.value();
					if (guild != null) {
						TextChannel textChannelById = guild.getTextChannelById(serverChatID);
						if (textChannelById != null) {
							textChannelById.sendMessageEmbeds(embed);
						} else {
							Seriex.logger().fatal("Cant find server chat with the id %s", serverChatID);
						}
					} else {
						Seriex.logger().fatal("Cant find guild with the id %s", configID);
					}
				}, 1000);
				super.onReady(event);
			}

			@Override
			public void onMessageReceived(MessageReceivedEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				if ("$create_register".equals(event.getMessage().getContentRaw())) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setImage(discordConfig.BANNER.value());
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
								String.format("After registering please head to <#%s> to select your language(s))!", event.getGuild().getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId()),
								false);
					builder.setColor(DISCORD_BOT_COLOR);
					builder.setTitle("Welcome to Seriex!");
					List<ItemComponent> alo = new ArrayList<>();
					alo.add(Button.primary("register", "Click to register!"));
					alo.add(Button.link(serverConfig.SERVER_WEBSITE.value() + "/tos.txt", "Seriex TOS"));
					alo.add(Button.link("https://discord.com/terms", "Discord TOS"));
					event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(alo).queue();
				}
				if ("$create_language".equals(event.getMessage().getContentRaw())) {
					event.getChannel().sendMessage("> Select your language(s)")
								.queue(completeMessage -> Arrays.stream(Language.values()).forEach(lang -> completeMessage.addReaction(Emoji.fromFormatted(lang.unicode())).queue()));
				}
				super.onMessageReceived(event);
			}

			@Override
			public void onMessageReactionAdd(MessageReactionAddEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				if (Objects.equals(event.getChannel().getId(), event.getGuild().getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId())) {
					String emoteName = event.getReaction().getEmoji().getName();
					Language foundLang = null;
					Language[] values = Language.values();
					for (int i = 0; i < values.length; i++) {
						Language language = values[i];
						if (Objects.equals(language.unicode(), emoteName)) {
							foundLang = language;
							break;
						}
					}
					if (foundLang != null) {
						Map<Language, Role> map = roleCache.get(event.getGuild().getId());
						Role role = map.get(foundLang);
						UserSnowflake snowflake = UserSnowflake.fromId(event.getUserId());
						StructPlayerDiscord discord = Seriex.get().database().getPlayerDiscord(snowflake);
						discord.languages |= foundLang.mask();
						event.getGuild().addRoleToMember(snowflake, role).queue();
						logger().info("Added role %s to the member %s!", role.getName(), event.getMember().getUser().getAsTag());
					}
				}
				super.onMessageReactionAdd(event);
			}

			@Override
			public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				if (Objects.equals(event.getChannel().getId(), event.getGuild().getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId())) {
					String emoteName = event.getReaction().getEmoji().getName();
					Language foundLang = null;
					Language[] values = Language.values();
					for (int i = 0; i < values.length; i++) {
						Language language = values[i];
						if (Objects.equals(language.unicode(), emoteName)) {
							foundLang = language;
							break;
						}
					}
					if (foundLang != null) {
						Map<Language, Role> map = roleCache.get(event.getGuild().getId());
						Role role = map.get(foundLang);
						String userId = event.getUserId();
						UserSnowflake fromId = UserSnowflake.fromId(userId);
						event.getGuild().removeRoleFromMember(fromId, role).queue();
						try {
							logger().info("Removed role %s from the member %s!", role.getName(), event.getMember().getUser().getAsTag());
						}
						catch (Exception e) {
							logger().info("Removed role %s from the member %s (id)!", role.getName(), userId);
						}
					}
				}
				super.onMessageReactionRemove(event);
			}

			@Override
			public void onButtonInteraction(ButtonInteractionEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				if ("register".equals(event.getComponentId())) {
					// @DISABLE_FORMATTING
					TextInput usernameInput = TextInput
								.create("username", "Username", TextInputStyle.SHORT)
								.setPlaceholder("Your in-game username.")
								.setRequired(true)
								.setRequiredRange(3, 16)
								.build();
					TextInput passwordInput =
								TextInput
								.create("password", "Password", TextInputStyle.SHORT)
								.setPlaceholder("Your password. (At least 8, maximum of 16 characters.)")
								.setRequired(true)
								.setMinLength(8)
								.setMaxLength(16).build();
					Modal modal = Modal
								.create("verify_panel", "Verification")
								.addActionRows(
											ActionRow.of(usernameInput),
											ActionRow.of(passwordInput)
											)
								.build();
					event.replyModal(modal).queue();
					// @ENABLE_FORMATTING
				} else {
					Seriex.get().logger().fatal("Unsupported modal component id: %s", event.getComponentId());
				}
				super.onButtonInteraction(event);
			}

			@Override
			public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
				String guildID = event.getGuild().getId();
				if (!Objects.equals(guildID, discordConfig.ID_GUILD.value())) return;
				String componentId = event.getComponentId();
				if (componentId.startsWith("2FA-")) {
					String username = componentId.replace("2FA-", "");
					StructPlayer player = Seriex.get().database().getPlayer(username);
					if (event.getValues().isEmpty()) {
						event.reply("what").setEphemeral(true).queue();
						return;
					}
					if (event.getValues().size() == 1) {
						boolean hasGoogleAuth = "google".equals(event.getValues().get(0));
						if (hasGoogleAuth) {
							if (!StringUtils.isEmpty(player.gAuth)) {
								event.reply("You cant change your 2FA selection.").setEphemeral(true).queue();
								return;
							}
							String secretKey = GAuth.generateSecretKey();
							StringBuilder lol = new StringBuilder();
							lol.append("```\n");
							lol.append("Heres your secret key: `");
							lol.append(secretKey);
							lol.append("`.\n");
							lol.append("```\n");
							lol.append("How to add your secret key to the authenticator app:\n");
							lol.append("> Open your Google Authenticator app\n");
							lol.append("> Press the '+' button and select 'Manual entry'\n");
							lol.append("> Enter your username and in the 'Key' field paste your secret key\n");
							lol.append("> Save\n");
							lol.append("> You should now see your entry in the list with 6-digits code that is changing every 30 seconds.\n");
							event.reply(lol.toString()).setEphemeral(true).queue();
							player.gAuth = secretKey;
							player.update();
						} else {
							if (!"-".equals(player.gAuth)) {
								event.reply("Continuing with no 2FA.").setEphemeral(true).queue();
								player.gAuth = "-";
								player.update();
							} else {
								event.reply("You cant change your 2FA selection. (not added yet)").setEphemeral(true).queue();
							}
						}
					} else {
						event.reply("how did you select more than 1??").setEphemeral(true).queue();
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
					if (optional_username.isPresent() && optional_password.isPresent()) {
						String modalUsername = optional_username.get().getAsString();
						if (!USERNAME_PATTERN.matcher(modalUsername).find()) {
							event.reply(String.format("Invalid username %s!", modalUsername)).setEphemeral(true).queue();
							return;
						}
						String modalPassword = optional_password.get().getAsString();
						if (modalPassword.contains(" ")) {
							event.reply("Password cannot contain spaces!").setEphemeral(true).queue();
							return;
						}
						StructPlayer structPlayer = new StructPlayer();
						structPlayer.username = modalUsername;
						structPlayer.salt = Hashing.randomString(8);
						structPlayer.firstLogin = true;
						structPlayer.password = Hashing.hashedString(structPlayer.salt + modalPassword);
						StructPlayerWallet structPlayerWallet = new StructPlayerWallet();
						structPlayerWallet.coins = 0;
						//	final byte[] bytes = UUID.nameUUIDFromBytes(username.getBytes(UTF_8)).toString().getBytes(UTF_8);
						//	String sha256 = "0x2173" + DigestUtils.sha256Hex(bytes);
						structPlayerWallet.player_wallet = "0x" + Primitives.unsignedInt(modalUsername.hashCode());
						if (Seriex.get().database().getPlayer(modalUsername) != null) {
							event.reply(String.format("A player with the username %s already exists!", modalUsername)).setEphemeral(true).queue();
							return;
						}
						if (!structPlayer.create()) {
							event.reply("Couldnt register! (0x1)").setEphemeral(true).queue();
							return;
						}
						// TODO find better fix for desync player_ids...
						StructPlayer databaseStructPlayer = Seriex.get().database().getPlayer(modalUsername);
						if (databaseStructPlayer == null) {
							event.reply("what the fuck (0x0)").setEphemeral(true).queue();  // should never happen
							return;
						}
						int databaseID = databaseStructPlayer.player_id;
						structPlayerWallet.player_id = databaseID; // create is called later so we can do this
						StructPlayerDiscord structPlayerDiscord = new StructPlayerDiscord();
						long idLong = event.getMember().getIdLong();
						structPlayerDiscord.discord_id = idLong;
						User user = event.getMember().getUser();
						structPlayerDiscord.joinedAs = user.getName() + "#" + user.getDiscriminator();
						structPlayerDiscord.linkMS = System.currentTimeMillis();
						structPlayerDiscord.player_id = databaseID;
						UserSnowflake snowflake = UserSnowflake.fromId(idLong);
						if (!structPlayerDiscord.create()) {
							event.reply("Couldnt register! (0x2)").setEphemeral(true).queue();
							return;
						}
						if (!structPlayerWallet.create()) {
							event.reply("Couldnt register! (0x3)").setEphemeral(true).queue();
							return;
						}
						Role guildVerifiedRole = verifiedRole.get(guildID);
						List<String> defaultValue = new ArrayList<>();
						defaultValue.add("none");
						event.getGuild().addRoleToMember(snowflake, guildVerifiedRole).queue();
						SelectMenu twoFA = SelectMenu.create("2FA-" + modalUsername).setPlaceholder("Choose 2FA method").setRequiredRange(1, 1).setDefaultValues(defaultValue)
									.addOption("Google 2FA", "google").addOption("None", "none").build();
						event.reply(String.format("Registered as %s! Please select your 2FA method (Select wisely cannot be changed later!)", modalUsername)).addActionRow(twoFA).setEphemeral(true).queue();
						Member member = event.getMember();
						event.getGuild().getTextChannelById(discordConfig.ID_REGISTER_LOGS.value())
									.sendMessage(String.format("%s#%s (%s) registered as %s", member.getEffectiveName(), member.getUser().getDiscriminator(), member.getId(), modalUsername)).queue();
					} else {
						event.reply("Couldn`t register, try again!").setEphemeral(true).queue();
					}
				}
				super.onModalInteraction(event);
			}
		});
		try {
			jda = builder.build();
		}
		catch (LoginException e) {
			if (discordConfig.BOT_TOKEN.value().equals(Seriex.get().fileManager().PRIVATE)) {
				Seriex.logger().fatal("No token found in config!");
			} else {
				e.printStackTrace();
			}
		}
	}

	public void addMessageToQueue(Player sender, String message) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(DISCORD_BOT_COLOR);
		String name = sender.getName();
		builder.setThumbnail(String.format("https://mc-heads.net/avatar/%s", name));
		builder.addField(name, message, true);
		serverChatMessages.add(builder.build());
	}

	@Override
	public void cleanup() throws SeriexException {
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
