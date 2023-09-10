package pisi.unitedmeows.seriex.util.language;

import static org.bukkit.enchantments.Enchantment.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.enchantments.Enchantment;

import pisi.unitedmeows.seriex.util.Create;

public class MinecraftLocale {

	private MinecraftLocale() {}

	public static final BiMap<String, Enchantment> ENCHANTMENT_MAP = Create.create(HashBiMap.create(24), map -> {
		map.put("protection", PROTECTION_ENVIRONMENTAL);
		map.put("fire_protection", PROTECTION_FIRE);
		map.put("feather_falling", PROTECTION_FALL);
		map.put("blast_protection", PROTECTION_EXPLOSIONS);
		map.put("projectile_protection", PROTECTION_PROJECTILE);
		map.put("respiration", OXYGEN);
		map.put("aqua_affinity", WATER_WORKER);
		map.put("thorns", THORNS);
		map.put("depth_strider", DEPTH_STRIDER);
		map.put("sharpness", DAMAGE_ALL);
		map.put("smite", DAMAGE_UNDEAD);
		map.put("bane_of_arthropods", DAMAGE_ARTHROPODS);
		map.put("knockback", KNOCKBACK);
		map.put("fire_aspect", FIRE_ASPECT);
		map.put("looting", LOOT_BONUS_MOBS);
		map.put("efficiency", DIG_SPEED);
		map.put("silk_touch", SILK_TOUCH);
		map.put("unbreaking", DURABILITY);
		map.put("fortune", LOOT_BONUS_BLOCKS);
		map.put("power", ARROW_DAMAGE);
		map.put("punch", ARROW_KNOCKBACK);
		map.put("flame", ARROW_FIRE);
		map.put("infinity", ARROW_INFINITE);
		map.put("luck_of_the_sea", LUCK);
		map.put("lure", LURE);
	});

	private static final List<String> availableEnchants = Create.create(new ArrayList<>(), list -> {
		Arrays.stream(Enchantment.values()).map(Enchantment::getName).forEach(list::add);
		Arrays.stream(Enchantment.values()).map(Enchantment::getName).map(String::toLowerCase).forEach(list::add);
		MinecraftLocale.ENCHANTMENT_MAP.keySet().stream().map(String::toUpperCase).forEach(list::add);
		list.addAll(MinecraftLocale.ENCHANTMENT_MAP.keySet());
	});
	
	public static Enchantment getByName(String enchantmentName) {
		Enchantment enchantment = Enchantment.getByName(enchantmentName.toUpperCase(Locale.ENGLISH));
		if (enchantment != null)
			return enchantment;

		return ENCHANTMENT_MAP.get(enchantmentName.toLowerCase(Locale.ENGLISH));
	}

	public static List<String> availableEnchants() {
		return availableEnchants; 
	}
}
