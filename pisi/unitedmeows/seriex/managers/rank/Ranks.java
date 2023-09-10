package pisi.unitedmeows.seriex.managers.rank;

import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.Seriex;

/*
 * ghost, bringing you the best worst code ever
 * enums having name() made shit confusing so, no enums
 * (RankData could be here later on)
 */
public class Ranks {
	private static final List<Ranks> rankArray = new ArrayList<>();

	public static final Ranks MAINTAINER = new Ranks(rankArray, "maintainer", 1, true);
	public static final Ranks HELPER = new Ranks(rankArray, "helper", 2);
	public static final Ranks DONATOR = new Ranks(rankArray, "donator", 3);
	public static final Ranks DEVELOPER = new Ranks(rankArray, "developer", 4);
	public static final Ranks VIP = new Ranks(rankArray, "vip", 5);
	public static final Ranks TESTER = new Ranks(rankArray, "tester", 6);

	private final boolean operator;
	private final int priority;
	private final String name;

	Ranks(List<Ranks> list, String name, int priority) {
		this(list, name, priority, false);
	}

	Ranks(List<Ranks> list, String name, int priority, boolean operator) {
		this.name = name;
		this.priority = priority;
		this.operator = operator;
		list.add(this);
	}

	public boolean operator() {
		return operator;
	}

	public int priority() {
		return priority;
	}

	public String internalName() {
		return name;
	}

	private static Ranks[] VALUES;

	public static Ranks[] values() {
		if (VALUES == null)
			VALUES = rankArray.toArray(Ranks[]::new);

		return VALUES;
	}

	public static Ranks of(String value) {
		for (Ranks rank : VALUES) {
			if (rank.internalName().equalsIgnoreCase(value))
				return rank;
		}
		Seriex.get().logger().error("No rank with the name '{}' found!", value);
		return null;
	}
}
