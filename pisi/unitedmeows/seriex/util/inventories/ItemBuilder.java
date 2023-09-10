package pisi.unitedmeows.seriex.util.inventories;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import dev.derklaro.reflexion.Reflexion;

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

	public static ItemBuilder head(String data) {
		return new ItemBuilder(Material.SKULL_ITEM).durability(3).headData(data);
	}

	public static ItemBuilder potion(PotionType type, int level, boolean splash, boolean extended) {
		return new ItemBuilder(Material.POTION).applyPotion(type, level, splash, extended);
	}

	public ItemBuilder amount(final int amount) {
		this.is.setAmount(amount);
		return this;
	}


	private static void profile(SkullMeta meta, GameProfile profile) {
		Reflexion.on(meta.getClass()).findField("profile").orElseThrow().setValue(meta, profile);
	}

	private ItemBuilder headData(String data) {
		SkullMeta itemMeta = (SkullMeta) is.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/" + data).getBytes());
		profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
		profile(itemMeta, profile);
		is.setItemMeta(itemMeta);
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
		if (name != null) {
			lore.add(ChatColor.translateAlternateColorCodes('&', name));
		}
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder unbreakable(final boolean yes) {
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

	public ItemBuilder selected(BooleanSupplier supplier) {
		return supplier.getAsBoolean()
					? enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, -2173).addFlags(ItemFlag.HIDE_ENCHANTS)
					: this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment) {
		this.is.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemBuilder max_enchantment(final Enchantment enchantment) {
		this.is.addUnsafeEnchantment(enchantment, enchantment.getMaxLevel());
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

	public ItemBuilder addFlags(ItemFlag... flags) {
		final ItemMeta itemMeta = this.is.getItemMeta();
		for (ItemFlag flag : flags)
			itemMeta.addItemFlags(flag);
		this.is.setItemMeta(itemMeta);
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

	private ItemBuilder applyPotion(PotionType type, int level, boolean splash, boolean extended) {
		new Potion(type, level, splash, extended).apply(is);
		return this;
	}
}
