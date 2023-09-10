package pisi.unitedmeows.seriex.api.events;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class EventFlag extends Event {
	private final Anticheat anticheat;
	private final PlayerW player;
	private final CheckRecord checkRecord;

	public EventFlag(Anticheat aniAnticheat, PlayerW player, CheckRecord checkRecord) {
		this.anticheat = aniAnticheat;
		this.player = player;
		this.checkRecord = checkRecord;
	}

	public Anticheat anticheat() {
		return anticheat;
	}

	public PlayerW player() {
		return player;
	}

	public CheckRecord record() {
		return checkRecord;
	}

	public static class CheckRecord {
		private final String name;
		private final boolean punishable;
		private final boolean cancellable;
		private final String violation;
		private final String log_message;

		public CheckRecord(String name, boolean punishable, boolean cancellable, String violation, String log_message) {
			this.name = name;
			this.punishable = punishable;
			this.cancellable = cancellable;
			this.violation = violation;
			this.log_message = log_message;
		}

		public String name() {
			return name;
		}

		public boolean punishable() {
			return punishable;
		}

		public boolean cancellable() {
			return cancellable;
		}

		public String violation() {
			return violation;
		}

		public String log_message() {
			return log_message;
		}
	}
}
