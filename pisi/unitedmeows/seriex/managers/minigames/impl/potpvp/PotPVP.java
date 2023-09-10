package pisi.unitedmeows.seriex.managers.minigames.impl.potpvp;

import static org.bukkit.Material.*;
import static org.bukkit.enchantments.Enchantment.DAMAGE_ALL;
import static org.bukkit.enchantments.Enchantment.DURABILITY;
import static pisi.unitedmeows.seriex.managers.minigames.impl.Minigame.KillstreakType.POTPVP_GAPPLE;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class PotPVP extends Minigame {
	private static final PotionEffect SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, 2147483647, 1);
	private static final Kit KIT = Kit.createKitAndFill(
				ItemBuilder.of(IRON_SWORD).enchantment(DAMAGE_ALL).enchantment(DURABILITY, 2173).build(),
				new ItemStack[] {
							ItemBuilder.of(IRON_HELMET).build(),
							ItemBuilder.of(IRON_CHESTPLATE).build(),
							ItemBuilder.of(IRON_LEGGINGS).build(),
							ItemBuilder.of(IRON_BOOTS).build()
				}, ItemBuilder.potion(PotionType.INSTANT_HEAL, 2, true, false).build());

	public PotPVP() {}

	@Override
	public void onHungerChange(FoodLevelChangeEvent event) {
		super.onHungerChange(event);
		HumanEntity entity = event.getEntity();
		if (entity instanceof Player player) {
			if (!isInGame(player))
				return;
			
			event.setFoodLevel(20);
		}
	}

	@Override
	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
		super.onPlayerInteract(event);
		Player player = event.getPlayer();
		if (!isInGame(player))
			return;

		Entity rightClicked = event.getRightClicked();
		if (rightClicked instanceof Player && "Right click to get the kit!".equals(rightClicked.getName())) {
			KIT.give(player);
		}
	}

	@Override
	public void onPlayerDeath(PlayerDeathEvent event) {
		super.onPlayerDeath(event);
		Player player = event.getEntity();
		if (!isInGame(player))
			return;

		event.getDrops().clear();

		Player killer = player.getKiller();
		if (!isInGame(killer))
			return;

		calculateKillstreak(killer, POTPVP_GAPPLE);
	}

	@Override
	public void onJoin(PlayerW playerW) {
		super.onJoin(playerW);
		KIT.giveIf(playerW.hook(), playerW::isInventoryEmpty);
		Anticheat.NCP_FC.convert(playerW);
	}

	@Override
	public void onRespawn(PlayerW playerW) {
		super.onRespawn(playerW);
		KIT.clearAndGive(playerW.hook());
		addPotionEffect(playerW.hook());
	}

	private void addPotionEffect(Player player) {
		Seriex.get().runLater(() -> {
			player.addPotionEffect(SPEED_EFFECT);
		}, 2);
	}

	@Override
	public Kit kit() {
		return KIT;
	}

	@Override
	public void onLeave(PlayerW playerW) {
		super.onLeave(playerW);
	}
}
