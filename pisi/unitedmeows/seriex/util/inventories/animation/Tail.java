package pisi.unitedmeows.seriex.util.inventories.animation;

import static org.bukkit.Material.STAINED_GLASS_PANE;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.SlotPos;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;

/**
 * @author IpanaDev
 */
public class Tail {
	public TailObject[] tails;
	public int tailAmount;

	private int maxRows, maxColumns;

	public Tail() {
		this(
					8,
					2,
					ItemBuilder.of(STAINED_GLASS_PANE).durability(15).build(),
					ItemBuilder.of(STAINED_GLASS_PANE).durability(7).build(),
					ItemBuilder.of(STAINED_GLASS_PANE).durability(8).build(),
					ItemBuilder.of(STAINED_GLASS_PANE).durability(0).build()

		);
	}

	public Tail(int maxRows, int maxColumns) {
		this(
					maxRows,
					maxColumns,
					ItemBuilder.of(STAINED_GLASS_PANE).durability(15).build(),
					ItemBuilder.of(STAINED_GLASS_PANE).durability(7).build(),
					ItemBuilder.of(STAINED_GLASS_PANE).durability(8).build(),
					ItemBuilder.of(STAINED_GLASS_PANE).durability(0).build()

		);
	}

	public Tail(int maxRows, int maxColumns, ItemStack... stacks) {
		this.maxRows = maxRows;
		this.maxColumns = maxColumns;
		this.tailAmount = stacks.length;
		this.tails = new TailObject[tailAmount];
		for (int i = 0; i < tailAmount; i++) {
			TailObject tail = new TailObject();
			tail.row = tailAmount - i;
			tail.column = 0;
			tail.stack = stacks[i];
			this.tails[i] = tail;
		}
	}

	public void update(InventoryContents contents) {
		int[] toBeClearedPos = new int[] {
					tails[tailAmount - 1].row,
					tails[tailAmount - 1].column,
		};

		for (int i = tails.length - 1; i >= 1; i--) {
			TailObject tailObject = tails[i];
			TailObject previous = tails[i - 1];
			tailObject.column = previous.column;
			tailObject.row = previous.row;
		}

		TailObject head = tails[0];
		if (head.column == 0 && head.row < maxRows) {
			head.row++;
		} else if (head.row == maxRows && head.column < maxColumns) {
			head.column++;
		} else if (head.column == maxColumns && head.row > 0) {
			head.row--;
		} else if (head.row == 0 && head.column > 0) {
			head.column--;
		}

		for (TailObject tailObject : tails) {
			contents.set(SlotPos.of(tailObject.column, tailObject.row), ClickableItem.empty(tailObject.stack));
		}
		contents.set(toBeClearedPos[1], toBeClearedPos[0], ClickableItem.empty(ItemBuilder.of(Material.AIR).build()));
	}
}
