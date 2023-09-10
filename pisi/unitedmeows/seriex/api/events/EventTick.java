package pisi.unitedmeows.seriex.api.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventTick extends Event {
	private int ticks;
	
	public EventTick(int ticks) {
		this.ticks = ticks;
	}

	public int tick() {
		return ticks;
	}
}
