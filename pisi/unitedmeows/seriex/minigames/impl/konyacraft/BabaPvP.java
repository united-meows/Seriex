package pisi.unitedmeows.seriex.minigames.impl.konyacraft;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import pisi.unitedmeows.seriex.minigames.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class BabaPvP extends Minigame {
	private static final Kit KIT = new Kit(ItemBuilder.of(Material.IRON_SWORD).build(), new ItemStack[] {
		ItemBuilder.of(Material.GLASS).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(), ItemBuilder.of(Material.IRON_CHESTPLATE).build(), ItemBuilder.of(Material.IRON_LEGGINGS).build(),
		ItemBuilder.of(Material.IRON_BOOTS).build()
	}, ItemBuilder.of(Material.BOW).enchantment(Enchantment.ARROW_KNOCKBACK, 2).enchantment(Enchantment.ARROW_INFINITE).build(), ItemBuilder.of(Material.ARROW).amount(1).build());

	@EventHandler
	public void onPlayerRightClickNPC(PlayerInteractAtEntityEvent event) {
		if (!isInGame(event.getPlayer())) return;
		Entity rightClicked = event.getRightClicked();
		if (rightClicked instanceof Player && "Right click to get the kit!".equals(rightClicked.getName())) {
			KIT.give(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!isInGame(player)) return;
		Player killer = player.getKiller();
		if (!isInGame(killer)) return;
		if (killer != null) {
			killer.setHealth(20);
			killer.setNoDamageTicks(Math.max(killer.getNoDamageTicks(), 15));
		}
		player.spigot().respawn();
	}

	@Override
	public void onJoin(PlayerW playerW) {
		super.onJoin(playerW);
		KIT.give(playerW.getHooked());
	}

	@Override
	public void onLeave(PlayerW playerW) {
		super.onLeave(playerW);
	}
}
