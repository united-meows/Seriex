package pisi.unitedmeows.seriex.managers.virtualplayers.misc;

import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;

public class VirtualNetwork extends NetworkManager {
	public VirtualNetwork(EnumProtocolDirection direction) {
		super(direction);
	}
}
