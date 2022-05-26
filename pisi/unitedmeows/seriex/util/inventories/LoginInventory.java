package pisi.unitedmeows.seriex.util.inventories;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.wesjd.anvilgui.AnvilGUI;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class LoginInventory {
	public static void open(PlayerW _w) {
		ItemStack questionMark = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) questionMark.getItemMeta();
		meta.setOwner("MHF_Question");
		questionMark.setItemMeta(meta);
		ItemStack builden = ItemBuilder.of(questionMark).name("&dForgot your password?").lore("Open a ticket on discord using the #ticket channel.")
					.build();
		// @DISABLE_FORMATTING

		new AnvilGUI.Builder()
	    .onComplete((player, text) -> {
	        if("you".equalsIgnoreCase(text)) {
	            player.sendMessage("login");
	            return AnvilGUI.Response.close();
	        } else return AnvilGUI.Response.text("incorrect password kekw");
	    })
	    .preventClose()
	    .text("Enter your password...")
	    .itemLeft(builden)
	    .itemRight(new ItemStack(Material.IRON_SWORD))
	    .onLeftInputClick(player -> {
	   	 
	    })
	    .onRightInputClick(player -> {
	   	 
	   	 
	    })
	    .plugin(Seriex.get())
	    .open(_w.getHooked());
		// @ENABLE_FORMATTING
	}
}
