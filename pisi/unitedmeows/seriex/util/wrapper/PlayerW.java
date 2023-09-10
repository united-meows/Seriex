package pisi.unitedmeows.seriex.util.wrapper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagManager;

import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.matcher.FieldMatcher;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.api.events.EventFlag;
import pisi.unitedmeows.seriex.api.events.EventTick;
import pisi.unitedmeows.seriex.database.structs.impl.player.*;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.managers.rank.RankManager.RankData;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.MaintainersUtil;
import pisi.unitedmeows.seriex.util.Permissions;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.events.SeriexEventSystem;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.placeholder.annotations.RegisterAttribute;
import pisi.unitedmeows.seriex.util.placeholder.api.IAttributeHolder;
import pisi.unitedmeows.yystal.hook.YString;

import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.NAME;

public class PlayerW {
	/**
	 * Matches the MCP debug name: Player[0000-9999]
	 */
	private static final Pattern GUEST_PATTERN = Pattern.compile("Player[0-9]{1,4}");

	/**
	 * The bukkit Player
	 */
	private Player player;

	/**
	 * The saved player ID from the database
	 */
	private int savedPlayerID = -1;
	/**
	 * String to attribute cache map
	 */
	private Map<String, IAttributeHolder> attributeHoldersString;
	/**
	 * String to attribute enum map
	 */
	private Map<Attributes, IAttributeHolder> attributeHoldersEnum;
	/**
	 * Current selected language for the player
	 */
	private Language selectedLanguage = Language.ENGLISH;
	/**
	 * Selected languages from player`s selections in discord. <br> Works by getting the player discord user from JDA and getting roles. <br> Kicks the player if there is no verified role.
	 */
	private Set<Language> selectedLanguages;
	/**
	 * For our speed-test, we firstly cache their previous walk speed, fly speed, jump boost effect... then we: <br>
	 * <p>
	 * <ul>
	 * <li>set the player`s walk speed to 0
	 * <li>set the player`s fly speed to 0
	 * <li>set the player`s food speed to 3
	 * <li>set the player`s sprinting value to false
	 * <li>add jump boost amplifier 200 with the maximum allowed duration
	 * </ul>
	 * <p>
	 * After the speed-test is finished, we revert to the old values. <br>
	 * <br>
	 * See: <br>
	 * {@link PlayerW#allowMovement()} & {@link PlayerW#denyMovement()}
	 */
	private MovementValuesWrapper prevValues;
	/**
	 * The player`s current session time in milliseconds
	 */
	private long playMS;
	/**
	 * Has the player logged in with the correct password?
	 */
	private boolean loggedIn;
	/**
	 * Has the player successfully entered their password? (not logged in)
	 */
	private boolean correctPassword;
	/**
	 * Current player state. <br> See {@link PlayerState} for possible values.
	 */
	private PlayerState playerState;
	/**
	 * Current player minigame.
	 */
	private Minigame currentMinigame;
	/**
	 * Current player anticheat.
	 */
	private Anticheat anticheat;
	/**
	 * Current players event system
	 */
	private SeriexEventSystem eventSystem;
	/**
	 * Current players rank
	 */
	private Ranks rank;
	/**
	 * Current attack amount
	 */
	private int attacks;
	/**
	 * Should the player bypass the durability patch limit?
	 */
	private boolean patchBypass;
	/**
	 * Is the player allowed to move?
	 */
	private boolean allowedToMove = true;
	/**
	 * Is the player viewing someone else's inventory?
	 */
	private boolean invsee;
	/**
	 * Is the player AFK?
	 */
	private boolean afk;
	/**
	 * UUIDs which the player has ignored
	 */
	private List<String> ignored;

	private final boolean canBeInitialized;

	@RegisterAttribute(Enum = NAME)
	private final IAttributeHolder nameAtr = () -> player.getName();
	@RegisterAttribute(Enum = Attributes.ID)
	private final IAttributeHolder idAtr = () -> String.valueOf(playerInfo().player_id);
	@RegisterAttribute(Enum = Attributes.TOKEN)
	private final IAttributeHolder tokenAtr = () -> playerInfo().token;
	@RegisterAttribute(Enum = Attributes.SALT)
	private final IAttributeHolder saltAtr = () -> playerInfo().salt;
	@RegisterAttribute(Enum = Attributes.PASSWORD)
	private final IAttributeHolder passwordAtr = () -> playerInfo().password;
	@RegisterAttribute(Enum = Attributes.WORLD)
	private final IAttributeHolder worldAtr = () -> player.getWorld().getName();

