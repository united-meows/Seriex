package pisi.unitedmeows.seriex.managers.area.impl.movement;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.impl.Area;
import pisi.unitedmeows.seriex.managers.area.impl.AreaBase;
import pisi.unitedmeows.seriex.managers.area.impl.AreaData;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.ItemPointer;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;

@AreaData(base = AreaBase.SCAFFOLD)
public class ScaffoldArea extends Area {
	public ScaffoldArea(String cfgName) {
		super(cfgName);
	}

	private Map<Player, ItemStack> previousItem;

	@Override
	public void start() {
		add_ptr("scaffold_item", new ItemPointer(ItemBuilder.of(Material.WOOL).durability(6).amount(8).build()));
		previousItem = new HashMap<>();
	}

	@Override
	public void enter(Player player) {
		previousItem.put(player, player.getInventory().getItem(8));
	}

	private void restoreItem(Player player) {
		ItemStack item = previousItem.get(player);
		player.getInventory().setItem(8, item);
	}

	@Override
	public void leave(Player player) {
		this.restoreItem(player);
	}

	@Override
	public void disconnect(Player player) {
		this.restoreItem(player);
	}

	@Override
	public boolean move(Player player) {
		ItemPointer pointer = get_ptr("scaffold_item");
		player.getInventory().setItem(8, pointer.data());
		return false;
	}

	private boolean isBlockSafe(Block placed) {
		ItemPointer ptr = get_ptr("scaffold_item");
		return placed != null && ptr.data().getType() == placed.getType();
	}

	@Override
	public boolean block_place(Player placedBy, Block placed) {
		boolean isPlacedInside = limits.intersectsWith(placed.getLocation().add(0.5, 0.5, 0.5));
		if (!isInside(placedBy) && isPlacedInside)
			return true;

		if(!isBlockSafe(placed))
			return true;
		
		if (isPlacedInside)
			Seriex.get().runLater(() -> placed.setType(Material.AIR), 50L);

		return false;
	}

	@Override
	public boolean isConfigured() { 
		return true; 
	}
}
