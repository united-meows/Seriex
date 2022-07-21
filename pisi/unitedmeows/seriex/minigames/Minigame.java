package pisi.unitedmeows.seriex.minigames;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.HashMap;
import java.util.List;

public class Minigame implements Listener {
    protected String name, worldName;
    protected Location spawnLocation;
    protected AxisBB allowedLimit;
    protected List<PlayerW> playersInMinigame = new GlueList<>();
    private HashMap<PlayerW, PlayerInventory> lastInventories = new HashMap<>();

    public void onJoin(PlayerW playerW) {
        lastInventories.put(playerW, playerW.getHooked().getInventory());

        World world = Bukkit.getWorld(worldName);
        spawnLocation.setWorld(world);
        world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
        playerW.getHooked().setBedSpawnLocation(spawnLocation);


        playersInMinigame.add(playerW);
        playerW.currentMinigame = this;
    }


    //TODO: onGuiSwitch pls use onLeave for old minigame (if present) then use onJoin for clicked minigame
    //TODO: on /spawn pls use onLeave playerW.currentMinigame (if present)
    public void onLeave(PlayerW playerW) {
        playerW.getHooked().getInventory().setContents(lastInventories.get(playerW).getContents());
        lastInventories.remove(playerW);

        World world = Bukkit.getWorld(((ServerConfig)Seriex.get().fileManager().getConfig(FileManager.SERVER)).WORLD_NAME.value());
        Location loc = ((ServerConfig)Seriex.get().fileManager().getConfig(FileManager.SERVER)).getWorldSpawn();
        world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        playerW.getHooked().setBedSpawnLocation(loc);


        playersInMinigame.remove(playerW);
        playerW.currentMinigame = null;
    }
}