	public PlayerW(Player _player) {
		player = _player;
		String name = player.getName();
		StructPlayer playerInfo = playerInfo();
		DiscordConfig discordConfig = Seriex.get().fileManager().config(DiscordConfig.class);
		if (playerInfo == null) {
			Seriex.get().logger().warn("Database values of player {} was missing! (maybe not verified?)", name);
			Seriex.get().kick_no_translation(_player, "Please register on discord!\n%s%s", ChatColor.BLUE, discordConfig.INVITE_LINK.value());
			this.canBeInitialized = false;
			return;
		}
		StructPlayerDiscord playerDiscord = this.playerDiscord();
		if (playerDiscord == null) {
			Seriex.get().kick_no_translation(hook(), "Please contact a maintainer... [DISCORD]");
			this.canBeInitialized = false;
			return;
		}
		if (handleSelectedLanguages(discordConfig, playerDiscord)) {
			this.canBeInitialized = false;
			return; // something failed
		}
		/* tries to retrieve player's settings if not exists creates new one */

		StructPlayerSettings playerSettings = playerSettings();
		if (playerSettings == null) {
			Seriex.get().logger().warn("The player {} does not have Settings row on database (maybe first login?)", name);
			playerSettings = new StructPlayerSettings();
			playerSettings.player_id = playerInfo.player_id;
			playerSettings.anticheat = Anticheat.VANILLA.name();
			playerSettings.guest = false;
			playerSettings.flags = true;
			playerSettings.hunger = false;
			playerSettings.selectedLanguage = Language.ENGLISH.languageCode();
			playerSettings.create();
		}

		// failsafe
		boolean update = false;
		if (playerSettings.anticheat == null) {
			playerSettings.anticheat = Anticheat.VANILLA.name();
			Seriex.get().logger().error("Player '{}' had no anticheat in the database.", name);
			update = true;
		}
		if (playerSettings.selectedLanguage == null) {
			playerSettings.selectedLanguage = Language.ENGLISH.languageCode();
			Seriex.get().logger().error("Player '{}' had no selected language in the database.", name);
			update = true;
		}

		if (update)
			playerSettings.update();

		if (playerInfo.rank_name == null) {
			Seriex.get().logger().error("Player '{}' had no rank in the database.", name);
			playerInfo.rank_name = MaintainersUtil.isMaintainer(name) ? Ranks.MAINTAINER.internalName() : Ranks.TESTER.internalName();
			playerInfo.update();
		}

		this.rank = Ranks.of(playerInfo.rank_name);
		Seriex.get().rankManager().onChangePlayerRank(this);
		this.attributeHoldersString = new HashMap<>();
		this.attributeHoldersEnum = new EnumMap<>(Attributes.class);
		this.registerAttributes();
		this.selectedLanguage = Language.fromCode(playerSettings.selectedLanguage, null);
		this.playerState = PlayerState.SPAWN;
		this.eventSystem = new SeriexEventSystem();
		this.eventSystem.subscribeAll(this);
		this.prevValues = null;
		this.ignored = new ArrayList<>();
		this.newPlayMS();
		this.canBeInitialized = true;
	}

	public PlayerW init() {
		if (!canBeInitialized) return this;
		Anticheat.tryToGetFromName(playerSettings().anticheat).convert(this);
		return this;
	}

	public void destruct() {
		if (invsee) {
			hook().getOpenInventory().close();
			invsee(false);
		}
	}

