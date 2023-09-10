package pisi.unitedmeows.seriex.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
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
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.discord.modals.IModal;
import pisi.unitedmeows.seriex.discord.modals.impl.Change2FAModal;
import pisi.unitedmeows.seriex.discord.modals.impl.PasswordRecoveryModal;
import pisi.unitedmeows.seriex.discord.modals.impl.RegisterModal;
import pisi.unitedmeows.seriex.discord.modals.impl.UnregisterModal;
import pisi.unitedmeows.seriex.discord.utilities.DiscordCache;
import pisi.unitedmeows.seriex.discord.utilities.MessageHandler;
import pisi.unitedmeows.seriex.discord.utilities.ReactionHandler;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.Once;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.safety.Try;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Promise;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DiscordBot extends Manager implements Once {
	public static final String INTERNAL_REGISTER_COMMAND = "$create_register";
	public static final String INTERNAL_LANGUAGE_COMMAND = "$create_language";
	public static final String INTERNAL_RECOVERY_COMMAND = "$create_recovery";
	public static final String INTERNAL_SETTINGS_COMMAND = "$create_settings";

	public final DiscordCache discordCache;
	private final MessageHandler messageHandler;
	private final ReactionHandler reactionHandler;

	private final List<IModal> modals = new ArrayList<>();

	private static final Queue<String> serverChatMessages = new ArrayDeque<>();

	private Promise sendPromise;
	private JDA jda;

	public DiscordBot(FileManager manager) {
		DiscordConfig discordConfig = manager.config(DiscordConfig.class);
		var builder = JDABuilder
					.createDefault(discordConfig.BOT_TOKEN.value())
					.enableIntents(EnumSet.allOf(GatewayIntent.class))
					.setChunkingFilter(ChunkingFilter.ALL)
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.setBulkDeleteSplittingEnabled(true)
					.setCompression(Compression.NONE);

		this.discordCache = DiscordCache.create();
		this.messageHandler = MessageHandler.create();
		this.reactionHandler = ReactionHandler.create();

		modals.add(new Change2FAModal());
		modals.add(new PasswordRecoveryModal());
		modals.add(new RegisterModal());
		modals.add(new UnregisterModal());

		Try.safe(() -> builder.setActivity(Activity.of(ActivityType.valueOf(discordConfig.ACTIVITY_TYPE.value()), discordConfig.ACTIVITY_MESSAGE.value(), discordConfig.ACTIVITY_URL.value())), "Couldnt set activity!");
		var logger = Seriex.get().logger();
		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onReady(@NotNull ReadyEvent event) {
				logger.info("SeriexBot is ready!");
				event.getJDA().getGuilds().forEach(discordCache::cacheInitial);

				sendPromise = Async.async_loop(() -> {
					if (serverChatMessages.isEmpty()) return;

					StringBuilder finalMessage = new StringBuilder();

					String chatMessage;
					int amount = 0;

					while ((chatMessage = serverChatMessages.poll()) != null) {
						finalMessage.append(chatMessage).append("\n");
						if (amount++ >= 50) break;
					}

					if (amount == 0 || finalMessage.isEmpty()) return;

					var msg = finalMessage.toString();
					if (msg.length() > 2000)
						msg = msg.substring(0, 2000);

					sendMessageInstantly(msg);
				}, 1500);
			}

			@Override
			public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
				discordCache.onMemberJoin(event.getGuild(), Objects.requireNonNull(event.getMember()));
			}

			@Override
			public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
				discordCache.onMemberQuit(event.getGuild(), Objects.requireNonNull(event.getMember()));
			}

			@Override
			public void onMessageReceived(@NotNull MessageReceivedEvent event) {
				messageHandler.handleMessages(event);
			}

			@Override
			public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
				reactionHandler.onReactionAdded(event, discordCache);
			}

			@Override
			public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
				reactionHandler.onReactionRemoved(event, discordCache);
			}

			@Override
			public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;

				String componentId = event.getComponentId();
				var optionalModal = modals.stream().filter(modal -> modal.buttonName().equals(componentId)).findFirst();
				if (optionalModal.isEmpty()) {
					logger.error("Unsupported modal component id: {}", componentId);
					return;
				}

				var modal = optionalModal.get();
				event.replyModal(modal.createdModal()).complete();
				super.onButtonInteraction(event);
			}

			@Override
			public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
				String guildID = Objects.requireNonNull(event.getGuild()).getId();
				if (!Objects.equals(guildID, discordConfig.ID_GUILD.value())) return;
				String componentId = event.getComponentId();
				if (componentId.startsWith("2FA-")) {
					String username = componentId.replace("2FA-", "");
					StructPlayer player = Seriex.get().database().getPlayer(username);

					if (event.getValues().size() != 1)
						return;

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
			public void onModalInteraction(@NotNull ModalInteractionEvent event) {
				if (!Objects.equals(event.getGuild().getId(), discordConfig.ID_GUILD.value())) return;

				String modalId = event.getModalId();
				var optionalModal = modals.stream().filter(modal -> modal.modalName().equals(modalId)).findFirst();

				if (optionalModal.isEmpty()) {
					logger.error("Unsupported modal interaction id: {}", modalId);
					return;
				}

				optionalModal.get().modalInteraction(event, DiscordBot.this);
				super.onModalInteraction(event);
			}
		});

		try {
			jda = builder.build();
		}
		catch (LoginException e) {
			if (discordConfig.BOT_TOKEN.value().equals(Seriex.get().fileManager().todo())) {
				logger.error("No token found in config!");
			} else {
				logger.error("Could not start JDA", e);
			}
		}
	}

	public void addMessageToQueue(String message) {
		serverChatMessages.add(message);
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
		// TODO: AutoConfigure
	}
}
