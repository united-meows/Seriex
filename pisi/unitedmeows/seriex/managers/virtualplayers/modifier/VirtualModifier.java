package pisi.unitedmeows.seriex.managers.virtualplayers.modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;

public class VirtualModifier {
	private final List<LazyPacket> lazyPackets = new ArrayList<>();
	protected VirtualPlayer npc;

	public VirtualModifier(VirtualPlayer npc) {
		this.npc = npc;
	}

	protected void queue(LazyPacket packet) {
		Packet<?> container = packet.provide(this.npc, null);
		this.lazyPackets.add(($, $1) -> container);
	}

	public void send() {
		this.send(Bukkit.getOnlinePlayers());
	}

	public void send(Iterable<? extends Player> players) {
		players.forEach(player -> {
			for (LazyPacket packetContainer : this.lazyPackets) {
				EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
				Packet<?> provided = packetContainer.provide(npc, entityPlayer);
				npc.sendPacket(entityPlayer, provided);
			}
		});
		this.lazyPackets.clear();
	}

	public void send(Player... targetPlayers) {
		this.send(Arrays.asList(targetPlayers));
	}

	@FunctionalInterface
	public interface LazyPacket {
		Packet<?> provide(VirtualPlayer npc, EntityPlayer player);
	}
} 