	// this runs every 5 ticks
	public Listener<EventTick> tickEvent = new Listener<EventTick>(event -> {
		ServerConfig serverConfig = Seriex.get().fileManager().config(ServerConfig.class);
		World playerWorld = hook().getWorld();
		World spawnWorld = serverConfig.getWorldSpawn().getWorld();
		if (playerWorld.equals(spawnWorld) && playerState != PlayerState.SPAWN) {
			// something weird has happened, we are not in state spawn while being in spawn

			String playerName = hook().getName();
			if (!Seriex.get().duels().getArenaManager().isInMatch(player)) {
				// player is not in a duel & still not in spawn...
				playerState = PlayerState.SPAWN;
				Seriex.get().logger().error("Player {} is in a duel state but not in a duel?", playerName);
				return;
			}

			// ok we somehow never left the minigame
			if (currentMinigame != null && currentMinigame.isInGame(player)) {
				currentMinigame.onLeave(this);
				playerState = PlayerState.SPAWN;
				Seriex.get().logger().error("Player {} in state minigame & but not in a minigame?", playerName);
				return;
			}
		}

		if (playerState == PlayerState.MINIGAMES && currentMinigame == null) {
			// we are in the state minigame, while in theory; we should be in spawn or in a duel,
			// this should mean something has gone incredibly wrong; so we kick the player
			Seriex.get().kick(hook(), Messages.SERVER_INTERNAL_ERROR);
		}
	}).filter(event -> event.tick() % 2 == 0);
	public Listener<EventFlag> flagEvent = new Listener<>(event -> {
		if (event.anticheat().equals(this.anticheat)) {
			if (playerSettings().flags)
				Seriex.get().msg_no_translation(player, "(%s) - %s, punishable => %b, cancel => %b, vl => %s, log => %s",
							event.anticheat().displayName,
							event.record().name(),
							event.record().punishable(),
							event.record().cancellable(),
							event.record().violation(),
							event.record().log_message());
		} else event.cancel(true); // cancel other anticheat flags....
	});

	public String attribute(String name) {
		IAttributeHolder attributeHolder = attributeHoldersString.getOrDefault(name, null);
		if (attributeHolder != null)
			return attributeHolder.compute();
		return YString.EMPTY_R;
	}

	public String attribute(Attributes attribute) {
		IAttributeHolder attributeHolder = attributeHoldersEnum.getOrDefault(attribute, null);
		if (attributeHolder != null)
			return attributeHolder.compute();
		return YString.EMPTY_R;
	}

	public StructPlayer playerInfo() {
		String name = player.getName();
		StructPlayer databasePlayer = Seriex.get().database().getPlayer(name);
		if (this.savedPlayerID == -1) {
			this.savedPlayerID = databasePlayer.player_id;
		} else if (this.savedPlayerID != databasePlayer.player_id) {
			Seriex.get().logger().error("Desync player id for player {}", name);
			Seriex.get().kick(player, Messages.SERVER_DESYNC);
		}

		return databasePlayer;
	}

	public StructPlayerWallet playerWallet() {
		return Seriex.get().database().getPlayerWallet(this.savedPlayerID);
	}

	public StructPlayerSettings playerSettings() {
		return Seriex.get().database().getPlayerSettings(this.savedPlayerID);
	}

	public StructPlayerDiscord playerDiscord() {
		return Seriex.get().database().getPlayerDiscord(this.savedPlayerID);
	}

	public List<StructPlayerLogin> playerLogins() {
		return Seriex.get().database().getPlayerLogins(this.savedPlayerID);
	}

	public void setLanguage(Language language) {
		this.selectedLanguage = language;
		StructPlayerSettings playerSettings = this.playerSettings();
		playerSettings.selectedLanguage = language.languageCode();
		playerSettings.update();
	}

	public Language selectedLanguage() {
		return selectedLanguage;
	}

	public boolean has2FA() {
		StructPlayer playerInfo = playerInfo();
		return playerInfo != null && playerInfo.has2FA;
	}

	public void fireEvent(Event event) {
		this.eventSystem.fire(event);
	}

	public void updateRank(Ranks rank) {
		StructPlayer structPlayer = playerInfo();
		structPlayer.rank_name = rank.internalName();
		if (structPlayer.update()) {
			this.rank = rank;
			this.updateNametag();
			Seriex.get().rankManager().onChangePlayerRank(this);
		}
	}

