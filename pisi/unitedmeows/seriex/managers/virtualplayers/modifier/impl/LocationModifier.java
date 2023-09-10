package pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl;

import org.bukkit.Location;

import dev.derklaro.reflexion.Reflexion;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.VirtualModifier;

public class LocationModifier extends VirtualModifier {
	private int lastX, lastY, lastZ;

	public LocationModifier(VirtualPlayer npc) {
		super(npc);

		// i think this fixes the first packet dies problem
		Location baseLocation = npc.baseLocation(); // might be wrong
		this.lastX = baseLocation.getBlockX();
		this.lastY = baseLocation.getBlockY();
		this.lastZ = baseLocation.getBlockZ();
	}

	public LocationModifier queueTeleport(Location location) {
		final byte yawAngle = (byte) (location.getYaw() * 256F / 360F);
		final byte pitchAngle = (byte) (location.getPitch() * 256F / 360F);

		super.queue((npc, target) -> {
			PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation();
			
			Reflexion reflexion = Reflexion.on(headRotation);
			reflexion.findField("a").ifPresent(field -> field.setValue(headRotation, npc.entityID()));
			reflexion.findField("b").ifPresent(field -> field.setValue(headRotation, yawAngle));
			return headRotation;
		});

		super.queue((npc, target) -> new PacketPlayOutEntityTeleport(
					npc.entityID(),
					(int) Math.floor(location.getX() * 32.0D),
					(int) Math.floor(location.getY() * 32.0D),
					(int) Math.floor(location.getZ() * 32.0D),
					yawAngle,
					pitchAngle,
					npc.entityPlayer().onGround));
		this.lastX = (int) Math.floor(location.getX() * 32.0D);
		this.lastY = (int) Math.floor(location.getY() * 32.0D);
		this.lastZ = (int) Math.floor(location.getZ() * 32.0D);
		return this;
	}

	public LocationModifier queueMovement(Location location) {
		byte yawAngle = (byte) (location.getYaw() * 256F / 360F);
		byte pitchAngle = (byte) (location.getPitch() * 256F / 360F);

		super.queue((npc, target) -> {
			PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation();
			
			Reflexion reflexion = Reflexion.on(headRotation);
			reflexion.findField("a").ifPresent(field -> field.setValue(headRotation, npc.entityID()));
			reflexion.findField("b").ifPresent(field -> field.setValue(headRotation, yawAngle));
			return headRotation;
		});

		super.queue((npc, target) -> {
			int packetX = (int) Math.floor(location.getX() * 32.0D);
			int packetY = (int) Math.floor(location.getY() * 32.0D);
			int packetZ = (int) Math.floor(location.getZ() * 32.0D);

			byte deltaX = (byte) (packetX - this.lastX);
			byte deltaY = (byte) (packetY - this.lastY);
			byte deltaZ = (byte) (packetZ - this.lastZ);

			PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutRelEntityMoveLook(npc.entityID(),
						deltaX,
						deltaY,
						deltaZ,
						yawAngle,
						pitchAngle, npc.entityPlayer().onGround);

			this.lastX = packetX;
			this.lastY = packetY;
			this.lastZ = packetZ;
			return moveLook;
		});
		return this;
	}

	public void updateF() {
		this.queueTeleport(npc.location()).send();
	}
}
