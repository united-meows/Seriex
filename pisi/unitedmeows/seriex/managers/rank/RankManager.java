package pisi.unitedmeows.seriex.managers.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.single.impl.RanksConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.utils.kThread;

public class RankManager extends Manager {
	private Map<Ranks, List<Ranks>> inheritanceMap;
	private Map<Ranks, RankData> rankCache;
	private GroupManager groupManager;

	@Override
	public void start(Seriex seriex) {
		this.rankCache = new HashMap<>();
		this.inheritanceMap = new HashMap<>();
		this.groupManager = (GroupManager) Seriex.get().plugin().getServer().getPluginManager().getPlugin("GroupManager");
	}

	@SuppressWarnings("resource")
	public void modifyPermissions(Ranks rank, String perm, int op) {
		Seriex seriex = Seriex.get();
		RanksConfig cfg = Seriex.get().fileManager().config(RanksConfig.class);
		String basePath = rank.internalName();
		CommentedFileConfig config = cfg.config();
		ArrayList<String> permissions = config.get(basePath + ".permissions");
		if (op == 0x1) permissions.add(perm);
		else if (op == 0x2) permissions.remove(perm);
		config.set(basePath + ".permissions", permissions);
		rankCache.get(rank).permissions = new HashSet<>(permissions);
		handleGroupManager();
	}

	public void onChangePlayerRank(PlayerW playerW) {
		AtomicBoolean update = new AtomicBoolean(false);
		WorldsHolder worldsHolder = groupManager.getWorldsHolder();
		Bukkit.getWorlds().forEach(world -> {
			OverloadedWorldHolder dataHolder = worldsHolder.getWorldData(world.getName());
			User user = dataHolder.getUser(playerW.hook().getName());
			// we can do this because this is after the players rank changes
			Group newGroup = dataHolder.getGroup(playerW.rank().internalName());
			if (user.getGroup().equals(newGroup)) {
				user.setGroup(newGroup);
				update.set(true);
			}
		});
		if (update.get()) {
			worldsHolder.saveChanges(true);
			GroupManager.getBukkitPermissions().updateAllPlayers();
		}
	}

	private void handleGroupManager() {
		AtomicBoolean update = new AtomicBoolean(false);
		WorldsHolder worldsHolder = groupManager.getWorldsHolder();
		Bukkit.getWorlds().forEach(world -> {
			OverloadedWorldHolder dataHolder = worldsHolder.getWorldData(world.getName());
			for (Ranks rank : Ranks.values()) {
				Group group = dataHolder.getGroup(rank.internalName());
				if (group == null) group = dataHolder.createGroup(rank.internalName());

				AtomicReference<Group> atomicBoxedGroup = new AtomicReference<>(group);
				rankData(rank).permissions.forEach(perm -> {
					Group atomicGroup = atomicBoxedGroup.get();
					if (!atomicGroup.hasSamePermissionNode(perm)) {
						atomicGroup.addPermission(perm);
						update.set(true);
					}
				});
			}
		});
		if (update.get()) {
			worldsHolder.saveChanges(true);
			GroupManager.getBukkitPermissions().updateAllPlayers();
		}
	}

	public boolean playerHasRankOrAbove(PlayerW player, Ranks ranks) {
		return player.rank().priority() <= ranks.priority();
	}

	@Deprecated
	public boolean playerHasRankOrAbove(Player player, Ranks ranks) {
		return playerHasRankOrAbove(Seriex.get().dataManager().user(player), ranks);
	}

	/**
	 * @deprecated See {@link RankManager#playerHasRankOrAbove}
	 */
	@Deprecated(forRemoval = true)
	public boolean playerInheritsRank(Player player, Ranks ranks) {
		PlayerW playerW = Seriex.get().dataManager().user(player);
		return inheritanceMap.get(ranks).contains(playerW.rank());
	}

	public boolean playerHasPermission(Player player, String permission) {
		return player.hasPermission(permission);
	}

	public boolean playerHasPermission(PlayerW playerW, String permission) {
		return playerHasPermission(playerW.hook(), permission);
	}

	@Override
	@SuppressWarnings("resource")
	public void post(Seriex seriex) {
		Ranks[] values = Ranks.values();
		RanksConfig cfg = Seriex.get().fileManager().config(RanksConfig.class);
		String base = ((ServerConfig) Seriex.get().fileManager().config(ServerConfig.class)).SERVER_NAME.value().toLowerCase(Locale.ENGLISH);
		CommentedFileConfig config = cfg.config();
		AtomicBoolean savedAny = new AtomicBoolean(false);
		for (Ranks rank : values) {
			String basePath = rank.internalName();

			BiConsumer<String, Object> consumer = (str, obj) -> {
				var cfgStr = base + "." + str;
				if (config.get(cfgStr) == null) {
					config.set(cfgStr, obj);
					savedAny.set(true);
				}
			};

			consumer.accept(basePath + ".shortcut", base + "." + basePath);
			consumer.accept(basePath + ".displayName", basePath);
			consumer.accept(basePath + ".permissions", new ArrayList<String>());
			consumer.accept(basePath + ".inheritance", "none");
			consumer.accept(basePath + ".message_color", "&f");
			consumer.accept(basePath + ".ign_color", "&d");
		}

		if (savedAny.get()) {
			config.save();
			// wait for i/o
			kThread.sleep(500L);
		}

		for (Ranks rank : values) {
			RankData data = new RankData();
			String basePath = rank.internalName();
			String inheritedRank = config.get(basePath + ".inheritance");
			data.displayName = config.get(basePath + ".displayName");
			data.shortcut = config.get(basePath + ".shortcut");
			data.messageColor = config.get(basePath + ".message_color");
			data.ignColor = config.get(basePath + ".ign_color");
			if (data.hasNullValues()) {
				Seriex.get().logger().error("Rank '%s' does not have any properties set.");
				continue;
			}
			data.permissions.addAll(config.get(basePath + ".permissions"));
			List<Ranks> inheritedRanks = new ArrayList<>();
			if (!"none".equals(inheritedRank)) {
				String inheritance;
				int amountOfLoops = 0;
				do {
					inheritance = config.get(basePath + ".inheritance");
					inheritedRanks.add(Ranks.of(inheritedRank));
					data.permissions.addAll(config.get(inheritedRank + ".permissions"));
					if (amountOfLoops++ > 100) break;
				} while ("none".equals(inheritance));

			}
			data.permissions.add(data.shortcut);
			this.inheritanceMap.put(rank, inheritedRanks);
			this.rankCache.putIfAbsent(rank, data);
		}
		handleGroupManager();
	}

	public RankData rankData(Ranks rank) {
		return rankCache.get(rank);
	}

	public static class RankData {
		protected String displayName, shortcut, messageColor, ignColor;
		protected Set<String> permissions = new HashSet<>();

		public Set<String> permissions() {
			return permissions;
		}

		public boolean hasNullValues() {
			return displayName == null
						|| shortcut == null
						|| messageColor == null
						|| ignColor == null;
		}

		public String generateSuffix() {
			return Seriex.colorizeString(displayName + " " + ignColor);
		}

		public String generateFormat() {
			return Seriex.colorizeString(displayName + " " + ignColor + "%s" + " &7* " + messageColor + "%s");
		}
	}
}
