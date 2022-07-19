package pisi.unitedmeows.seriex.adapters;

import static com.comphenix.protocol.PacketType.Status.Client.*;
import static com.comphenix.protocol.PacketType.Status.Server.*;
import static com.comphenix.protocol.events.ListenerPriority.*;
import static org.bukkit.ChatColor.*;
import static pisi.unitedmeows.seriex.Seriex.*;

import org.bukkit.ChatColor;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;

public class MOTDAdapter {
	private static final String TAB = "                                                                                         ";

	public PacketAdapter createAdapter(final ProtocolManager protocolManager) {
		final WrappedServerPing wrappedServerPing = new WrappedServerPing();
		final PacketAdapter packetAdapter = new PacketAdapter(get(), HIGHEST, START) {
			@Override
			public void onPacketReceiving(final PacketEvent event) {
				final PacketContainer packetContainer = new PacketContainer(SERVER_INFO);
				packetContainer.getServerPings().write(0, wrappedServerPing);
				try {
					protocolManager.sendServerPacket(event.getPlayer(), packetContainer);
					final StringBuilder motd = new StringBuilder();
					motd.append(ChatColor.BLUE);
					motd.append(BOLD);
					motd.append("discord.gg");
					motd.append(DARK_GRAY);
					motd.append(BOLD);
					motd.append("/");
					motd.append(DARK_PURPLE);
					motd.append(BOLD);
					String value = ((DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD)).INVITE_LINK.value();
					motd.append(value.replace("discord.gg/", ""));
					motd.append("\n");
					motd.append(LIGHT_PURPLE);
					motd.append(get().motd());
					wrappedServerPing.setMotD(motd.toString());
					final StringBuilder versionName = new StringBuilder();
					versionName.append(ChatColor.WHITE);
					versionName.append("New Seriex is now live!");
					versionName.append(TAB);
					versionName.append(LIGHT_PURPLE);
					versionName.append(get().getServer().getOnlinePlayers().size());
					versionName.append(DARK_GRAY);
					versionName.append("/");
					versionName.append(DARK_PURPLE);
					versionName.append("-2173");
					wrappedServerPing.setVersionName(versionName.toString());
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
				event.setCancelled(true);
			}
		};
		return packetAdapter;
	}
}
