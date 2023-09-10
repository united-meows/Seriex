package pisi.unitedmeows.seriex.adapters;

import static com.comphenix.protocol.PacketType.Play.Client.CHAT;
import static com.comphenix.protocol.events.ListenerPriority.HIGHEST;

import java.util.regex.Pattern;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import pisi.unitedmeows.seriex.Seriex;

public class Log4JAdapter {
	public static final Pattern LOG4J_PATTERN = Pattern.compile("\\$\\{.+}");

	public static PacketAdapter createAdapter() {
		return new PacketAdapter(Seriex.get().plugin(), HIGHEST, CHAT) {
			@Override
			public void onPacketSending(PacketEvent event) {
				StructureModifier<Object> modifiers = event.getPacket().getModifier();
				for (int i = 0; i < modifiers.size(); i++) {
					Object modifier = modifiers.read(i);
					if (modifier instanceof BaseComponent[] baseComponents
								&& (isExploit(ComponentSerializer.toString(baseComponents)) || isExploit(BaseComponent.toPlainText(baseComponents)))) {
						Seriex.get().logger().error("{} attempted the Log4J exploit. (0x1)", event.getPlayer().getName());
						event.setCancelled(true);
						return;
					}
				}
				StructureModifier<WrappedChatComponent> chatComponents = event.getPacket().getChatComponents();
				for (int i = 0; i < chatComponents.size(); i++) {
					WrappedChatComponent chatComponent = chatComponents.read(i);
					if (chatComponent == null)
						continue;

					String json = chatComponent.getJson();
					if (isExploit(json) || isExploit(toPlainText(json))) {
						Seriex.get().logger().error("{} attempted the Log4J exploit. (0x2)", event.getPlayer().getName());
						event.setCancelled(true);
						return;
					}
				}
				StructureModifier<WrappedChatComponent[]> chatComponentArrays = event.getPacket().getChatComponentArrays();
				for (int i = 0; i < chatComponentArrays.size(); i++) {
					WrappedChatComponent[] chatComponentsArray = chatComponentArrays.read(i);
					if (chatComponentsArray == null)
						continue;

					for (WrappedChatComponent chatComponent : chatComponentsArray) {
						String json = chatComponent.getJson();
						if (isExploit(json) || isExploit(toPlainText(json))) {
							Seriex.get().logger().error("{} attempted the Log4J exploit. (0x3)", event.getPlayer().getName());
							event.setCancelled(true);
							return;
						}
					}
				}
			}

			@Override
			public void onPacketReceiving(final PacketEvent event) {
				PacketContainer container = new PacketContainer(PacketType.Play.Client.CHAT);
				String chatMessage = container.getStrings().read(0);
				if (isExploit(chatMessage)) {
					Seriex.get().logger().error("{} attempted the Log4J exploit. (0x4)", event.getPlayer().getName());
					event.setCancelled(true);
				}
			}
		};
	}

	private static String toPlainText(String json) {
		return BaseComponent.toPlainText(ComponentSerializer.parse(json));
	}

	private static boolean isExploit(String message) {
		if (message == null) return false;
		return LOG4J_PATTERN.matcher(message).find();
	}
}
