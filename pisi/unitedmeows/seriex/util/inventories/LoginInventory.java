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
		String forgotPasswordText = Seriex.get().I18n().getString("auth.forgot_password", _w);
		String forgotPasswordTooltip = Seriex.get().I18n().getString("auth.forgot_password_tooltip", _w);
		// TODO set ("auth.click_to_login") in TranslationConfig
		// default message -> Click to login!
		String clickToLogin = Seriex.get().I18n().getString("auth.click_to_login", _w);
		// TODO set ("auth.default_text") in TranslationConfig
		// default message -> Enter your password...
		String defaultText = Seriex.get().I18n().getString("auth.default_text", _w);
		ItemStack forgor = ItemBuilder.of(questionMark).name(String.format("&d%s", forgotPasswordText)).lore(forgotPasswordTooltip).build();
		ItemStack loginItem = ItemBuilder.of(Material.ENCHANTED_BOOK).name(String.format("&5%s", clickToLogin)).enchantment(Enchantment.LURE, 2173).build();
		Builder plugin = new AnvilGUI.Builder().onComplete((player, text) -> {
			if (Objects.equals(Hashing.hashedString(_w.attribute("salt") + text), _w.attribute("password"))) {
				// TODO set ("auth.logged_in") in TranslationConfig
				// Succesfully logged in! <- default message
				String value = Seriex.get().I18n().getString("auth.logged_in", Seriex.get().dataManager().user(player));
				player.sendMessage(Seriex.get().colorizeString(String.format("%s &7%s", Seriex.get().getSuffix(), value)));
				authListener.stopAuthentication(_w);
				return AnvilGUI.Response.close();
			} else {
				authListener.stopAuthentication(_w);
				// TODO set ("auth.incorrrect_password") in TranslationConfig
				// Wrong password! <- default message
				String value = Seriex.get().I18n().getString("auth.incorrrect_password", Seriex.get().dataManager().user(player));
				return AnvilGUI.Response.text(value);
			}
		}).preventClose().text(defaultText).itemLeft(forgor).itemRight(loginItem).onLeftInputClick(player -> {
			Seriex.get().sendMessage(player, "Open a ticket on discord using the #ticket channel.");
		}).plugin(Seriex.get());
		plugin.open(_w.getHooked());
	}
}
