package pisi.unitedmeows.seriex.util.wrapper;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerSettings;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.minigames.Minigame;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.placeholder.annotations.RegisterAttribute;
import pisi.unitedmeows.seriex.util.placeholder.api.IAttributeHolder;
import pisi.unitedmeows.yystal.clazz.HookClass;
import pisi.unitedmeows.yystal.hook.YString;
import pisi.unitedmeows.yystal.utils.CoID;

public class PlayerW extends HookClass<Player> {
	private static final Pattern pattern = Pattern.compile("Player[0-9]{1,4}");
	private StructPlayer playerInfo;
	private StructPlayerSettings playerSettings;
	private StructPlayerDiscord playerDiscord;
	private HashMap<String, IAttributeHolder> attributeHolders;
	@RegisterAttribute(name = "name")
	private IAttributeHolder nameAtr = () -> hooked.getName();
	@RegisterAttribute(name = "id")
	private IAttributeHolder idAtr = () -> String.valueOf(playerInfo.player_id);
	@RegisterAttribute(name = "token")
	private IAttributeHolder tokenAtr = () -> playerInfo.token;
	@RegisterAttribute(name = "salt")
	public IAttributeHolder saltAtr = () -> playerInfo.salt;
	@RegisterAttribute(name = "password")
	public IAttributeHolder passwordAtr = () -> playerInfo.password;
	@RegisterAttribute(name = "world")
	private IAttributeHolder worldAtr = () -> hooked.getWorld().getName();
	// the language text will be translated into
	private Language selectedLanguage = Language.ENGLISH;
	// this is for discord
	private Set<Language> selectedLanguages;
	public MovementValuesWrapper prevValues;
	public long playMS;
	public boolean loggedIn;
	public PlayerState playerState;
	public Minigame currentMinigame;

	public boolean has2FA() {
		return playerInfo != null && playerInfo.gAuth != null && !"-".equals(playerInfo.gAuth);
	}

