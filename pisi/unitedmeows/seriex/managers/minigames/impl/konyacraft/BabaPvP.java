package pisi.unitedmeows.seriex.managers.minigames.impl.konyacraft;

import static org.bukkit.Material.*;
import static org.bukkit.enchantments.Enchantment.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class BabaPvP extends Minigame {
	private static final Kit KIT = Kit.createKit(
				ItemBuilder.of(IRON_SWORD).build(),
				new ItemStack[] {
							ItemBuilder.of(GLASS).enchantment(PROTECTION_ENVIRONMENTAL).build(),
							ItemBuilder.of(IRON_CHESTPLATE).build(),
							ItemBuilder.of(IRON_LEGGINGS).build(),
							ItemBuilder.of(IRON_BOOTS).build()
				},
				ItemBuilder.of(BOW).max_enchantment(ARROW_KNOCKBACK).enchantment(ARROW_INFINITE).build(),
				ItemBuilder.of(ARROW).amount(1).build());

	@Override
	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
		super.onPlayerInteract(event);
		if (!isInGame(event.getPlayer()))
			return;
		Entity rightClicked = event.getRightClicked();
		if (rightClicked instanceof Player && "Right click to get the kit!".equals(rightClicked.getName())) {
			KIT.give(event.getPlayer());
		}
	}

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
	public void onPlayerDeath(PlayerDeathEvent event) {
		super.onPlayerDeath(event);
		Player player = event.getEntity();	
		if (!isInGame(player))
			return;
		
		event.getDrops().clear();
		player.setHealth(player.getMaxHealth());
		player.teleport(this.spawnLocation);

		Player killer = player.getKiller();
		if (!isInGame(killer))
			return;

		killer.setHealth(20);
		killer.setNoDamageTicks(Math.max(killer.getNoDamageTicks(), 15));
		calculateKillstreak(killer, KillstreakType.BASIC);
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
