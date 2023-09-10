package pisi.unitedmeows.seriex.commands;

import dev.rollczi.litecommands.argument.option.Opt;
import org.bukkit.Material;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.route.Route;
import org.bukkit.inventory.ItemStack;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.HashMap;
import java.util.Locale;

@Route(name = "give" , aliases = {
			"i", "item"
})
public class GiveCommand {

	@Execute(min = 2, max = 3)
	public void give(PlayerW sender, @Arg Material material, @Arg Integer amount, @Opt Option<Integer> durability) {
		Seriex seriex = Seriex.get();

		handleGive(sender, sender, material, amount, durability.orElseGet(0));
	}

	@Execute(min = 3, max = 4)
	public void give(PlayerW sender, @Arg PlayerW other, @Arg Material material, @Arg Integer amount, @Opt Option<Integer> durability) {
		Seriex seriex = Seriex.get();
		if (!sender.rank().operator()) {
			seriex.msg(sender, Messages.COMMAND_NOT_ALLOWED, Ranks.MAINTAINER.internalName());
			return;
		}

		if(sender.disallowCommand()) return;

		handleGive(sender, other, material, amount, durability.orElseGet(0));
	}

	private void handleGive(PlayerW sender, PlayerW receiver, Material material, Integer stackAmount, Integer durability) {
		Seriex seriex = Seriex.get();

		int maximumStackSize = ItemBuilder.of(material).build().getMaxStackSize();
		int fixedStackAmount = sender.rank().operator() ? Math.min(64, stackAmount) : Math.min(maximumStackSize, Math.max(1, stackAmount));
		ItemStack builtItem = ItemBuilder.of(material).amount(fixedStackAmount).durability(durability).build();
		HashMap<Integer, ItemStack> remaining = receiver.hook().getInventory().addItem(builtItem);
		if (!remaining.isEmpty()) {
			seriex.msg(sender, Messages.COMMAND_GIVE_INVENTORY_FULL);
			seriex.msg(receiver, Messages.COMMAND_GIVE_INVENTORY_FULL);
		} else {
			seriex.msg(sender, Messages.COMMAND_GIVE_ADDED_TO_INVENTORY, material.name().toUpperCase(Locale.ENGLISH), durability, fixedStackAmount);
			seriex.msg(receiver, Messages.COMMAND_GIVE_ADDED_TO_INVENTORY, material.name().toUpperCase(Locale.ENGLISH), durability, fixedStackAmount);
		}
		receiver.hook().updateInventory();
	}
}
