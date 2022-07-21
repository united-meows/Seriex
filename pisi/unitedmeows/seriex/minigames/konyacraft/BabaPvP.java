package pisi.unitedmeows.seriex.minigames.konyacraft;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import pisi.unitedmeows.seriex.minigames.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class BabaPvP extends Minigame {
    {
        //TODO: CONFIG NOOOOOOOOOOOOOOOB
        name = "BabaPvP";
        worldName = "ATATÜRK";
        spawnLocation = null;
        allowedLimit = null;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            event.getEntity().getKiller().setHealth(20);
            event.getEntity().getKiller().setNoDamageTicks(Math.max(event.getEntity().getKiller().getNoDamageTicks(), 15));
        }
        event.getEntity().spigot().respawn();
    }

    @Override
    public void onJoin(PlayerW playerW) {
        super.onJoin(playerW);
        playerW.getHooked().setItemInHand(ItemBuilder.of(Material.IRON_SWORD).build());
        playerW.getHooked().getInventory().setHelmet(ItemBuilder.of(Material.GLASS).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build());
        playerW.getHooked().getInventory().setChestplate(ItemBuilder.of(Material.IRON_CHESTPLATE).build());
        playerW.getHooked().getInventory().setLeggings(ItemBuilder.of(Material.IRON_LEGGINGS).build());
        playerW.getHooked().getInventory().setBoots(ItemBuilder.of(Material.IRON_BOOTS).build());
        playerW.getHooked().getInventory().setItem(38, ItemBuilder.of(Material.BOW).enchantment(Enchantment.ARROW_KNOCKBACK, 2).enchantment(Enchantment.ARROW_INFINITE).build());
    }

    @Override
    public void onLeave(PlayerW playerW) {
        super.onLeave(playerW);
    }
}
