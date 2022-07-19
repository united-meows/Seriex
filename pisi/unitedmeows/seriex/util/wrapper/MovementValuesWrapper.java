package pisi.unitedmeows.seriex.util.wrapper;

// todo: find better name for this class
public class MovementValuesWrapper {
	public float walkSpeed , flySpeed;
	public int foodLevel;
	public boolean isSprinting;

	public MovementValuesWrapper(float walkSpeed, float flySpeed, int foodLevel, boolean isSprinting) {
		this.walkSpeed = walkSpeed;
		this.flySpeed = flySpeed;
		this.foodLevel = foodLevel;
		this.isSprinting = isSprinting;
	}
}
