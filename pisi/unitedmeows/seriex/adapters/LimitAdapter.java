package pisi.unitedmeows.seriex.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.temporary.TemporaryPlayer;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.collections.bucket.PacketBucket;
import pisi.unitedmeows.seriex.util.config.single.impl.PacketLimiterConfig;

public class LimitAdapter extends PacketAdapter implements Listener {

	private final ConcurrentHashMap<UUID, PlayerBucketHolder> bucketHolders = new ConcurrentHashMap<>();
	private int maximumThreshold;

	public LimitAdapter() {
		super(Seriex.get().plugin(), ListenerPriority.LOWEST, availableClientPackets(), ListenerOptions.ASYNC);

		for (final Player player : Bukkit.getOnlinePlayers()) {
			this.bucketHolders.put(player.getUniqueId(), new PlayerBucketHolder(player.getUniqueId()));
		}

		final PacketLimiterConfig config = Seriex.get().fileManager().config(PacketLimiterConfig.class);
		this.maximumThreshold = config.PACKET_THRESHOLD.value().intValue();
	}

	private static List<PacketType> availableClientPackets() {
		final List<PacketType> packets = new ArrayList<>();
		for (final PacketType type : PacketType.values()) {
			if (type.isClient() && type.getProtocol() == PacketType.Protocol.PLAY && type.isSupported()) {
				packets.add(type);
			}
		}
		return packets;
	}

	@Override
	public void onPacketReceiving(final PacketEvent event) {
		if (event.isCancelled())
			return;
		final Player player = event.getPlayer();
		if (player instanceof TemporaryPlayer || player == null) {
			return; // uuid isnt available
		}
		final UUID uuid = player.getUniqueId();
		PlayerBucketHolder info = this.bucketHolders.get(uuid);
		if (info == null)
			return;

		final PacketBucket bucket = info.packets;

		synchronized (bucket) {
			final PlayerBucketHolder currInfo = this.bucketHolders.get(uuid);
			if (currInfo != info) {
				Seriex.get().logger().error("Desync between buckets (should not happen very often...)");
				return;
			}

			if (info.violatedLimit) {
				event.setCancelled(true);
				return;
			}

			final int packets = bucket.incrementPackets(1);
			if (bucket.getCurrentPacketRate() > maximumThreshold) {
				info.violatedLimit = true;
				event.setCancelled(true);

				Seriex.get().run(() -> {
					final Player target = Bukkit.getPlayer(uuid);
					if (target == null)
						return;
					Seriex.get().kick_no_translation(player, "Too many packets (%s packets in the last %s seconds)", packets, bucket.intervalTime / 1000.0);
					Seriex.get().logger().info("Player {} was kicked for sending too many packets. ({} packets in the last {} seconds)", target.getName(), packets, bucket.intervalTime / 1000.0);
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR , ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final UUID player = event.getPlayer().getUniqueId();
		this.bucketHolders.put(player, new PlayerBucketHolder(player));
	}

	@EventHandler(priority = EventPriority.MONITOR , ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		this.bucketHolders.remove(event.getPlayer().getUniqueId());
	}

	public static final class PlayerBucketHolder {
		public final PacketBucket packets;
		public final UUID player;
		public boolean violatedLimit;

		public PlayerBucketHolder(final UUID player) {
			final PacketLimiterConfig config = Seriex.get().fileManager().config(PacketLimiterConfig.class);
			this.packets = new PacketBucket(config.BUCKET_INTERVAL.value(), config.BUCKET_AMOUNT.value().intValue());
			this.player = player;
		}
	}
}
