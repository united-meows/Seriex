package pisi.unitedmeows.seriex.util.wrapper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerSettings;
import pisi.unitedmeows.seriex.discord.DiscordBot;
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
	private Set<Language> selectedLanguages;
	@RegisterAttribute(name = "name")
	private IAttributeHolder nameAtr = hooked::getName;
	@RegisterAttribute(name = "id")
	private IAttributeHolder idAtr = () -> String.valueOf(playerInfo.player_id);
	@RegisterAttribute(name = "token")
	private IAttributeHolder tokenAtr = () -> playerInfo.token;
	@RegisterAttribute(name = "salt")
	public IAttributeHolder saltAtr = () -> playerInfo.salt;
	@RegisterAttribute(name = "password")
	public IAttributeHolder passwordAtr = () -> playerInfo.password;
	@RegisterAttribute(name = "world")
	private IAttributeHolder worldAtr = getHooked().getWorld()::getName;
	private Language selectedLanguage = Language.ENGLISH;

	public PlayerW(final Player _player) {
		hooked = _player;
		final String name = hooked.getName();
		playerInfo = Seriex.get().database().getPlayer(_player.getName());
		if (playerInfo == null) {
			Seriex.logger().warn("Database values of player %s was missing! (maybe not verified?)", name);
			_player.kickPlayer(ChatColor.YELLOW + "Please verify :DDDD or your db values are corrupted" /* @ghost make this cool */);
			return; // bug fix
		}
		if (selectedLanguages == null) {
			selectedLanguages = new HashSet<>();
		}
		DiscordConfig discordConfig = (DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD);
		playerDiscord = Seriex.get().database().getPlayerDiscord(_player.getName());
		DiscordBot discordBot = Seriex.get().discordBot();
		String guildID = discordConfig.ID_GUILD.value();
		Map<Language, Role> map = discordBot.roleCache.get(guildID);
		discordBot.JDA().getGuildById(guildID).getMember(UserSnowflake.fromId(playerDiscord.discord_id)).getRoles().stream().filter(map::containsValue).forEach(role -> {
			Set<Language> collect = map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), role)).map(Map.Entry::getKey).collect(Collectors.toSet());
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
	}

	private void registerAttributes() {
		for (Field field : getClass().getDeclaredFields()) {
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

	public boolean isGuest() {
		return pattern.matcher(hooked.getName()).matches();
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
		if ("loki".equals(input)) return "loki"; // ;((( my ip -slowcheet4h
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
				finalIp.append(splitIp[i] + ".");
			} else {
				for (int j = 0; j < splitIp[i].length(); j++) {
					if (j == 0) {
						if (!first) {
							finalIp.append(maskChar);
							first = true;
						} else {
							finalIp.append("." + maskChar);
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
