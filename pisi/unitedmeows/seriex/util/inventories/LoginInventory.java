package pisi.unitedmeows.seriex.util.inventories;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.AnvilGUI.Builder;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.auth.AuthListener;
import pisi.unitedmeows.seriex.util.language.I18n;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class LoginInventory {
	public static void open(PlayerW _w, AuthListener authListener) {
		try {
			long openMS = System.currentTimeMillis();
			Seriex.logger().debug("[LOGIN] - opened inventory?");
			I18n i18n = Seriex.get().I18n();
			String forgotPasswordTooltip = i18n.getString("auth.forgot_password", _w);
			String clickToLogin = i18n.getString("auth.click_to_login", _w);
			String defaultText = i18n.getString("auth.default_text", _w);
			ItemStack loginItem = ItemBuilder.of(Material.ENCHANTED_BOOK).name(String.format("&5%s", clickToLogin)).lore(forgotPasswordTooltip).enchantment(Enchantment.DURABILITY, -2173).build();
			Builder plugin = new AnvilGUI.Builder().onComplete((player, text) -> {
				if (Objects.equals(Hashing.hashedString(_w.attribute("salt") + text), _w.attribute("password"))) {
					// TODO set ("auth.logged_in") in TranslationConfig
					// Succesfully logged in! <- default message
					String value = i18n.getString("auth.logged_in", Seriex.get().dataManager().user(player));
					player.sendMessage(Seriex.get().colorizeString(String.format("%s &7%s", Seriex.get().suffix(), value)));
					authListener.stopAuthentication(_w);
					_w.loggedIn = true;
					return AnvilGUI.Response.close();
				} else {
					// TODO set ("auth.incorrrect_password") in TranslationConfig
					// Wrong password! <- default message
					String value = i18n.getString("auth.incorrrect_password", Seriex.get().dataManager().user(player));
					return AnvilGUI.Response.text(value);
				}
			}).preventClose().text(defaultText).itemLeft(loginItem).onLeftInputClick(player -> {
				Seriex.get().msg(player, "Open a ticket on discord using the #ticket channel.");
			}).onClose(player -> {
				if (_w.loggedIn) return;
				// TODO set ("auth.close_gui") in TranslationConfig
				String value = i18n.getString("auth.close_gui", Seriex.get().dataManager().user(player));
				if (System.currentTimeMillis() - openMS > 1000) {
					Seriex.get().kick(player, value);
				}
			}).plugin(Seriex.get());
			plugin.open(_w.getHooked());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
