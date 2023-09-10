package pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl;

import com.google.common.collect.Lists;

import dev.derklaro.reflexion.Reflexion;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.VirtualModifier;

public class VisibilityModifier extends VirtualModifier {

	public VisibilityModifier(VirtualPlayer npc) {
		super(npc);
	}

	public VisibilityModifier queuePlayerListChange(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action) {
		super.queue((targetNpc, target) -> {
			PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
			Reflexion.on(packetPlayOutPlayerInfo).findField("a").orElseThrow().setValue(packetPlayOutPlayerInfo, action);
			PlayerInfoData data = packetPlayOutPlayerInfo.new PlayerInfoData(
						targetNpc.virtualProfile().toGameProfile(),  // profile
						-2173,													// latency
						EnumGamemode.SURVIVAL, 								// gamemode
						new ChatComponentText("") 						// tab displayName
			);
			Reflexion.on(packetPlayOutPlayerInfo).findField("b").orElseThrow().setValue(packetPlayOutPlayerInfo, Lists.newArrayList(data));
			return packetPlayOutPlayerInfo;
		});
		return this;
	}

	public VisibilityModifier queueNamedEntity() {
		super.queue((targetNpc, target) -> new PacketPlayOutNamedEntitySpawn(targetNpc.entityPlayer()));
		return this;
	}

	public VisibilityModifier queueSpawn() {
		this.queuePlayerListChange(EnumPlayerInfoAction.ADD_PLAYER);
		this.queueNamedEntity();
		EntityPlayer entityPlayer = npc.entityPlayer();
		entityPlayer.world.players.add(entityPlayer);
		entityPlayer.world.addEntity(entityPlayer);
		return this;
	}

	public VisibilityModifier queueDestroy() {
		super.queue((targetNpc, target) -> new PacketPlayOutEntityDestroy(targetNpc.entityID()));
		return this;
	}

	public VisibilityModifier queueRemove() {
		this.queueDestroy();
		EntityPlayer entityPlayer = npc.entityPlayer();
		entityPlayer.world.players.remove(entityPlayer);
		entityPlayer.world.removeEntity(entityPlayer);
		Seriex.get().virtualPlayerManager().remove(npc);
		return this;
	}
}
