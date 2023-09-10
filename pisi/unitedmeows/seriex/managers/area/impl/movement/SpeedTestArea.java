package pisi.unitedmeows.seriex.managers.area.impl.movement;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.impl.Area;
import pisi.unitedmeows.seriex.managers.area.impl.AreaBase;
import pisi.unitedmeows.seriex.managers.area.impl.AreaData;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.LocationPointer;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

@AreaData(base = AreaBase.SPEEDTEST , autoJoin = false)
public class SpeedTestArea extends Area {
	// TODO: pointer
	private static final double END_DISTANCE = 2.5D;

	public SpeedTestArea(String cfgName) {
		super(cfgName);
	}

	private Player player;
	private Location previousLocation;
	private long startTime;
	private boolean canMove;
	public boolean inUse;

	@Override
	public void start() {
		add_ptr("start_location", new LocationPointer(warpLocation));
		add_ptr("end_location", new LocationPointer(warpLocation));
	}

	@Override
	public void enter(Player player) {
		this.player = player;
		this.inUse = true;
		this.previousLocation = player.getLocation();
		Location start_location = ((LocationPointer) get_ptr("start_location")).data();
		Location end_location = ((LocationPointer) get_ptr("end_location")).data();

		player.teleport(start_location);
		this.canMove = false;
		final int maxTime = 3;
		for (int i = maxTime; i >= 0; i--) {
			final int finalized = i;
			Seriex.get().runLater(() -> {
				PlayerW user = Seriex.get().dataManager().user(player);
				if (finalized != 0) {
					user.denyMovement();
					Location origin = start_location.clone();
					Location target = end_location.clone();
					float[] optimalRotation = {
								origin.setDirection(target.subtract(origin.toVector()).toVector()).getYaw(),
								0.0F
					};
					Seriex.get().msg(player, Messages.AREA_SPEEDTEST_STARTING, finalized);
					((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(player.getEntityId(),
								(byte) (optimalRotation[0] * 256F / 360F),
								(byte) (optimalRotation[1] * 256F / 360F), true));
				} else {
					user.allowMovement();
					Seriex.get().msg(player, Messages.AREA_SPEEDTEST_STARTED);
					this.canMove = true;
					this.startTime = System.currentTimeMillis();
					Seriex.get().runLater(() -> {
						if(inUse && canMove) {
							this.leave(player);
							Seriex.get().msg(player, Messages.AREA_SPEEDTEST_TOOK_TOO_LONG);
						}
					}, 10 * 20L /* 10 seconds */);
				}
			}, (maxTime + 1) * 20L - 20L * i);
		}
	}

	@Override
	public void leave(Player player) {
		this.inUse = false;
		Location finishLocation = player.getLocation();
		player.teleport(previousLocation);
		Location start_location = ((LocationPointer) get_ptr("start_location")).data();
		long ticks = (System.currentTimeMillis() - startTime) / 50L;
		Seriex.get().msg(player, Messages.AREA_SPEEDTEST_RESULT,
					ticks,
					start_location.distance(finishLocation) / ticks);
	}

	@Override
	public boolean move(Player player) {
		if (!canMove) {
			Location start_location = ((LocationPointer) get_ptr("start_location")).data();
			player.teleport(start_location);
			return true;
		}
		Location end_location = ((LocationPointer) get_ptr("end_location")).data();
		if (player.getLocation().clone().toVector().setY(0).distance(end_location.clone().toVector().setY(0)) < END_DISTANCE) {
			this.handleLeave(player);
		}
		return false;
	}

	@Override
	protected boolean isConfigured() {
		Location start_location = ((LocationPointer) get_ptr("start_location")).data();
		Location end_location = ((LocationPointer) get_ptr("end_location")).data();
		return !start_location.equals(warpLocation) && !end_location.equals(warpLocation);
	}
}
