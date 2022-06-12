package pisi.unitedmeows.seriex.util.inventories;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.AnvilGUI.Builder;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.auth.AuthListener;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class LoginInventory {
	public static void open(PlayerW _w, AuthListener authListener) {
		ItemStack questionMark = new ItemStack(Material.FISHING_ROD, 1, (short) 3);
		//		SkullMeta meta = (SkullMeta) questionMark.getItemMeta();
		//		meta.setOwner("MHF_Question");
		//		questionMark.setItemMeta(meta);
		ItemStack forgor = ItemBuilder.of(questionMark).name("&dForgot your password?").lore("Open a ticket on discord using the #ticket channel.").build();
		ItemStack loginItem = ItemBuilder.of(Material.ENCHANTED_BOOK).name("&5Click to login!").enchantment(Enchantment.LURE, 2173).build();
		// @DISABLE_FORMATTING
		Builder plugin = new AnvilGUI.Builder()
	    .onComplete((player, text) -> {
	        if(Objects.equals(Hashing.hashedString(_w.attribute("salt") + text),
	      			  _w.attribute("password"))) {
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
	    .itemLeft(forgor)
	    .itemRight(loginItem)
	    .onLeftInputClick(player -> {
	   	 Seriex.get().sendMessage(player, "Open a ticket on discord using the #ticket channel.");
	    })
	    .onRightInputClick(player -> {
	   	 Seriex.get().sendMessage(player, "Right click!");
	    })
	    .plugin(Seriex.get());
		plugin
	    .open(_w.getHooked());
		// @ENABLE_FORMATTING
	}
}