	public PlayerW(final Player _player) {
		// field init
		hooked = _player;
		final String name = hooked.getName();
		playerInfo = Seriex.get().database().getPlayer(_player.getName());
		DiscordConfig discordConfig = (DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD);
		if (playerInfo == null) {
			Seriex.logger().warn("Database values of player %s was missing! (maybe not verified?)", name);
			Seriex.get().kick(_player, "Please verify on discord!\n%s%s", ChatColor.BLUE, discordConfig.INVITE_LINK.value());
			return; // bug fix
		}
		if (selectedLanguages == null) {
			selectedLanguages = EnumSet.noneOf(Language.class);
		}
		playerDiscord = Seriex.get().database().getPlayerDiscord(_player.getName());
		if (playerDiscord == null) {
			// todo remove
			Seriex.get().kick(getHooked(), "Please contact an maintainer... [0xDISCORD]");
			return;
		}
		DiscordBot discordBot = Seriex.get().discordBot();
		String guildID = discordConfig.ID_GUILD.value();
		Map<Language, Role> map = DiscordBot.roleCache.get(guildID);
		Guild guildById = discordBot.JDA().getGuildById(guildID);
		UserSnowflake snowflake = UserSnowflake.fromId(playerDiscord.discord_id);
		Member member = null;
		if (guildById != null) {
			member = guildById.getMember(snowflake);
		} else {
			Seriex.logger().fatal("Couldn't find guild by the id %s", guildID);
			return;
		}
		if (member == null) {
			Seriex.get().kick(getHooked(), "Please contact an maintainer... [0xMEMBER]");
			return;
		}
		List<Role> roles = member.getRoles();
		if (roles.isEmpty()) {
			Seriex.get().kick(getHooked(), "You have no roles.");
			return;
		}
		boolean hasVerifiedRole = false;
		boolean hasAnyLanguageRole = false;
		for (int i = 0; i < roles.size(); i++) {
			Role role = roles.get(i);
			if (!hasVerifiedRole && role == DiscordBot.verifiedRole.get(guildID)) {
				hasVerifiedRole = true;
			}
			if (!hasAnyLanguageRole && map.containsValue(role)) {
				hasAnyLanguageRole = true;
			}
		}
		// this fixes adding roles like "not a random" etc...
		boolean onlyHasVerified = hasVerifiedRole && !hasAnyLanguageRole;
		if (onlyHasVerified) {
			Seriex.get().kick(getHooked(), "You have no language roles selected.");
			return;
		}
		roles.stream().filter(map::containsValue).forEach(role -> {
			Set<Language> collect = map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), role)).map(Map.Entry::getKey).collect(Collectors.toSet());
			// maybe save this in db?
			selectedLanguages.addAll(collect);
		});
		/* tries to retrieve player's settings if not exists creates new one */
		playerSettings = Seriex.get().database().getPlayerSettings(playerInfo.player_id);
		if (playerSettings == null) {
			Seriex.logger().warn("The player %s does not have Settings row on database (maybe first login?)", name);
			playerSettings = new StructPlayerSettings();
			playerSettings.player_id = playerInfo.player_id;
			playerSettings.create();
		}
		attributeHolders = new HashMap<>();
		registerAttributes();
		playerState = PlayerState.SPAWN;
	}

	private void registerAttributes() {
		Field[] declaredFields = getClass().getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			if (field.getType() == IAttributeHolder.class) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(RegisterAttribute.class)) {
					final RegisterAttribute registerAttribute = field.getAnnotation(RegisterAttribute.class);
					try {
						attributeHolders.put(registerAttribute.name(), (IAttributeHolder) field.get(this));
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public String attribute(String name) {
		IAttributeHolder attributeHolder = attributeHolders.getOrDefault(name, null);
		if (attributeHolder != null) return attributeHolder.compute();
		return YString.EMPTY_R;
	}

	private String generateUserToken(final String name) {
		return CoID.generate().toString();
	}

	public void cleanupUser(final boolean items) {
		hooked.setHealth(20.0);
		hooked.setFoodLevel(20);
		hooked.setSaturation(12.8F);
		hooked.setMaximumNoDamageTicks(20);
		new BukkitRunnable() {
			@Override
			public void run() {
				hooked.setFireTicks(0);
			}
		}.runTaskLater(Seriex.get(), 1L);
		hooked.setFallDistance(0.0f);
		hooked.setLevel(0);
		hooked.setExp(0.0F);
		hooked.setWalkSpeed(0.2F);
		hooked.getInventory().setHeldItemSlot(0);
		hooked.setAllowFlight(false);
		if (items) {
			hooked.getInventory().clear();
			hooked.getInventory().setArmorContents((ItemStack[]) null);
		}
		hooked.closeInventory();
		hooked.setGameMode(GameMode.SURVIVAL);
		((CraftPlayer) hooked).getHandle().getDataWatcher().watch(9, (Object) (byte) 0); // removes arrows
		for (final PotionEffect effect : hooked.getActivePotionEffects()) {
			hooked.removePotionEffect(effect.getType());
		}
		for (int i = -1; i < 3; i++) { // dont question pls
			if (i == 0) {
				hooked.updateInventory();
			} else if (i > 0) {
				new BukkitRunnable() {
					@Override
					public void run() {
						hooked.updateInventory();
					}
				}.runTaskLater(Seriex.get(), i);
			}
		}
	}

	public final void denyMovement() {
		Player player = getHooked();
		if (prevValues == null) {
			prevValues = new MovementValuesWrapper(player.getWalkSpeed(), player.getFlySpeed(), player.getFoodLevel(), player.isSprinting());
		} else {
			Seriex.logger().fatal("Already denied movement for %s!", player.getName());
		}
		player.setWalkSpeed(0.0F);
		player.setFlySpeed(0.0F);
		player.setFoodLevel(0);
		player.setSprinting(false);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));
	}

	public final void allowMovement() {
		Player player = getHooked();
		if (prevValues == null) {
			Seriex.logger().fatal("Movement was not denied for %s!", player.getName());
			return;
		}
		if (Math.abs(prevValues.walkSpeed) <= 1E-4F) {
			prevValues.walkSpeed = 0.2F;
			prevValues.flySpeed = 0.1F;
			prevValues.foodLevel = 20;
			prevValues.isSprinting = true;
			Seriex.logger().fatal("Movement values was broken for %s!", player.getName());
		}
		player.setWalkSpeed(prevValues.walkSpeed);
		player.setFlySpeed(prevValues.flySpeed);
		player.setFoodLevel(prevValues.foodLevel);
		player.setSprinting(prevValues.isSprinting);
		player.removePotionEffect(PotionEffectType.JUMP);
		prevValues = null;
	}

	public boolean isGuest() {
		return isGuest(hooked.getName());
	}

	public static boolean isGuest(String name) {
		return pattern.matcher(name).matches();
	}

	public String getIP() {
		if ("slowcheet4h".equals(getHooked().getName())) return "loki";
		return hooked.getAddress().getHostName();
	}

	/**
	 * @apiNote Input is an IP address.
	 *          <br>
	 *          btw fuck regex LOL
	 */
	public String getMaskedIP(final String input) {
		if (!input.contains(".")) return input; // ;((( my ip -slowcheet4h
		final String maskChar = "-";
		final String ip = input.replace('.', ':');
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

	public StructPlayer playerInfo() {
		return playerInfo;
	}

	public StructPlayerSettings playerSettings() {
		return playerSettings;
	}

	public StructPlayerDiscord playerDiscord() {
		return playerDiscord;
	}

	public Language selectedLanguage() {
		return selectedLanguage;
	}

	@Override
	public Player getHooked() {
		return super.getHooked();
	}
}
