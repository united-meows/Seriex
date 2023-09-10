package pisi.unitedmeows.seriex.managers.area.impl.combat;

import static com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.*;
import static pisi.unitedmeows.seriex.util.inventories.ItemBuilder.of;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleConsumer;
import java.util.function.ObjIntConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.ItemStack;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.impl.Area;
import pisi.unitedmeows.seriex.managers.area.impl.AreaBase;
import pisi.unitedmeows.seriex.managers.area.impl.AreaData;
import pisi.unitedmeows.seriex.managers.area.pointer.normal.impl.BooleanPointer;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.LocationPointer;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer.Imitate;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer.Settings;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.EquipmentModifier;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier.MetadataFlag;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl.MetadataModifier.MetadataType;
import pisi.unitedmeows.seriex.managers.virtualplayers.path.VirtualWaypoint;
import pisi.unitedmeows.seriex.managers.virtualplayers.profile.VirtualProfile;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.safety.NullSafety;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

@AreaData(base = AreaBase.DAMAGE_TEST)
public class DamageTestArea extends Area {
	private static final String NPC_LOCATION = "npc_location";
	private static final String DURABILITY = "durability";
	private static final String BLOCKING = "blocking";

	private Map<UUID, Pair<List<Double>, List<Double>>> hits;

	public DamageTestArea(String cfgName) {
		super(cfgName);
	}

	private boolean startCalculation;
	private VirtualPlayer target;
	private int hurtTime;

	private boolean durabilityBot;
	private boolean shouldBlock;

	@Override
	public void start() {
		ServerConfig serverConfig = Seriex.get().fileManager().config(ServerConfig.class);
		Location pointerData = new Location(Bukkit.getWorld(serverConfig.WORLD_NAME.value()), 0, 0, 0);
		this.hits = new ConcurrentHashMap<>();

		add_ptr(NPC_LOCATION, new LocationPointer(pointerData));
		add_ptr(DURABILITY, new BooleanPointer(false));
		add_ptr(BLOCKING, new BooleanPointer(false));

		this.buildTarget(get_ptr(NPC_LOCATION));
		BooleanPointer durability_ptr = get_ptr(DURABILITY);
		BooleanPointer block_ptr = get_ptr(BLOCKING);

		this.durabilityBot = durability_ptr.data();
		this.shouldBlock = block_ptr.data();

		check_ptr_update((ptr_obj, name) -> {
			if (NPC_LOCATION.equals(name)) {
				LocationPointer loc_ptr = (LocationPointer) ptr_obj;
				NullSafety.assertNotNull(target);
				this.target.updateBaseLocation(loc_ptr.data());
			} else if (DURABILITY.equals(name)) {
				BooleanPointer bool_ptr = (BooleanPointer) ptr_obj;
				this.durabilityBot = bool_ptr.data();
				NullSafety.assertNotNull(target);
				this.equipKit();
			} else if (BLOCKING.equals(name)) {
				BooleanPointer bool_ptr = (BooleanPointer) ptr_obj;
				this.shouldBlock = bool_ptr.data();
				NullSafety.assertNotNull(target);
				this.target.equipmentModifier().equip(
							MAINHAND,
							this.shouldBlock ? of(Material.DIAMOND_SWORD).build() : of(Material.AIR).build()).send();
			}
		});
	}

	private void equipKit() {
		EquipmentModifier equipmentModifier = this.target.equipmentModifier();
		equipmentModifier.equip(HEAD, durabilityBot ? of(Material.DIAMOND_HELMET).build() : null).send();
		equipmentModifier.equip(CHEST, durabilityBot ? of(Material.DIAMOND_CHESTPLATE).build() : null).send();
		equipmentModifier.equip(LEGS, durabilityBot ? of(Material.DIAMOND_LEGGINGS).max_enchantment(Enchantment.DURABILITY).build() : null).send();
		equipmentModifier.equip(FEET, durabilityBot ? of(Material.DIAMOND_BOOTS).max_enchantment(Enchantment.DURABILITY).build() : null).send();
		equipmentModifier.equip(MAINHAND, this.shouldBlock ? of(Material.DIAMOND_SWORD).build() : of(Material.AIR).build()).send();
	}

	private void buildTarget(LocationPointer loc_ptr) {
		VirtualPlayer virtualPlayer = Seriex.get().virtualPlayerManager().get(this.name);
		if (virtualPlayer != null) {
			this.target = virtualPlayer;
			return;
		}
		VirtualWaypoint virtualWaypoint = VirtualWaypoint.create(null);
		target = VirtualPlayer.create(
					VirtualProfile.create(this.name, "ghost2173"),
					loc_ptr.data(),
					virtualWaypoint,
					new Pair[] {
								Pair.of(Settings.AREA, true),
								Pair.of(Settings.DAMAGE, true)
					},
					new Pair[] {
								Pair.of(Imitate.LOOK, true),
								Pair.of(Imitate.SNEAKING, true)
					});
	}

	private boolean isPlayerBlocking;

	@Override
	public void enter(Player player) {
		hits.put(player.getUniqueId(), Pair.of(new ArrayList<>(), new ArrayList<>()));
		if (this.durabilityBot) {
			this.equipKit();
			this.target.bukkitPlayer().updateInventory();
		}
	}

