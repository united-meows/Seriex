package pisi.unitedmeows.seriex.util.inventories;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemBuilder {
	private final ItemStack is;

	private ItemBuilder(final Material mat) {
		this.is = new ItemStack(mat);
	}

	private ItemBuilder(final ItemStack is) {
		this.is = is;
	}

	public static ItemBuilder of(final ItemStack is) {
		return new ItemBuilder(is);
	}

	public static ItemBuilder of(final Material is) {
		return new ItemBuilder(is);
	}

	public ItemBuilder amount(final int amount) {
		this.is.setAmount(amount);
		return this;
	}

	public ItemBuilder name(final String name) {
		final ItemMeta meta = this.is.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final String name) {
		final ItemMeta meta = this.is.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.add(ChatColor.translateAlternateColorCodes('&', name));
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder setUnbreakable(final boolean yes) {
		final ItemMeta meta = this.is.getItemMeta();
		meta.spigot().setUnbreakable(yes);
		return this;
	}

	public ItemBuilder lore(final List<String> lore) {
		final List<String> toSet = new ArrayList<>();
		final ItemMeta meta = this.is.getItemMeta();
		for (final String string : lore) {
			toSet.add(ChatColor.translateAlternateColorCodes('&', string));
		}
		meta.setLore(toSet);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder durability(final int durability) {
		this.is.setDurability((short) durability);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
		this.is.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment) {
		this.is.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemBuilder type(final Material material) {
		this.is.setType(material);
		return this;
	}

	public ItemBuilder clearLore() {
		final ItemMeta meta = this.is.getItemMeta();
		meta.setLore(new ArrayList<>());
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder clearEnchantments() {
		for (final Enchantment e : this.is.getEnchantments().keySet()) {
			this.is.removeEnchantment(e);
		}
		return this;
	}

	public ItemBuilder clearFlags() {
		final ItemMeta itemMeta = this.is.getItemMeta();
		itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_PLACED_ON);
		this.is.setItemMeta(itemMeta);
		return this;
	}

	public ItemStack build() {
		return this.is;
	}
}
