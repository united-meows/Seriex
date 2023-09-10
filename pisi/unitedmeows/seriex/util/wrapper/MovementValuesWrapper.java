package pisi.unitedmeows.seriex.util.wrapper;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pisi.unitedmeows.seriex.Seriex;

public class MovementValuesWrapper {
	private static final float BASE_WALK_SPEED = 0.2f;
	private static final float BASE_FLY_SPEED = 0.1f;
	private static final float SMALLEST_ALLOWED_SPEED = 1E-3F;

	private PotionEffect previousJumpPotionEffect;
	private float walkSpeed, flySpeed;
	private final Player player;
	private boolean isSprinting;
	private int foodLevel;

	public MovementValuesWrapper(Player player) {
		this.walkSpeed = player.getWalkSpeed();
		if (this.walkSpeed != BASE_WALK_SPEED) {
			this.walkSpeed = BASE_WALK_SPEED;
		}
		this.flySpeed = player.getFlySpeed();
		if (this.flySpeed != BASE_FLY_SPEED) {
			this.flySpeed = BASE_FLY_SPEED;
		}
		this.foodLevel = player.getFoodLevel();
		this.isSprinting = player.isSprinting();
		this.player = player;
		this.previousJumpPotionEffect = null;
	}

	public void interruptMovement(Player player) {
		player.setWalkSpeed(0);
		player.setFlySpeed(0);
		player.setFoodLevel(3);
		player.setSprinting(false);
		if (player.hasPotionEffect(PotionEffectType.JUMP) && !player.getActivePotionEffects().isEmpty() /* cant happen but, lets check it anyway */) {
			player.getActivePotionEffects()
						.parallelStream()
						.filter(potionEffect -> potionEffect.getType() == PotionEffectType.JUMP)
						.findFirst()
						.ifPresent(potionEffect -> previousJumpPotionEffect = potionEffect);
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));
	}

	public void allowMovement(Player player) {
		player.setWalkSpeed(walkSpeed);
		player.setFlySpeed(flySpeed);
		player.setFoodLevel(foodLevel);
		player.setSprinting(isSprinting);
		player.removePotionEffect(PotionEffectType.JUMP);
		if (previousJumpPotionEffect != null) {
			player.addPotionEffect(previousJumpPotionEffect);
		}
	}

	// these are not %100 accurate but good enough lol
	public boolean areValuesBroken() {
		return areValuesBroken(this.walkSpeed, this.flySpeed, this.foodLevel);
	}

	public static boolean areValuesBroken(Player player) {
		return areValuesBroken(player.getWalkSpeed(), player.getFlySpeed(), player.getFoodLevel());
	}

	private static boolean areValuesBroken(double walkSpeed, double flySpeed, double foodLevel) {
		return Math.abs(walkSpeed) <= SMALLEST_ALLOWED_SPEED
					|| Math.abs(flySpeed) <= SMALLEST_ALLOWED_SPEED
					|| foodLevel <= 0;
	}

	public void fixValues() {
		walkSpeed = BASE_WALK_SPEED;
		flySpeed = BASE_FLY_SPEED;
		foodLevel = 20;
		isSprinting = true;
		Seriex.get().logger().error("Movement values were broken for {}!", player.getName());
		allowMovement(player);
	}
}
