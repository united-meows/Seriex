package pisi.unitedmeows.seriex.managers.http;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.json.JsonMapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.lang.reflect.Type;

import static pisi.unitedmeows.seriex.listener.SeriexSpigotListener.serverChatMessages;
import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.NAME;

public class WebServer extends Manager {
	private Javalin server;
	private static final Gson GSON = new Gson();

	@Override
	@SuppressWarnings("java:S2095") // Toggle off useless try-resources-block-or-else-LMAO!!! warning
	public void post(Seriex seriex) {
		server = Javalin
					.create(config -> {
						config.jsonMapper(this.gsonMapper());
						config.showJavalinBanner = false;
					})
					.get("/players", this::players)
					.get("/online_players", this::online_players)
					.get("/chat", this::chat)
					.start(2173);
	}

	@Override
	public void cleanup() throws SeriexException {
		server.close();
	}

	private void chat(Context ctx) {
		// SeriexSpigotListener.serverChatMessages
		ctx.json(serverChatMessages);
		ctx.status(HttpStatus.OK);
	}

	private void online_players(Context ctx) {
		ctx.json(Bukkit.getOnlinePlayers().stream().map(OnlinePlayer::fromPlayer).toList());
		ctx.status(HttpStatus.OK);
	}

	private void players(Context ctx) {
		ctx.json(Seriex.get().database().getPlayers().stream().map(DatabasePlayer::fromStruct).toList());
		ctx.status(HttpStatus.OK);
	}

	private record PlayerDiscord(long snowflake, String discord_avatar, long registeredAt) {
		public static PlayerDiscord fromPlayerW(PlayerW playerW) {
			var discord = playerW.playerDiscord();
			return new PlayerDiscord(discord.snowflake, discord.user().getAvatarUrl(), discord.linkMS);
		}

		public static PlayerDiscord fromStruct(StructPlayer structPlayer) {
			var discord = Seriex.get().database().getPlayerDiscord(structPlayer.player_id);
			return new PlayerDiscord(discord.snowflake, discord.user().getAvatarUrl(), discord.linkMS);
		}
	}

	private record OnlinePlayer(String username, PlayerDiscord discord, long onlineTime) {
		public static OnlinePlayer fromPlayer(Player player) {
			var playerW = Seriex.get().dataManager().user(player);
			return new OnlinePlayer(playerW.attribute(NAME), PlayerDiscord.fromPlayerW(playerW), playerW.playMS());
		}
	}

	private record DatabasePlayer(String username, PlayerDiscord discord, long playTime, long lastLogin) {
		public static DatabasePlayer fromStruct(StructPlayer structPlayer) {
			SeriexDB database = Seriex.get().database();
			var playerDiscord = PlayerDiscord.fromStruct(structPlayer);
			var playerLogins = database.getPlayerLogins(structPlayer.player_id);

			return new DatabasePlayer(
						structPlayer.username,
						playerDiscord,
						System.currentTimeMillis() - structPlayer.playTime,
						playerLogins.stream().map(struct -> struct.ms).findFirst().orElse(0L)
			);
		}
	}

	private JsonMapper gsonMapper() {
		return new JsonMapper() {
			@Override
			public @NotNull String toJsonString(@NotNull Object obj, @NotNull Type type) {
				return GSON.toJson(obj, type);
			}

			@Override
			public <T> @NotNull T fromJsonString(@NotNull String json, @NotNull Type targetType) {
				return GSON.fromJson(json, targetType);
			}
		};
	}
}

