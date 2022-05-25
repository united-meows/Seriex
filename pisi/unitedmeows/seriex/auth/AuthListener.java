package pisi.unitedmeows.seriex.auth;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.pispigot.Pispigot;
import pisi.unitedmeows.pispigot.event.impl.client.*;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.auth.util.Authentication;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.data.DataManager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class AuthListener extends Manager implements org.bukkit.event.Listener {

	private Map<PlayerW, Authentication> playerMap = new HashMap<>();

	@Override
	public void start(Seriex seriex) {
		Bukkit.getPluginManager().registerEvents(this, Seriex.get());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final PlayerW playerW = Seriex.get().dataManager().addUser(event.getPlayer());
		final Authentication authentication = new Authentication(playerW);
		authentication.start();


		playerMap.put(playerW, authentication);

		Pispigot.playerSystem(event.getPlayer()).subscribeAll(this);
	}

	private Listener<C03PacketPlayer> walkingListener = new Listener<C03PacketPlayer>(packet -> {
		packet.setCanceled(true);
	}).listen(C04PacketPlayerPosition.class, C05PacketPlayerLook.class, C06PacketPlayerPosLook.class)
			.weight(Event.Weight.MASTER);

	private Listener<C14PacketTabComplete> c14PacketTabCompleteListener = new Listener<C14PacketTabComplete>(packet -> {
		packet.setCanceled(true);
	}).weight(Event.Weight.MASTER);


	@Override
	public void cleanup() throws SeriexException {
		playerMap.forEach((k, v) -> v.close());
		playerMap.clear();
	}

	public void stopAuthentication(PlayerW playerW) {
		final Authentication authentication = playerMap.remove(playerW);
		authentication.close();
	}
}
