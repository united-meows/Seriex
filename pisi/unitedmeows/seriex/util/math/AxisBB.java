package pisi.unitedmeows.seriex.util.math;

import org.bukkit.Location;

public class AxisBB {
	public double minX;
	public double minY;
	public double minZ;
	public double maxX;
	public double maxY;
	public double maxZ;

	public AxisBB(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
	}

	public AxisBB addCoord(final double x, final double y, final double z) {
		double d0 = this.minX;
		double d1 = this.minY;
		double d2 = this.minZ;
		double d3 = this.maxX;
		double d4 = this.maxY;
		double d5 = this.maxZ;
		if (x < 0.0D) {
			d0 += x;
		} else if (x > 0.0D) {
			d3 += x;
		}
		if (y < 0.0D) {
			d1 += y;
		} else if (y > 0.0D) {
			d4 += y;
		}
		if (z < 0.0D) {
			d2 += z;
		} else if (z > 0.0D) {
			d5 += z;
		}
		return new AxisBB(d0, d1, d2, d3, d4, d5);
	}

	public AxisBB expand(final double x, final double y, final double z) {
		final double d0 = this.minX - x;
		final double d1 = this.minY - y;
		final double d2 = this.minZ - z;
		final double d3 = this.maxX + x;
		final double d4 = this.maxY + y;
		final double d5 = this.maxZ + z;
		return new AxisBB(d0, d1, d2, d3, d4, d5);
	}

	public AxisBB copy() {
		final double var7 = this.minX;
		final double var9 = this.minY;
		final double var11 = this.minZ;
		final double var13 = this.maxX;
		final double var15 = this.maxY;
		final double var17 = this.maxZ;
		return new AxisBB(var7, var9, var11, var13, var15, var17);
	}

	public AxisBB union(final AxisBB other) {
		final double d0 = Math.min(this.minX, other.minX);
		final double d1 = Math.min(this.minY, other.minY);
		final double d2 = Math.min(this.minZ, other.minZ);
		final double d3 = Math.max(this.maxX, other.maxX);
		final double d4 = Math.max(this.maxY, other.maxY);
		final double d5 = Math.max(this.maxZ, other.maxZ);
		return new AxisBB(d0, d1, d2, d3, d4, d5);
	}

	public static AxisBB fromBounds(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		final double d0 = Math.min(x1, x2);
		final double d1 = Math.min(y1, y2);
		final double d2 = Math.min(z1, z2);
		final double d3 = Math.max(x1, x2);
		final double d4 = Math.max(y1, y2);
		final double d5 = Math.max(z1, z2);
		return new AxisBB(d0, d1, d2, d3, d4, d5);
	}

	public AxisBB offset(final double x, final double y, final double z, final double x1, final double y1, final double z1) {
		return new AxisBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x1, this.maxY + y1, this.maxZ + z1);
	}

	public AxisBB offset(final double x, final double y, final double z) {
		return new AxisBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
	}

	public double calculateXOffset(final AxisBB other, double offsetX) {
		if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
			if (offsetX > 0.0D && other.maxX <= this.minX) {
				final double d1 = this.minX - other.maxX;
				if (d1 < offsetX) {
					offsetX = d1;
				}
			} else if (offsetX < 0.0D && other.minX >= this.maxX) {
				final double d0 = this.maxX - other.minX;
				if (d0 > offsetX) {
					offsetX = d0;
				}
			}
			return offsetX;
		} else return offsetX;
	}

	public double calculateYOffset(final AxisBB other, double offsetY) {
		if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
			if (offsetY > 0.0D && other.maxY <= this.minY) {
				final double d1 = this.minY - other.maxY;
				if (d1 < offsetY) {
					offsetY = d1;
				}
			} else if (offsetY < 0.0D && other.minY >= this.maxY) {
				final double d0 = this.maxY - other.minY;
				if (d0 > offsetY) {
					offsetY = d0;
				}
			}
			return offsetY;
		} else return offsetY;
	}

	public double calculateZOffset(final AxisBB other, double offsetZ) {
		if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
			if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
				final double d1 = this.minZ - other.maxZ;
				if (d1 < offsetZ) {
					offsetZ = d1;
				}
			} else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
				final double d0 = this.maxZ - other.minZ;
				if (d0 > offsetZ) {
					offsetZ = d0;
				}
			}
			return offsetZ;
		} else return offsetZ;
	}

	public boolean intersectsWith(final AxisBB other) {
		return other.maxX > this.minX && other.minX < this.maxX ? (other.maxY > this.minY && other.minY < this.maxY ? other.maxZ > this.minZ && other.minZ < this.maxZ : false) : false;
	}

	public boolean isLocInside(final Location vec) {
		return vec.getX() > this.minX && vec.getX() < this.maxX ? (vec.getY() > this.minY && vec.getY() < this.maxY ? vec.getZ() > this.minZ && vec.getZ() < this.maxZ : false) : false;
	}

	public double getAverageEdgeLength() {
		final double d0 = this.maxX - this.minX;
		final double d1 = this.maxY - this.minY;
		final double d2 = this.maxZ - this.minZ;
		return (d0 + d1 + d2) / 3.0D;
	}

	public AxisBB contract(final double x, final double y, final double z) {
		final double d0 = this.minX + x;
		final double d1 = this.minY + y;
		final double d2 = this.minZ + z;
		final double d3 = this.maxX - x;
		final double d4 = this.maxY - y;
		final double d5 = this.maxZ - z;
		return new AxisBB(d0, d1, d2, d3, d4, d5);
	}

	@Override
	public String toString() {
		return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
	}

	public boolean hasNaN() {
		return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
	}

	public AxisBB offsetAndUpdate(final double par1, final double par3, final double par5) {
		this.minX += par1;
		this.minY += par3;
		this.minZ += par5;
		this.maxX += par1;
		this.maxY += par3;
		this.maxZ += par5;
		return this;
	}
}