	private void setNametagReflection(NametagManager manager, String player, String prefix, String suffix, int sort) {
		Reflexion.on(NametagManager.class)
					.findMethod("setNametag", String.class, String.class, String.class, int.class)
					.orElseThrow().invoke(manager, player, prefix, suffix, sort);
	}

	public void updateNametag() {
		Seriex.get();
		setNametagReflection(NametagEdit.getInstance().getManager(),
					player.getName(), // player name
					rankData().generateSuffix(), // prefix
					afk ? Seriex.colorizeString(" &7&oAFK") : "", // suffix
					rank.priority()); // priority
		NametagEdit.getApi().applyTags();
	}

	public void giveMoney(int amount) {
		if (amount < 0) amount *= -1;

		StructPlayerWallet playerWallet = playerWallet();
		if (playerWallet.coins + amount >= Integer.MAX_VALUE) playerWallet.coins = Integer.MAX_VALUE;
		else playerWallet.coins += amount;
		playerWallet.update();
		Seriex.get().msg(player, Messages.PLAYER_RECEIVE_MONEY, amount);
	}

	public void takeMoney(int amount) {
		if (amount < 0) amount *= -1;

		StructPlayerWallet playerWallet = playerWallet();
		playerWallet.coins -= amount;

		if (playerWallet.coins < 0)
			playerWallet.coins = 0;

		playerWallet.update();
		Seriex.get().msg(player, Messages.PLAYER_SPEND_MONEY, amount);
	}

	public boolean hasDurabilityPatchBypass() {
		ServerConfig serverConfig = Seriex.get().fileManager().config(ServerConfig.class);
		return serverConfig.ALLOW_PATCH_BYPASS.value() && patchBypass;
	}

	public void toggleDurabilityPatchBypass() {
		this.patchBypass ^= true;
	}

	public int attacks() {
		return attacks;
	}

	public void reset_attacks() {
		attacks = 1;
	}

	public boolean ignore(String uuid) {
		if (ignored.contains(uuid)) {
			ignored.remove(uuid);
			return false;
		}
		ignored.add(uuid);
		return true;
	}

	public List<String> ignored() {
		return ignored;
	}

	public boolean isIgnored(String uuid) {
		return ignored.contains(uuid);
	}

	public void updateAttacks(int ctr) {
		this.attacks = ctr;
	}

	public RankData rankData() {
		return Seriex.get().rankManager().rankData(rank);
	}

	public boolean hasPermission(Permissions permission) {
		return hasPermission(permission.permission());
	}

	public boolean hasPermission(String permission) {
		RankData rankData = rankData();
		return rank.operator() || rankData.permissions().contains(permission);
	}

	public boolean isMovementAllowed() {return allowedToMove;}

	public final void denyMovement() {
		Player hooked = hook();
		if (prevValues != null) {
			Seriex.get().logger().error("Already denied movement (will interrupt anyway) for {}!", hooked.getName());
			prevValues.interruptMovement(hooked);
			return;
		}

		(prevValues = new MovementValuesWrapper(hooked)).interruptMovement(hooked);
		allowedToMove = false;
	}

	public final void allowMovement() {
		Player hooked = hook();
		if (prevValues == null) {
			if (MovementValuesWrapper.areValuesBroken(hooked)) {
				(prevValues = new MovementValuesWrapper(hooked)).fixValues();
				prevValues = null;
			} else Seriex.get().logger().error("Movement was not denied for {}!", hooked.getName());
		} else {
			allowedToMove = true;
			prevValues.allowMovement(hooked);
			prevValues = null;
		}
	}

	public boolean isGuest() {return GUEST_PATTERN.matcher(player.getName()).matches();}

	public String getIP() {return player.getAddress().getHostName();}

	public int getPing() {return nmsHandle().ping;}

	public Anticheat anticheat() {
		return anticheat;
	}

	/**
	 * @deprecated <br>
	 * This is only used in Anticheat for internal use and should not be used. <br> Use {@link Anticheat#convert(PlayerW)} instead.
	 */
	@Deprecated
	@SuppressWarnings("all")
	public void unsafe_anticheat(Anticheat anticheat) {
		this.anticheat = anticheat;
	}

	public Minigame currentMinigame() {
		return currentMinigame;
	}

