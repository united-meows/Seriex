package pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import dev.derklaro.reflexion.Reflexion;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.VirtualModifier;

public class RotationModifier extends VirtualModifier {

	public RotationModifier(VirtualPlayer npc) {
		super(npc);
	}

	public RotationModifier queueRotate(float yaw, float pitch) {
		final byte yawAngle = (byte) (yaw * 256F / 360F);
		final byte pitchAngle = (byte) (pitch * 256F / 360F);

		super.queue((npc, target) -> {
			PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation();
			
			Reflexion reflexion = Reflexion.on(headRotation);
			reflexion.findField("a").ifPresent(field -> field.setValue(headRotation, npc.entityID()));
			reflexion.findField("b").ifPresent(field -> field.setValue(headRotation, yawAngle));
			return headRotation;
		});
		super.queue((npc, target) -> {
			final Location location = new Location(npc.bukkitWorld(), npc.pos().getX(), npc.pos().getY(), npc.pos().getZ());
			return new PacketPlayOutEntityTeleport(
						npc.entityID(),
						(int) Math.floor(location.getX() * 32.0D),
						(int) Math.floor(location.getY() * 32.0D),
						(int) Math.floor(location.getZ() * 32.0D),
						yawAngle,
						pitchAngle,
						npc.entityPlayer().onGround);
		});
		return this;
	}

	public RotationModifier queueLookAt(Location location) {
		Vector pos = npc.pos();

		double xDifference = location.getX() - pos.getX();
		double yDifference = location.getY() - pos.getY();
		double zDifference = location.getZ() - pos.getZ();

		double r = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2) + Math.pow(zDifference, 2));

		float yaw = (float) (-Math.atan2(xDifference, zDifference) / Math.PI * 180D);
		yaw = yaw < 0 ? yaw + 360 : yaw;

		float pitch = (float) (-Math.asin(yDifference / r) / Math.PI * 180D);

		return this.queueRotate(yaw, pitch);
	}
}
