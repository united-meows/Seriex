package pisi.unitedmeows.seriex.managers.scoreboard;

import static java.text.MessageFormat.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.mrmicky.fastboard.FastBoard;
import me.realized.duels.api.arena.Arena;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.managers.minigames.impl.potpvp.PotPVP;
import pisi.unitedmeows.seriex.managers.minigames.impl.survival.Survival;
import pisi.unitedmeows.seriex.util.config.single.impl.ScoreboardConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.I18n;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class ScoreboardManager extends Manager {
	private final Map<UUID, FastBoard> boards = new HashMap<>();
	private FancyScoreboardText fancyScoreboardText;
	private String title_prefix;
	private String subtitle_prefix;

	@Override
	public void start(Seriex seriex) {
		ScoreboardConfig scoreboardConfig = Seriex.get().fileManager().config(ScoreboardConfig.class);
		title_prefix = scoreboardConfig.TITLE_PREFIX.value();
		subtitle_prefix = scoreboardConfig.SUBTITLE_PREFIX.value();

		ServerConfig serverConfig = Seriex.get().fileManager().config(ServerConfig.class);
		String value = serverConfig.SERVER_NAME.value();
		fancyScoreboardText = new FancyScoreboardText(value,
					ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE,
					ChatColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE);
		seriex.plugin().getServer().getScheduler().runTaskTimer(seriex.plugin(), () -> {
			fancyScoreboardText.moveRight();
			for (FastBoard board : this.boards.values()) {
				Player player = board.getPlayer();
				if (!player.isOnline()) {
					onQuit(player);
					continue;
				}
				updateBoard(board);
			}
		}, 0, 10);
	}

	public void onJoin(Player player) {
		FastBoard board = new FastBoard(player);
		this.boards.put(player.getUniqueId(), board);
	}

	public void onQuit(Player player) {
		FastBoard board = this.boards.remove(player.getUniqueId());
		if (board != null) {
			board.delete();
		}
	}

	private void updateBoard(FastBoard board) {
		I18n i18n = Seriex.get().I18n();
		Player player = board.getPlayer();
		PlayerW user = Seriex.get().dataManager().user(player);
		board.updateTitle(fancyScoreboardText.getAnimatedText());
		List<String> lines = new ArrayList<>();

		final String smthColor = "&d";
		final String resetColor = "&7";

		lines.add("");

		lines.add(format("{0} {1}", title_prefix, i18n.getMessage(Messages.SCOREBOARD_INFO_TITLE, user)));
		lines.add(format("{0} {1}㎳{2} {3}", subtitle_prefix, smthColor, resetColor, getColoredPing(user.getPing()) + formattedNumber(user.getPing(), 0)));
		lines.add(format("{0} {1}✪{2} {3}", subtitle_prefix, smthColor, resetColor, user.anticheat().displayName));
		lines.add(format("{0} {1}✧{2} {3}", subtitle_prefix, smthColor, resetColor, user.rank().internalName().toUpperCase(Locale.ENGLISH)));
		lines.add(format("{0} {1}${2} {3}", subtitle_prefix, smthColor, resetColor, formattedNumber(user.playerWallet().coins, 0)));
		lines.add("");

		lines.add(format("{0} {1}", title_prefix, i18n.getMessage(Messages.SCOREBOARD_INFO_SERVER, user)));
		lines.add(format("{0} {1}TPS {2}", subtitle_prefix, smthColor, getColoredTPS(getTPS())));
		lines.add("");

		AtomicBoolean addedLines = new AtomicBoolean(false);
		PlayerState playerState = user.playerState();
		if (playerState == PlayerState.DUEL) {
			Arena arena = Seriex.get().duels().getArenaManager().get(player);
			if (arena != null && arena.getMatch() != null) {
				arena.getMatch().getPlayers().stream().filter(p -> p != player).findFirst().ifPresent(opponentPlayer -> {
					lines.add(format("{0} Duel", title_prefix));
					PlayerW opponentUser = Seriex.get().dataManager().user(opponentPlayer);
					lines.add(format("{0} {1}⚔{2} {3}", subtitle_prefix, smthColor, resetColor, opponentPlayer.getName()));
					lines.add(format("{0} {1}❤{2} {3}/{4}", subtitle_prefix, smthColor, resetColor, opponentPlayer.getHealth(), opponentPlayer.getMaxHealth()));
					lines.add(format("{0} {1}㎳{2} {3}", subtitle_prefix, smthColor, resetColor, getColoredPing(opponentUser.getPing()) + formattedNumber(opponentUser.getPing(), 0)));
					lines.add(format("{0} {1}✪{2} {3}", subtitle_prefix, smthColor, resetColor, opponentUser.anticheat().displayName));
					addedLines.set(true);
				});
			}
		} else if (playerState == PlayerState.MINIGAMES) {
			Minigame currentMinigame = user.currentMinigame();
			lines.add(format("{0} {1}", title_prefix, currentMinigame.name));
			lines.add(format("{0} Streak {1}⚔{2} {3}", subtitle_prefix, smthColor, resetColor, currentMinigame.streak(player)));
			lines.add(format("{0} {1}● {2}/{3}", subtitle_prefix, smthColor, currentMinigame.playersInMinigame.size(), Bukkit.getOnlinePlayers().size()));
			if (currentMinigame instanceof PotPVP || currentMinigame instanceof Survival) {
				lines.add(format("{0} Healing {1}{2}", subtitle_prefix, smthColor,
							Arrays.stream(player.getInventory().getContents())
										.filter(Objects::nonNull)
										.filter(item -> {
											int durabilityToSearch = currentMinigame instanceof PotPVP ? 16421 : 8229;
											return item.getDurability() == durabilityToSearch;
										}).map(ItemStack::getAmount).count()));
			}
			addedLines.set(true);
		} else if (playerState == PlayerState.SPAWN) {
			Seriex.get().areaManager().areaList.stream()
						.filter(area -> area.players.contains(player.getUniqueId()))
						.findFirst().ifPresent(area -> {
							lines.add(format("{0} Current Area", title_prefix));
							lines.add(format("{0} Name {1}{2}", subtitle_prefix, smthColor, area.name));
							addedLines.set(true);
						});
		}
		if (addedLines.get())
			lines.add("");
		board.updateLines(lines.stream().map(line -> {
			String line0 = line;
			if (line0.length() > 30)
				line0 = line0.substring(0, 30);

			return Seriex.colorizeString(line0);
		}).toArray(String[]::new));
	}

	private final char[] suffixes = new char[] {
				'K', 'M', 'B', 'T'
	};

	private String formattedNumber(double number, int iteration) {
		if (number < 100_000)
			return String.valueOf((int) number);

		double exponent = (double) (long) number / 100 / 10.0;
		boolean isRound = exponent * 10 % 10 == 0;
		String formatted = String.valueOf(isRound || exponent > 9.99 ? (int) exponent * 10.0 / 10.0 : exponent) + suffixes[iteration];
		return exponent < 1000 ? formatted : formattedNumber(exponent, iteration + 1);
	}

	private ChatColor getColoredPing(int ping) {
		ChatColor color = ChatColor.RED;
		if (ping < 70) color = ChatColor.GREEN;
		else if (ping < 100) color = ChatColor.DARK_GREEN;
		else if (ping < 250) color = ChatColor.BLUE;
		return color;
	}

	private String getColoredTPS(double tps) {
		ChatColor color = ChatColor.GREEN;
		if (tps < 5.0D) color = ChatColor.RED;
		else if (tps < 10.0D) color = ChatColor.BLUE;
		else if (tps < 18.0D) color = ChatColor.YELLOW;
		return color + String.valueOf(tps);
	}

	private double getTPS() { return clamp(roundToPlace(MinecraftServer.getServer().recentTps[0], 2), 0.0D, 20.0D); }

	private double clamp(double num, double min, double max) {
		return num > max ? max : Math.max(num, min);
	}

	private double roundToPlace(double value, int places) {
		if (places < 0)
			return value;
		return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
}