	public void currentMinigame(Minigame currentMinigame) {
		this.currentMinigame = currentMinigame;
	}

	public boolean loggedIn() {
		return loggedIn;
	}

	public void loggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public boolean invsee() {
		return invsee;
	}

	public void invsee(boolean invsee) {
		this.invsee = invsee;
	}

	public boolean correctPassword() {
		return correctPassword;
	}

	public void correctPassword(boolean correctPassword) {
		this.correctPassword = correctPassword;
	}

	public boolean afk() {
		return afk;
	}

	public void afk(boolean afk) {
		this.afk = afk;
		this.updateNametag();
	}

	public PlayerState playerState() {
		return playerState;
	}

	public void playerState(PlayerState playerState) {
		this.playerState = playerState;
	}

	public long playMS() {
		return playMS;
	}

	public boolean disallowCommand() {
		var disallowed = !rank().operator() && playerState != PlayerState.SPAWN;

		if (disallowed)
			Seriex.get().msg(player, Messages.COMMAND_WRONG_STATE, playerState.name());

		return disallowed;
	}

	public boolean doesntHaveRank(Ranks rank) {
		if (hook().isOp()) return false;

		if (!Seriex.get().rankManager().playerHasRankOrAbove(this, rank)) {
			Seriex.get().msg(this, Messages.COMMAND_NOT_ALLOWED, rank.internalName());
			return true;
		}

		return false;
	}

	public void handlePlayMS() {
		StructPlayer playerStruct = playerInfo();
		long newPlayTime = System.currentTimeMillis() - playMS();
		if (playerStruct.playTime > System.currentTimeMillis()) { // bugfix
			playerStruct.playTime = newPlayTime;
		} else {
			playerStruct.playTime += newPlayTime;
		}
		playerStruct.update();
		this.newPlayMS();
	}

	public void newPlayMS() {
		this.playMS = System.currentTimeMillis();
	}

	public Ranks rank() {
		return rank;
	}

	public String getMaskedIP() {
		String ipAddress = getIP();
		if (!ipAddress.contains(".")) {
			Seriex.get().logger().error("IP Address for player {} doesnt contain any dots? (ip = '{}')", attribute(NAME), ipAddress);
			return "Hidden IP";
		}
		final String maskChar = "-";
		final String ip = ipAddress.replace('.', ':');
		final String[] splitIp = ip.split(":");
		final StringBuilder finalIp = new StringBuilder();
		boolean first = false;
		int maskAmount = 3;
		for (int i = 0; i < splitIp.length; i++) {
			if (i < splitIp.length - maskAmount) {
				// example based on ip = 127.0.0.1, maskAmount 2 masks half of the ip 127.0.x.x
				// if you want to mask to the first dot
				// make the 2 -> 3
				// after that ip`s will look like 127.x.x.x
				// appending differently avoids object allocation (til)
				finalIp.append(splitIp[i]);
				finalIp.append(".");
			} else {
				for (int j = 0; j < splitIp[i].length(); j++) {
					if (j == 0) {
						if (!first) {
							finalIp.append(maskChar);
							first = true;
						} else {
							finalIp.append(".");
							finalIp.append(maskChar);
						}
					} else {
						finalIp.append(maskChar);
					}
				}
			}
		}
		return finalIp.toString();
	}