	private void onLeave(Player player) {
		hits.remove(player.getUniqueId());
	}

	@Override
	public void leave(Player player) {
		this.onLeave(player);
	}

	@Override
	public void disconnect(Player player) {
		this.onLeave(player);
	}

	@Override
	public void tick() {
		if (hurtTime > 0)
			hurtTime--;

		if (shouldBlock) {
			ItemStack heldItem = this.target.entityPlayer().bA();
			this.target.entityPlayer()
						.a /* EntityPlayer#setItemInUse(item, duration) */
						(heldItem, 72000); /* 72000 comes from ItemSword#getMaxItemUseDuration */
			this.target.metadataModifier().setFlag(MetadataType.PLAYER_FLAGS, MetadataFlag.USING_ITEM, true);
		}
	}

	@Override
	public boolean attack(Player damager, LivingEntity damaged, EntityDamageByEntityEvent rawEvent) {
		if (!isInside(damager))
			return damaged.getEntityId() == this.target.entityID();

		boolean damageAllowed = target.setting(Settings.DAMAGE);
		if (!damageAllowed
					|| rawEvent.getCause() != DamageCause.ENTITY_ATTACK
					|| damaged.getEntityId() != this.target.entityID())
			return true;

		PlayerW user = Seriex.get().dataManager().user(damager);

		boolean surpassedLimit = false;
		double damage = rawEvent.getDamage();
		int maximum_damage_limit = 100;

		if (hurtTime <= 10) {
			Pair<List<Double>, List<Double>> pair = hits.get(damager.getUniqueId());
			pair.key().clear();
			pair.value().clear();
			user.reset_attacks();
			Seriex.get().msg_no_translation(damager, "&5|[&fAttacks&5]|");
			this.target.fakeDamage(damage > maximum_damage_limit);
			startCalculation = true;
			hurtTime = 20;
		} else {
			user.updateAttacks(user.attacks() + 1);
			if (user.attacks() > 4 && !user.hasDurabilityPatchBypass()) {
				user.updateAttacks(4);
				surpassedLimit = true;
			}
		}
		DoubleConsumer healthConsumer = value -> {
			damaged.setMaxHealth(value);
			damaged.setHealth(value);
		};
		if (!surpassedLimit) {
			healthConsumer.accept(1000);
			Seriex.get().runLater(() -> healthConsumer.accept(20), hurtTime);
			Runnable durabilityRunnable = null;
			if (durabilityBot) {
				EntityPlayer entityPlayer = this.target.entityPlayer();
				int[] previousArmorDurability = new int[entityPlayer.inventory.armor.length];
				for (int i = 0; i < previousArmorDurability.length; i++) {
					previousArmorDurability[i] = entityPlayer.inventory.armor[i].getData();
				}
				target.bukkitPlayer().updateInventory();
				durabilityRunnable = () -> {
					ObjIntConsumer<String> consumer = (armorName, integer) -> {
						int oldDurability = previousArmorDurability[integer];
						ItemStack armor = entityPlayer.inventory.armor[integer];
						int newDurability = armor == null ? 0 : armor.getData();
						Seriex.get().msg_no_translation(damager, "%s: %s", armorName, newDurability - oldDurability);
					};
					consumer.accept("Helmet", 3);
					consumer.accept("Chestplate", 2);
					consumer.accept("Leggings", 1);
					consumer.accept("Boots", 0);
				};
			}
			if (startCalculation) {
				final Runnable finalRunnable = durabilityRunnable;
				Seriex.get().runLater(() -> {
					if (finalRunnable != null) { // nice work java!
						Seriex.get().msg_no_translation(damager, "&5|[&fArmor&5]|");
						finalRunnable.run();
					}
					Pair<List<Double>, List<Double>> pair = hits.get(damager.getUniqueId());
					boolean someIssueLol = hits == null || pair == null || pair.key() == null || pair.value() == null;
					Seriex.get().msg_no_translation(damager, "&7Results &5=> &fAttacks: %s , Total Damage: %s , Total Final Damage: %s",
								user.attacks(),
								someIssueLol ? "N/A" : pair.key().stream().mapToDouble(Double::doubleValue).sum(),
								someIssueLol ? "N/A" : BigDecimal.valueOf(pair.value().stream().mapToDouble(Double::doubleValue).sum()).setScale(3, RoundingMode.HALF_UP).doubleValue());
					this.equipKit();
				}, 10);
				startCalculation = false;
			}
			double finDamage = BigDecimal.valueOf(rawEvent.getFinalDamage()).setScale(3, RoundingMode.HALF_UP).doubleValue();
			double rouDamage = BigDecimal.valueOf(damage).setScale(3, RoundingMode.HALF_UP).doubleValue();
			Pair<List<Double>, List<Double>> pair = this.hits.get(damager.getUniqueId());
			pair.key().add(rouDamage);
			pair.value().add(finDamage);
			Seriex.get().msg_no_translation(damager, "&8[&f%s&8] &5=> &a%s, &c%s", user.attacks(), damage, finDamage);
		}
		return damage > maximum_damage_limit;
	}

	@Override
	public boolean move(Player player) {
		return false;
	}

	@Override
	public boolean isConfigured() { return target != null; }
}
