package pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl;

import dev.derklaro.reflexion.Reflexion;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.VirtualModifier;

public class AnimationModifier extends VirtualModifier {

	public AnimationModifier(VirtualPlayer npc) {
		super(npc);
	}

	public AnimationModifier queue(EntityAnimation entityAnimation) {
		return this.queue(entityAnimation.id);
	}

	private AnimationModifier queue(int animationId) {
		super.queue((targetNpc, target) -> {
			PacketPlayOutAnimation animationPacket = new PacketPlayOutAnimation();
			Reflexion reflexion = Reflexion.on(animationPacket);
			reflexion.findField("a").ifPresent(field -> field.setValue(animationPacket, targetNpc.entityID()));
			reflexion.findField("b").ifPresent(field -> field.setValue(animationPacket, animationId));
			return animationPacket;
		});
		return this;
	}

	public enum EntityAnimation {
		SWING(0),
		TAKE_DAMAGE(1), // does not play sound
		LEAVE_BED(2),
		CRITICAL_EFFECT(4),
		MAGIC_CRITICAL_EFFECT(5);

		private final int id;

		EntityAnimation(int id) {
			this.id = id;
		}

		public static EntityAnimation fromString(String name) {
			for (EntityAnimation enumValue : EntityAnimation.values()) {
				if (enumValue.name().equalsIgnoreCase(name)) { return enumValue; }
			}
			return null;
		}
	}
}
