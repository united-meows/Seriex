package pisi.unitedmeows.seriex.util.simulator;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EnumMonsterType;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.ItemArmor;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MobEffectList;
import pisi.unitedmeows.seriex.util.Pair;

/**
 * Logic behind this class is that since we know the damage source (EntityDamageSource)
 * we can simulate the damage by just copying the damage method in EntityLiving
 * <br>
 * <br>
 * Currently unused.
 */
public class EntityDamageSimulator {
	private EntityHuman prosecutor; // sounded way funnier in my head

	private double health;
	private double absorptionHearts;
	private double lastDamage;

	private int hurtTime;

	private boolean baseOnly = false;
	private double baseHealth;
	private int resistanceAmplifier = -1;
	private ItemStack[] equipment = {
				/* empty */ };

	private List<Double> record;
	private OnHitFinish onHitFinish;

	private Map<DamageModifier, Boolean> damageModifierOptions;

	public static EntityDamageSimulator createSimulation(EntityHuman prosecutor, Pair<DamageModifier, Boolean>... options) {
		return new EntityDamageSimulator(prosecutor, options);
	}

	public EntityDamageSimulator baseHealth(double baseHealth) {
		this.baseHealth = baseHealth;
		return this;
	}

	public EntityDamageSimulator absorptionHearts(double absorptionHearts) {
		this.absorptionHearts = absorptionHearts;
		return this;
	}

	public EntityDamageSimulator resistancePotion(int resistanceAmplifier) {
		this.resistanceAmplifier = resistanceAmplifier;
		return this;
	}

	public EntityDamageSimulator armor(ItemStack[] equipment) {
		this.equipment = equipment;
		return this;
	}

	public EntityDamageSimulator record(List<Double> recorder, boolean baseOnly) {
		this.record = recorder;
		this.baseOnly = baseOnly;
		return this;
	}

	public EntityDamageSimulator hitFinish(OnHitFinish finish) {
		this.onHitFinish = finish;
		return this;
	}

	private EntityDamageSimulator(EntityHuman prosecutor, Pair<DamageModifier, Boolean>... options) {
		this.prosecutor = prosecutor;
		this.damageModifierOptions = new EnumMap<>(DamageModifier.class);
		for (Pair<DamageModifier, Boolean> option : options) {
			damageModifierOptions.put(option.key(), option.value());
		}
	}

	public boolean damageEntity(double baseDamage) {
		if (health < 0F)
			return false;
		if (this.hurtTime > 10) {
			if (baseDamage <= this.lastDamage) // 0 damage
				return false;
			double damage = calculateEntityDamage(baseDamage - this.lastDamage);
			if (damage == baseHealth)
				return false;
			this.lastDamage = baseDamage;
		} else {
			double damage = calculateEntityDamage(baseDamage - this.lastDamage);
			if (damage == baseHealth) // 0 damage
				return false;
			this.lastDamage = baseDamage;
			this.hurtTime = 20;
		}
		return true;
	}

	public boolean calculateAttack() {
		float baseDamage = (float) prosecutor.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
		float modifierForCreature = EnchantmentManager.a(prosecutor.bA(), EnumMonsterType.UNDEFINED);
		if (baseDamage > 0.0F || modifierForCreature > 0.0F) {
			boolean criticals = prosecutor.fallDistance > 0.0F && !prosecutor.onGround && !prosecutor.k_() && !prosecutor.V() && !prosecutor.hasEffect(MobEffectList.BLINDNESS) && prosecutor.vehicle == null;
			if (criticals && baseDamage > 0.0F) {
				baseDamage *= 1.5F;
			}
			baseDamage += modifierForCreature;
			if (damageEntity(baseDamage)) {
				// give knockback if you want :D
				return true;
			}
		}
		return false;
	}

