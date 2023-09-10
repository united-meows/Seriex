package pisi.unitedmeows.seriex.util.inventories.ui;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.AnvilGUI.Builder;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.auth.AuthManager;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.language.I18n;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.safety.Try;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes;

public class LoginGUI {
	public static void open(PlayerW _w, AuthManager authListener) {
		Try.safe(() -> {
			Seriex seriex = Seriex.get();
			I18n i18n = seriex.I18n();
			ItemStack loginItem = ItemBuilder.of(Material.ENCHANTED_BOOK)
						.name(String.format("&5%s", i18n.getMessage(Messages.AUTH_CLICK_TO_LOGIN, _w)))
						.lore(i18n.getMessage(Messages.AUTH_FORGOT_PASSWORD, _w))
						.enchantment(Enchantment.DURABILITY, -2173).build();
			Builder anvilBuilder = new AnvilGUI.Builder()
						.onComplete((player, text) -> {
							if (Objects.equals(Hashing.hashedString(_w.attribute(Attributes.SALT) + text), _w.attribute(Attributes.PASSWORD))) {
								authListener.stopAuthentication(_w);
								_w.correctPassword(true);
								return AnvilGUI.Response.close();
							} else {
								return AnvilGUI.Response.text(i18n.getMessage(Messages.AUTH_INCORRECT_PASSWORD, _w));
							}
						})
						.onClose(player -> { 
							Seriex.get().runLater(() -> {
								if(_w.correctPassword())
									return;
								
								Seriex.get().kick(_w.hook(), Messages.AUTH_GUI_FAILURE);
							}, 20);
						})
						.text(i18n.getMessage(Messages.AUTH_DEFAULT_GUI_TEXT, _w))
						.itemLeft(loginItem)
						.onLeftInputClick(player -> seriex.msg(player, Messages.AUTH_FORGOT_PASSWORD_ACTION))
						.plugin(seriex.plugin());
			Bukkit.getScheduler().runTaskLater(seriex.plugin(), () -> anvilBuilder.open(_w.hook()), 20);
		}, () -> Seriex.get().kick(_w.hook(), Messages.AUTH_GUI_FAILURE));
	}
}
