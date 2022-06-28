package pisi.unitedmeows.seriex.discord;

import static java.nio.charset.StandardCharsets.*;
import static pisi.unitedmeows.seriex.Seriex.*;

import java.awt.Color;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
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
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerWallet;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Promise;

public class DiscordBot extends Manager {
	private static final Color DISCORD_BOT_COLOR = new Color(8281781);
	private static final Color VERIFIED_MEMBER_COLOR = new Color(42, 106, 209);
	public static final Map<String, Map<Language, Role>> roleCache = new HashMap<>();
	private static final Map<String, Role> verifiedRole = new HashMap<>();
	private static final Queue<MessageEmbed> serverChatMessages = new ArrayDeque<>();
	private JDA jda;
	private Promise sendPromise;

	public DiscordBot(FileManager manager) {
		DiscordConfig discordConfig = (DiscordConfig) manager.getConfig(manager.DISCORD);
		ServerConfig serverConfig = (ServerConfig) manager.getConfig(manager.SERVER);
		JDABuilder builder = JDABuilder.createDefault(discordConfig.BOT_TOKEN.value());
		builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
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
								logger().info("Created cache for the role %s in the guild %s!", language.name(), guild.getName());
								Role value = rolesByName.stream().findFirst().get();
								map.put(language, value);
								roleCache.put(guild.getId(), map);
							}
						});
					}
				});
				sendPromise = Async.async_loop(() -> {
					MessageEmbed embed = serverChatMessages.poll();
					event.getJDA().getGuilds().forEach((Guild guild) -> {
						guild.getTextChannelById(discordConfig.ID_SERVER_CHAT.value()).sendMessageEmbeds(embed);
					});
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
								.queue(completeMessage -> Arrays.stream(Language.values()).forEach(lang -> completeMessage.addReaction(lang.unicode()).queue()));
				}
				super.onMessageReceived(event);
			}

			@Override
			public void onMessageReactionAdd(MessageReactionAddEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				if (Objects.equals(event.getChannel().getId(), event.getGuild().getTextChannelById(discordConfig.ID_LANGUAGE_CHANNEL.value()).getId())) {
					String emoteName = event.getReactionEmote().getName();
					Language foundLang = null;
					for (Language language : Language.values()) {
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
					String emoteName = event.getReactionEmote().getName();
					Language foundLang = null;
					for (Language language : Language.values()) {
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
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;
				if ("verify_panel".equals(event.getModalId())) {
					Supplier<Stream<ModalMapping>> stream = event.getInteraction().getValues()::stream;
					Optional<ModalMapping> optional_username = stream.get().filter(modalMapping -> "username".equals(modalMapping.getId())).findAny();
					Optional<ModalMapping> optional_password = stream.get().filter(modalMapping -> "password".equals(modalMapping.getId())).findAny();
					if (optional_username.isPresent() && optional_password.isPresent()) {
						String username = optional_username.get().getAsString();
						String password = optional_password.get().getAsString();
						StructPlayer structPlayer = new StructPlayer();
						structPlayer.username = username;
						structPlayer.salt = Hashing.randomString(8);
						structPlayer.firstLogin = true;
						structPlayer.password = Hashing.hashedString(structPlayer.salt + password);
						StructPlayerWallet structPlayerWallet = new StructPlayerWallet();
						structPlayerWallet.player_id = structPlayer.player_id;
						structPlayerWallet.coins = 0;
						final byte[] bytes = UUID.nameUUIDFromBytes(username.getBytes(UTF_8)).toString().getBytes(UTF_8);
						structPlayerWallet.player_wallet = "0x2173" + DigestUtils.sha256Hex(bytes);
						structPlayerWallet.create();
						boolean structPlayerCreated = Seriex.get().database().createStruct(structPlayer, "WHERE NOT EXISTS (SELECT * FROM %s WHERE username='%d')".replace("%d", username));
						if (!structPlayerCreated) {
							event.reply("Couldnt register (0x1)!").setEphemeral(true).queue();
							return;
						}
						StructPlayerDiscord structPlayerDiscord = new StructPlayerDiscord();
						long idLong = event.getMember().getIdLong();
						structPlayerDiscord.discord_id = idLong;
						User user = event.getMember().getUser();
						structPlayerDiscord.joinedAs = user.getName() + "#" + user.getDiscriminator();
						structPlayerDiscord.linkMS = System.currentTimeMillis();
						structPlayerDiscord.player_id = structPlayer.player_id;
						boolean structPlayerDiscordCreated = Seriex.get().database().createStruct(structPlayer, "WHERE NOT EXISTS (SELECT * FROM %s WHERE discord_id='%d')".replace("%d", idLong + ""));
						if (!structPlayerDiscordCreated) {
							event.reply("Couldnt register (0x2)!").setEphemeral(true).queue();
							return;
						}
						event.reply(String.format("Registered as %s!", username)).setEphemeral(true).queue();
						Member member = event.getMember();
						event.getGuild().getTextChannelById(discordConfig.ID_REGISTER_LOGS.value())
									.sendMessage(String.format("%s#%s (%s) registered as %s", member.getEffectiveName(), member.getUser().getDiscriminator(), member.getId(), username)).queue();
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
			e.printStackTrace();
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
}