	public double calculateEntityDamage(double baseDamage0) {
		double originalDamage = baseDamage0;
		double baseDamage = baseDamage0;
		for (DamageModifier damageModifier : DamageModifier.values()) {
			Boolean modifierValue = damageModifierOptions.get(damageModifier);
			if (modifierValue == null || !modifierValue.booleanValue())
				continue;
			float modifier = damageModifier.modifier.apply(this, baseDamage).floatValue();
			baseDamage += modifier;
		}
		record.add(baseOnly ? originalDamage : baseDamage);
		double newHealth = baseHealth - baseDamage;
		this.health = newHealth;
		return newHealth;
	}

	protected int armorBar() {
		int totalProtectionValue = 0;
		for (ItemStack item : equipment) {
			if (item != null && item.getItem() instanceof ItemArmor itemArmor) {
				totalProtectionValue += itemArmor.c;
			}
		}
		return totalProtectionValue;
	}

	protected float applyMagicModifier(float f) {
		if (f <= 0.0F)
			return 0.0F;
		int i = EnchantmentManager.a(equipment, DamageSource.playerAttack(prosecutor));
		if (i > 20)
			i = 20;
		if (i > 0 && i <= 20) {
			int j = 25 - i;
			float f1 = f * j;
			f = f1 / 25.0F;
		}
		return f;
	}

	protected float applyArmorModifier(float baseDamage) {
		int i = 25 - armorBar();
		float f1 = baseDamage * i;
		return f1 / 25.0F;
	}

	public enum DamageModifier {
		BLOCKING((player, baseDamage) -> {
			if (baseDamage > 0.0D)
				return -(baseDamage - (1.0D + baseDamage) * 0.5D);
			return -0.0D;
		}),

		ARMOR((player, baseDamage) -> -(baseDamage - player.applyArmorModifier(baseDamage.floatValue()))),
		RESISTANCE((player, baseDamage) -> {
			if (player.resistanceAmplifier == -1)
				return 0.0;
			int amplifier = (player.resistanceAmplifier + 1) * 5;
			int j = 25 - amplifier;
			float f1 = baseDamage.floatValue() * j;
			return -(baseDamage.doubleValue() - f1 / 25.0F);
		}),
		ENCHANTMENTS((player, baseDamage) -> -(baseDamage.doubleValue() - player.applyMagicModifier(baseDamage.floatValue()))),
		ABSORPTION((player, baseDamage) -> -Math.max(baseDamage.doubleValue() - Math.max(baseDamage.doubleValue() - player.absorptionHearts, 0.0D), 0.0D));

		final BiFunction<EntityDamageSimulator, Double, Double> modifier;

		DamageModifier(BiFunction<EntityDamageSimulator, Double, Double> modifier) {
			this.modifier = modifier;
		}

		public float apply(EntityDamageSimulator damageSimulator, double baseDamage) {
			return modifier.apply(damageSimulator, baseDamage).floatValue();
		}
	}

	public void updateHurttime() {
		if (this.hurtTime > 0)
			this.hurtTime--;
		else if (record != null && !record.isEmpty()) {
			onHitFinish.finish(record);
			record.clear();
			this.health = baseHealth;
		}
	}

	@FunctionalInterface
	public interface OnHitFinish {
		void finish(List<Double> damages);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EntityDamageSimulator [");
		if (prosecutor != null) builder.append("prosecutor=").append(prosecutor).append(", ");
		builder.append("health=").append(health).append(", absorptionHearts=").append(absorptionHearts).append(", lastDamage=").append(lastDamage).append(", hurtTime=").append(hurtTime).append(", baseOnly=").append(baseOnly).append(", baseHealth=")
					.append(baseHealth).append(", resistanceAmplifier=").append(resistanceAmplifier).append(", ");
		if (equipment != null) builder.append("equipment=").append(Arrays.toString(equipment)).append(", ");
		if (record != null) builder.append("record=").append(record).append(", ");
		if (damageModifierOptions != null) builder.append("damageModifierOptions=").append(damageModifierOptions);
		builder.append("]");
		return builder.toString();
	}
}
