package pisi.unitedmeows.seriex.managers.minigames.impl.survival;

import static java.lang.Integer.MAX_VALUE;
import static org.bukkit.Material.*;
import static org.bukkit.enchantments.Enchantment.*;
import static org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE;
import static org.bukkit.potion.PotionEffectType.SPEED;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class Survival extends Minigame {
	private static final Kit KIT = Kit.createKit(
				ItemBuilder.of(DIAMOND_SWORD).max_enchantment(DAMAGE_ALL).max_enchantment(DURABILITY).build(),
				new ItemStack[] {
							ItemBuilder.of(DIAMOND_HELMET).max_enchantment(PROTECTION_ENVIRONMENTAL).max_enchantment(DURABILITY).build(),
							ItemBuilder.of(DIAMOND_CHESTPLATE).max_enchantment(PROTECTION_ENVIRONMENTAL).max_enchantment(DURABILITY).build(),
							ItemBuilder.of(DIAMOND_LEGGINGS).max_enchantment(PROTECTION_ENVIRONMENTAL).max_enchantment(DURABILITY).build(),
							ItemBuilder.of(DIAMOND_BOOTS).max_enchantment(PROTECTION_ENVIRONMENTAL).max_enchantment(DURABILITY).build()
				},
				ItemBuilder.of(POTION).durability(8229).amount(16).build(),
				ItemBuilder.of(GOLDEN_APPLE).name("&eGolden Apple").amount(3).unbreakable(true).build()).potionEffects(
							new PotionEffect(SPEED, MAX_VALUE, 1),
							new PotionEffect(INCREASE_DAMAGE, MAX_VALUE, 1));

	public Survival() {}

	@Override
	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
		super.onPlayerInteract(event);
		if (!isInGame(event.getPlayer())) return;
		Entity rightClicked = event.getRightClicked();
		if (rightClicked instanceof Player && "Right click to get the kit!".equals(rightClicked.getName())) {
			KIT.give(event.getPlayer());
		}
	}

	@Override
	public void onPlayerItemConsume(final PlayerItemConsumeEvent event) {
		super.onPlayerItemConsume(event);
		if (!isInGame(event.getPlayer()))
			return;
		boolean isGoldenAppleAndBreakable =  // i fear npe
					event.getItem() != null
								&& event.getItem().getType() != null
								&& event.getItem().getType() == GOLDEN_APPLE
								&& event.getItem().getItemMeta() != null
								&& event.getItem().getItemMeta().spigot() != null
								&& !event.getItem().getItemMeta().spigot().isUnbreakable();
		if (isGoldenAppleAndBreakable) {
			ItemStack item = new ItemStack(AIR);
			event.setItem(item);
			event.getPlayer().setItemInHand(item);
		}
	}

	@Override
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		super.onPlayerDropItem(event);
		if (!isInGame(event.getPlayer()))
			return;
		Item itemDrop = event.getItemDrop();
		if (itemDrop == null)
			return;
		ItemStack itemStack = itemDrop.getItemStack();
		if (itemStack == null)
			return;
		final Material drop = itemStack.getType();

		if (drop == GLASS_BOTTLE) {
			itemDrop.remove();
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

		Player killer = player.getKiller();
		if (!isInGame(killer))
			return;

		calculateKillstreak(killer, KillstreakType.BASIC);
	}

	@Override
	public void onJoin(PlayerW playerW) {
		this.onJoinRunnable = () -> {
			KIT.clearAndGive(playerW.hook());
			Anticheat.NCP_MINEZ.convert(playerW);
		};

		super.onJoin(playerW);
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
}
