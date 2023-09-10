package pisi.unitedmeows.seriex.util;

public enum Permissions {
	NO_ANTICHEAT_RESTRICTIONS("seriex.no_anticheat_restrictions");

	private final String perm;

	Permissions(String _perm) {
		this.perm = _perm;
	}

	public String permission() {
		return perm;
	}
}
