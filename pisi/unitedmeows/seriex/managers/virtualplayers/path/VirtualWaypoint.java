package pisi.unitedmeows.seriex.managers.virtualplayers.path;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;

public class VirtualWaypoint {

	private List<Vector> waypoints;
	private double speed = 0.2;
	private int pointIndex;
	private boolean loop;

	public static VirtualWaypoint create(List<Vector> waypoints) {
		return new VirtualWaypoint(waypoints);
	}

	private VirtualWaypoint(List<Vector> waypoints) {
		this.waypoints = waypoints;
	}

	public boolean handleMovement(VirtualPlayer virtualPlayer) {
		if (waypoints != null && waypoints.isEmpty()) {
			waypoints = null;
		}
		if (waypoints == null || pointIndex >= waypoints.size()) {
			if (loop)
				pointIndex = 0;
			return false;
		}
		Vector vector = waypoints.get(pointIndex);
		double prevDist = virtualPlayer.pos().distance(vector);
		float yawTo = getYawTo(vector, virtualPlayer);
		double[] horizontals = calc(speed, yawTo);
		virtualPlayer.entityPlayer().move(horizontals[0], 0, horizontals[1]);
		virtualPlayer.entityPlayer().f(yawTo); // setHeadRotation

		double curDist = virtualPlayer.pos().distance(vector);
		if (prevDist < curDist) {
			pointIndex++;
			if (loop && pointIndex >= waypoints.size()) {
				pointIndex = 0;
			}
		}
		return true;
	}

	private float getYawTo(Vector vector, VirtualPlayer virtualPlayer) {
		EntityPlayer entityPlayer = virtualPlayer.entityPlayer();
		double xDiff = vector.getX() - entityPlayer.locX;
		double zDiff = vector.getZ() - entityPlayer.locZ;
		return (float) (Math.atan2(zDiff, xDiff) * 180.0D / Math.PI - 90.0F);
	}

	private double[] calc(double speed, float neededYaw) {
		double xSpeed = speed * Math.cos(Math.toRadians(neededYaw + 90.0F));
		double zSpeed = speed * Math.sin(Math.toRadians(neededYaw + 90.0F));
		return new double[] {
					xSpeed, zSpeed
		};
	}

	public VirtualWaypoint speed(double speed) {
		this.speed = speed;
		return this;
	}

	public VirtualWaypoint loop(boolean loop) {
		this.loop = loop;
		return this;
	}

	public boolean isLooping() { return this.loop; }

	public double getSpeed() { return speed; }

	public List<Vector> getWaypoints() { return waypoints; }

	private Vector vec2loc(Location location) {
		return new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public boolean add(Location location) {
		return add(vec2loc(location));
	}

	public boolean remove(Location location) {
		return remove(vec2loc(location));
	}

	public boolean add(Vector vector) {
		if (waypoints == null) {
			waypoints = new ArrayList<>();
		}
		return waypoints.add(vector);
	}

	public boolean remove(Vector vector) {
		if (hasWaypoints())
			return remove(waypoints.indexOf(vector));
		return false;
	}

	public boolean remove(int index) {
		if (hasWaypoints()) {
			waypoints.remove(index);
			if (waypoints.isEmpty()) {
				waypoints = null;
			}
			return true;
		}
		return false;
	}

	public void cleanup() {
		if (waypoints != null) {
			waypoints.clear();
			waypoints = null;
		}
	}

	public boolean hasWaypoints() {
		return waypoints != null;
	}
}
