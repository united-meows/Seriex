package pisi.unitedmeows.seriex.commands;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.argument.option.Opt;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.Create;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;


@Route(name = "potion")
public class PotionCommand {
	@Execute(required = 5)
	public void potion(PlayerW sender,
				@Arg PotionType potionType,
				@Arg Integer level,
				@Arg Boolean splash,
				@Arg Boolean extended,
				@Arg Integer amount) {
		Seriex seriex = Seriex.get();

		if (sender.disallowCommand()) return;


		int fixedPotionLevel = Math.min(2, Math.max(1, level));
		List<ItemStack> createdItems = Create.create(new ArrayList<ItemStack>(), list -> {
			IntStream.range(0, amount).forEach(integer -> {
				list.add(ItemBuilder.potion(potionType, fixedPotionLevel, splash, extended).build());
			});
		});
		HashMap<Integer, ItemStack> remaining = sender.hook().getInventory().addItem(createdItems.toArray(ItemStack[]::new));
		if (!remaining.isEmpty()) {
			seriex.msg(sender, Messages.COMMAND_POTION_INVENTORY_FULL, remaining.size());
		} else {
			seriex.msg(sender, Messages.COMMAND_POTION_ADDED_TO_INVENTORY, amount, potionType.name().toUpperCase(Locale.ENGLISH));
		}
		sender.hook().updateInventory();
	}


	@Execute(required = 4)
	public void potion(PlayerW sender,
				@Arg PotionType potionType,
				@Arg Integer level,
				@Arg Boolean splash,
				@Arg Boolean extended) {
		Seriex seriex = Seriex.get();

		if (sender.disallowCommand()) return;


		int fixedPotionLevel = Math.min(2, Math.max(1, level));
		HashMap<Integer, ItemStack> remaining = sender.hook().getInventory().addItem(ItemBuilder.potion(potionType, fixedPotionLevel, splash, extended).build());
		if (!remaining.isEmpty()) {
			seriex.msg(sender, Messages.COMMAND_POTION_INVENTORY_FULL, remaining.size());
		} else {
			seriex.msg(sender, Messages.COMMAND_POTION_ADDED_TO_INVENTORY, 1, potionType.name().toUpperCase(Locale.ENGLISH));
		}
		sender.hook().updateInventory();
	}

	@Execute(required = 3)
	public void potion(PlayerW sender,
				@Arg PotionType potionType,
				@Arg Integer level,
				@Arg Boolean splash) {
		Seriex seriex = Seriex.get();

		if (sender.disallowCommand()) return;


		int fixedPotionLevel = Math.min(2, Math.max(1, level));
		HashMap<Integer, ItemStack> remaining = sender.hook().getInventory().addItem(ItemBuilder.potion(potionType, fixedPotionLevel, splash, false).build());
		if (!remaining.isEmpty()) {
			seriex.msg(sender, Messages.COMMAND_POTION_INVENTORY_FULL, remaining.size());
		} else {
			seriex.msg(sender, Messages.COMMAND_POTION_ADDED_TO_INVENTORY, 1, potionType.name().toUpperCase(Locale.ENGLISH));
		}
		sender.hook().updateInventory();
	}

	@Execute(required = 2)
	public void potion(PlayerW sender,
				@Arg PotionType potionType,
				@Arg Integer level) {
		Seriex seriex = Seriex.get();

		if (sender.disallowCommand()) return;


		int fixedPotionLevel = Math.min(2, Math.max(1, level));
		HashMap<Integer, ItemStack> remaining = sender.hook().getInventory().addItem(ItemBuilder.potion(potionType, fixedPotionLevel, false, false).build());
		if (!remaining.isEmpty()) {
			seriex.msg(sender, Messages.COMMAND_POTION_INVENTORY_FULL, remaining.size());
		} else {
			seriex.msg(sender, Messages.COMMAND_POTION_ADDED_TO_INVENTORY, 1, potionType.name().toUpperCase(Locale.ENGLISH));
		}
		sender.hook().updateInventory();
	}
}
