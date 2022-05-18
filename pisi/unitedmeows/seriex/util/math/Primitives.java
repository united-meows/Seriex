package pisi.unitedmeows.seriex.util.math;

public class Primitives {

	public static long unsignedInt(int i) {
		return i & 0xFFFFFFFFL;
	}

	public static int unsignedShort(int s) {
		return s & 0xFFFF;
	}

	public static int unsignedByte(int b) {
		return b & 0xFF;
	}

	private static final long k0 = 0xD6D018F5L;
	private static final long k1 = 0xA2AA033BL;
	private static final long k2 = 0x62992FC1L;
	private static final long k3 = 0x4cf5ad432745937fL;

	public static long hash(long seed, long off) {
		long h = ~(((seed + k2) * k0 * off) + ~k3);
		long v0 = h;
		long v1 = h;
		long v2 = h;
		long v3 = h;
		v2 ^= Long.rotateRight(((v0 + v3) * k0) + v1, 37) * k1;
		v3 ^= Long.rotateRight(((v1 + v2) * k1) + v0, 37) * k0;
		v0 ^= Long.rotateRight(((v0 + v2) * k0) + v3, 37) * k1;
		v1 ^= Long.rotateRight(((v1 + v3) * k1) + v2, 37) * k0;
		h += v0 ^ v1;
		return finalize(h);
	}

	private static long finalize(long h) {
		h ^= Long.rotateRight(h, 28);
		h *= k0;
		h ^= Long.rotateRight(h, 29);
		return h;
	}
}
