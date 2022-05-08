package pisi.unitedmeows.seriex.util.wrapper;

import static java.nio.charset.StandardCharsets.*;

import java.io.File;
import java.util.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.StructPlayerW;
import pisi.unitedmeows.seriex.util.config.impl.server.PlayerConfig;
import pisi.unitedmeows.yystal.clazz.HookClass;
import pisi.unitedmeows.yystal.utils.Pair;

public class PlayerW extends HookClass<Player> {

	private final StructPlayerW playerConfig;

	public PlayerW(final Player _player) {
		hooked = _player;
		final String name = hooked.getName();

		playerConfig = Seriex.get().database().getPlayerW(name);
		if (playerConfig.isNew) {
			Seriex.get().logger().info("Creating the database entries for %s", hooked.getName());
		} else {
			Seriex.get().logger().info("%s isnt new!", hooked.getName());
		}
	}

	private String generateUserToken(final String name) {
		final byte[] bytes = UUID.nameUUIDFromBytes(name.getBytes(UTF_8)).toString().getBytes(UTF_8);
		return "2173" + DigestUtils.sha256Hex(bytes);
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

	/**
	 * @apiNote Input is an IP address.
	 *          <br>
	 *          btw fuck regex LOL
	 */
	public String getMaskedIP(final String input) {
		final String maskChar = "-";
		final String ip = input.replace('.', ':');
		final String[] splitIp = ip.split(":");
		final StringBuilder finalIp = new StringBuilder();
		boolean allah = false;
		for (int i = 0; i < splitIp.length; i++) {
			if (i < splitIp.length - 2) { // example based on ip = 127.0.0.1 masks half of the ip 127.0.x.x
													// if you want to mask to the first dot
													// make the 2 -> 3
													// after that ip`s will look like 127.x.x.x
				finalIp.append(splitIp[i] + ".");
			} else {
				for (int j = 0; j < splitIp[i].length(); j++) {
					if (j == 0) {
						if (!allah) {
							finalIp.append(maskChar);
							allah = true;
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
}