	private boolean handleSelectedLanguages(DiscordConfig discordConfig, StructPlayerDiscord playerDiscord) {
		selectedLanguages = EnumSet.noneOf(Language.class);
		DiscordBot discordBot = Seriex.get().discordBot();
		String guildID = discordConfig.ID_GUILD.value();
		Map<Language, Role> map = DiscordBot.LANGUAGE_ROLE_CACHE.get(guildID);
		Guild guildById = discordBot.JDA().getGuildById(guildID);
		UserSnowflake snowflake = UserSnowflake.fromId(playerDiscord.snowflake);
		if (guildById == null) {
			Seriex.get().logger().error("Couldn't find guild by the id {}", guildID);
			Seriex.get().kick_no_translation(hook(), "Please contact a maintainer... [DISCORD_ID]");
			return true;
		}
		Member member = guildById.getMember(snowflake);
		if (member == null) {
			Seriex.get().kick_no_translation(hook(), "Please contact a maintainer... [MEMBER]");
			return true;
		}
		List<Role> roles = member.getRoles();
		if (roles.isEmpty()) {
			Seriex.get().kick_no_translation(hook(), "You are not registered in the discord server.");
			return true;
		}
		boolean hasVerifiedRole = false;
		boolean hasAnyLanguageRole = false;
		for (Role role : roles) {
			if (!hasVerifiedRole && Objects.equals(role, DiscordBot.VERIFIED_ROLE_CACHE.get(guildID))) {
				hasVerifiedRole = true;
			}
			if (!hasAnyLanguageRole && map.containsValue(role)) {
				hasAnyLanguageRole = true;
			}
		}
		// this fixes adding roles like "not a random" etc...
		boolean onlyHasVerified = hasVerifiedRole && !hasAnyLanguageRole;
		if (onlyHasVerified) {
			Seriex.get().kick_no_translation(hook(), "You have no language roles selected.");
			return true;
		}
		roles.stream().filter(map::containsValue).forEach(role -> {
			Set<Language> collect = map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), role)).map(Map.Entry::getKey).collect(Collectors.toSet());
			// maybe save this in db?
			selectedLanguages.addAll(collect);
		});

		if (selectedLanguages.isEmpty()) {
			Seriex.get().kick_no_translation(hook(), "You have no language roles selected. (!)");
			return true;
		}

		if (playerDiscord.languages == 0) {
			playerDiscord.languages = selectedLanguages.stream().mapToInt(Language::mask).sum();
			playerDiscord.update();
			Seriex.get().logger().error("PlayerDiscord had no languages for player {}", player.getName());
		}

		return false;
	}

	private void registerAttributes() {
		Reflexion.on(this)
					.findFields(FieldMatcher.newMatcher().and(field -> field.isAnnotationPresent(RegisterAttribute.class)))
					.forEach(fieldAccessor -> {
						Field member = fieldAccessor.getMember();
						fieldAccessor.<IAttributeHolder>getValue(PlayerW.this).ifSuccess(value -> {
							RegisterAttribute registerAttribute = member.getAnnotation(RegisterAttribute.class);
							attributeHoldersString.put(registerAttribute.Enum().qualifiedName, value);
							attributeHoldersEnum.put(registerAttribute.Enum(), value);
						});
					});
	}

	public boolean isInventoryEmpty() {
		Predicate<ItemStack> hasItem = item -> item != null && item.getType() != Material.AIR;
		PlayerInventory inventory = hook().getInventory();
		ItemStack[] contents = inventory.getContents();
		for (ItemStack itemStack : contents) {
			if (hasItem.test(itemStack))
				return false;
		}

		ItemStack[] armorContents = inventory.getArmorContents();
		for (ItemStack itemStack : armorContents) {
			if (hasItem.test(itemStack))
				return false;
		}

		return true;
	}

	public void teleport(PlayerW target) {
		PlayerState ourState = playerState;
		PlayerState theirState = target.playerState();

		if (ourState == PlayerState.DUEL) {
			Seriex.get().msg(player, Messages.COMMAND_TELEPORT_FAILURE_TELEPORTER_IN_DUEL);
			return;
		}

		if (theirState == PlayerState.DUEL) {
			Seriex.get().msg(player, Messages.COMMAND_TELEPORT_FAILURE_TARGET_IN_DUEL, target.attribute(NAME));
			return;
		}

		if (theirState == PlayerState.MINIGAMES) {
			if (ourState == PlayerState.MINIGAMES && !this.currentMinigame.equals(target.currentMinigame)) target.currentMinigame.onJoin(this);
			if (ourState == PlayerState.SPAWN) target.currentMinigame.onJoin(this);
		} else if (theirState == PlayerState.SPAWN && ourState == PlayerState.MINIGAMES) {
			this.currentMinigame.onLeave(this);
		}

		player.teleport(target.hook());
		Seriex.get().msg(player, Messages.COMMAND_TELEPORT_SUCCESS, target.attribute(NAME));
	}

	public void teleportToLocation(Location target) {
		PlayerState ourState = playerState;

		if (ourState == PlayerState.DUEL) {
			Seriex.get().msg(player, Messages.COMMAND_TELEPORT_FAILURE_TELEPORTER_IN_DUEL);
			return;
		}

		var senderLocation = hook().getLocation();

		var targetWorld = target.getWorld().getName();
		var currentWorld = senderLocation.getWorld().getName();

		if (!targetWorld.equals(currentWorld)) {
			Seriex.get().msg_no_translation(this, "You are in world '%s', teleport location is in world '%s'.", currentWorld, targetWorld);
			return;
		}

		player.teleport(target);
		Seriex.get().msg(player, Messages.COMMAND_TELEPORT_SUCCESS, target.toString());
	}

	public void cleanupUser(final boolean items) {
		player.setHealth(20.0);
		player.setFoodLevel(20);
		player.setSaturation(12.8F);
		player.setMaximumNoDamageTicks(20);
		new BukkitRunnable() {
			@Override
			public void run() {
				player.setFireTicks(0);
			}
		}.runTaskLater(Seriex.get().plugin(), 1L);
		player.setFallDistance(0.0f);
		player.setLevel(0);
		player.setExp(0.0F);
		player.setWalkSpeed(0.2F);
		player.getInventory().setHeldItemSlot(0);
		player.setAllowFlight(false);
		if (items) {
			player.getInventory().clear();
			player.getInventory().setArmorContents((ItemStack[]) null);
		}
		player.closeInventory();
		player.setGameMode(GameMode.SURVIVAL);
		nmsHandle().getDataWatcher().watch(9, (byte) 0); // removes arrows
		for (final PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		for (int i = 0; i < 3; i++) {
			Seriex.get().runLater(player::updateInventory, i);
		}
	}

	public EntityPlayer nmsHandle() {
		return craftPlayer().getHandle();
	}

	public CraftPlayer craftPlayer() {
		return (CraftPlayer) player;
	}

	public UUID uuid() {
		return hook().getUniqueId();
	}

	public Player hook() {
		return player;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", PlayerW.class.getSimpleName() + "[", "]")
					.add("player=" + player)
					.add("savedPlayerID=" + savedPlayerID)
					.add("attributeHoldersString=" + attributeHoldersString)
					.add("attributeHoldersEnum=" + attributeHoldersEnum)
					.add("selectedLanguage=" + selectedLanguage)
					.add("selectedLanguages=" + selectedLanguages)
					.add("prevValues=" + prevValues)
					.add("playMS=" + playMS)
					.add("loggedIn=" + loggedIn)
					.add("correctPassword=" + correctPassword)
					.add("playerState=" + playerState)
					.add("currentMinigame=" + currentMinigame)
					.add("anticheat=" + anticheat)
					.add("eventSystem=" + eventSystem)
					.add("rank=" + rank)
					.add("attacks=" + attacks)
					.add("patchBypass=" + patchBypass)
					.add("allowedToMove=" + allowedToMove)
					.add("invsee=" + invsee)
					.add("afk=" + afk)
					.add("ignored=" + ignored)
					.add("canBeInitialized=" + canBeInitialized)
					.add("nameAtr=" + nameAtr)
					.add("idAtr=" + idAtr)
					.add("tokenAtr=" + tokenAtr)
					.add("saltAtr=" + saltAtr)
					.add("passwordAtr=" + passwordAtr)
					.add("worldAtr=" + worldAtr)
					.add("tickEvent=" + tickEvent)
					.add("flagEvent=" + flagEvent)
					.toString();
	}

	public enum Attributes {
		NAME("name"),
		ID("id"),
		TOKEN("token"),
		SALT("salt"),
		PASSWORD("password"),
		WORLD("world");

		/**
		 * Qualified name is needed for {@link pisi.unitedmeows.seriex.util.placeholder.impl.PlayerPlaceHolder#compute(String attributeName)}
		 */
		final String qualifiedName;

		Attributes(String qualifiedName) {
			this.qualifiedName = qualifiedName;
		}
	}

	@Override
	public int hashCode() {
		return (savedPlayerID << 5) - savedPlayerID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		PlayerW other = (PlayerW) obj;
		return savedPlayerID == other.savedPlayerID;
	}

}
