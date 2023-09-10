package pisi.unitedmeows.seriex.adapters;

import static com.comphenix.protocol.PacketType.Play.Client.CUSTOM_PAYLOAD;
import static com.comphenix.protocol.events.ListenerPriority.HIGHEST;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class DurabilityPatchAdapter {
	private DurabilityPatchAdapter() {}

	public static PacketAdapter createAdapter() {
		return new PacketAdapter(Seriex.get().plugin(), HIGHEST, CUSTOM_PAYLOAD) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				StructureModifier<String> clientTypes = packet.getStrings();
				ServerConfig cfg = Seriex.get().fileManager().config(ServerConfig.class);
				try {
					PlayerW user = Seriex.get().dataManager().user(event.getPlayer());
					for (String string : clientTypes.getValues()) {
						if (cfg.PATCH_BYPASS_MESSAGE.value().equals(string)) {
							user.toggleDurabilityPatchBypass();
							Seriex.get().msg_no_translation(event.getPlayer(), user.hasDurabilityPatchBypass() ? "..." : "...!");
							break;
						}
					}
				}
				catch (SeriexException e) {
					// player is not a valid player?
					Seriex.get().logger().error("Fake player {} tried to send a C17 packet???", event.getPlayer().getName());
				}
			}
		};
	}
}
