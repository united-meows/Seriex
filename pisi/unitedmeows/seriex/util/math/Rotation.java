package pisi.unitedmeows.seriex.util.math;

import org.bukkit.Location;

public class Rotation {
	private final float yaw, pitch;

	private Rotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public static Rotation create(float yaw, float pitch) {
		return new Rotation(yaw, pitch);
	}

	public Rotation add(float yaw, float pitch) {
		return new Rotation(this.yaw + yaw, this.pitch + pitch);
	}

	public Rotation rem(float yaw, float pitch) {
		return new Rotation(this.yaw - yaw, this.pitch - pitch);
	}

	public Location location(Location location) {
		var newLoc = location.clone();
		newLoc.setYaw(this.yaw);
		newLoc.setPitch(this.pitch);
		return newLoc;
	}

	public float yaw() {return yaw;}

	public float pitch() {return pitch;}
}
