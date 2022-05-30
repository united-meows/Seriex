package pisi.unitedmeows.seriex.util.inventories;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.wesjd.anvilgui.AnvilGUI;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.auth.AuthListener;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class LoginInventory {
	public static void open(PlayerW _w, AuthListener authListener) {
		ItemStack questionMark = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) questionMark.getItemMeta();
		meta.setOwner("MHF_Question");
		questionMark.setItemMeta(meta);
		ItemStack builden = ItemBuilder.of(questionMark).name("&dForgot your password?").lore("Open a ticket on discord using the #ticket channel.").build();
		ItemStack exclamation = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta exclamationMeta = (SkullMeta) exclamation.getItemMeta();
		exclamationMeta.setOwner("MHF_Question");
		exclamation.setItemMeta(exclamationMeta);
		ItemStack exclamationItem = ItemBuilder.of(questionMark).name("&5Click to login!").enchantment(Enchantment.ARROW_INFINITE, 1).build();
		// @DISABLE_FORMATTING
		new AnvilGUI.Builder()
	    .onComplete((player, text) -> {
	        if(Objects.equals(Hashing.hashedString(_w.attribute("salt") + text), _w.attribute("password"))) {
	            player.sendMessage("logged in");
	            authListener.stopAuthentication(_w);
	            return AnvilGUI.Response.close();
	        } else {
	      	   authListener.stopAuthentication(_w);
	      	  return AnvilGUI.Response.text("incorrect password kekw");
	        }
	    })
	    .preventClose()
	    .text("Enter your password...")
	    .itemLeft(builden)
	    .itemRight(exclamationItem)
	    .onLeftInputClick(player -> {
	   	 Seriex.get().sendMessage(player, "Open a ticket on discord using the #ticket channel.");
	    })
	    .onRightInputClick(player -> {
	   	 Seriex.get().sendMessage(player, "Right click!");
	    })
	    .plugin(Seriex.get())
	    .open(_w.getHooked());
		// @ENABLE_FORMATTING
	}
}
