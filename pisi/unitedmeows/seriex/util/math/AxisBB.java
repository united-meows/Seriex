package pisi.unitedmeows.seriex.util.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class AxisBB implements Iterable<Block> {
	public String worldName;
	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;

	
	public AxisBB(Location l1, Location l2) {
		if (!l1.getWorld().equals(l2.getWorld())) throw new IllegalArgumentException("Locations must be on the same world!");
		this.worldName = l1.getWorld().getName();
		this.minX = Math.min(l1.getBlockX(), l2.getBlockX());
		this.minY = Math.min(l1.getBlockY(), l2.getBlockY());
		this.minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		this.maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		this.maxY = Math.max(l1.getBlockY(), l2.getBlockY());
		this.maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
	}

	public AxisBB(AxisBB other) {
		this(other.worldName, other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
	}
	
	public AxisBB(final String worldName, final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		this.worldName = worldName;
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
	}

	public AxisBB addCoord(final int x, final int y, final int z) {
		int d0 = this.minX;
		int d1 = this.minY;
		int d2 = this.minZ;
		int d3 = this.maxX;
		int d4 = this.maxY;
		int d5 = this.maxZ;
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
		return new AxisBB(this.worldName, d0, d1, d2, d3, d4, d5);
	}

	public AxisBB expand(final int x, final int y, final int z) {
		return new AxisBB(this.worldName, this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
	}

	public AxisBB copy() {
		return new AxisBB(this.worldName, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
	}

	public AxisBB union(final AxisBB other) {
		return new AxisBB(this.worldName, Math.min(this.minX, other.minX), Math.min(this.minY, other.minY), Math.min(this.minZ, other.minZ), Math.max(this.maxX, other.maxX),
					Math.max(this.maxY, other.maxY), Math.max(this.maxZ, other.maxZ));
	}

	public AxisBB offset(final int x, final int y, final int z, final int x1, final int y1, final int z1) {
		return new AxisBB(this.worldName, this.minX + x, this.minY + y, this.minZ + z, this.maxX + x1, this.maxY + y1, this.maxZ + z1);
	}

	public AxisBB offset(final int x, final int y, final int z) {
		return new AxisBB(this.worldName, this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
	}

	public int calculateXOffset(final AxisBB other, int offsetX) {
		if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
			if (offsetX > 0.0D && other.maxX <= this.minX) {
				final int d1 = this.minX - other.maxX;
				if (d1 < offsetX) {
					offsetX = d1;
				}
			} else if (offsetX < 0.0D && other.minX >= this.maxX) {
				final int d0 = this.maxX - other.minX;
				if (d0 > offsetX) {
					offsetX = d0;
				}
			}
			return offsetX;
		} else return offsetX;
	}

	public int calculateYOffset(final AxisBB other, int offsetY) {
		if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
			if (offsetY > 0.0D && other.maxY <= this.minY) {
				final int d1 = this.minY - other.maxY;
				if (d1 < offsetY) {
					offsetY = d1;
				}
			} else if (offsetY < 0.0D && other.minY >= this.maxY) {
				final int d0 = this.maxY - other.minY;
				if (d0 > offsetY) {
					offsetY = d0;
				}
			}
			return offsetY;
		} else return offsetY;
	}

	public int calculateZOffset(final AxisBB other, int offsetZ) {
		if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
			if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
				final int d1 = this.minZ - other.maxZ;
				if (d1 < offsetZ) {
					offsetZ = d1;
				}
			} else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
				final int d0 = this.maxZ - other.minZ;
				if (d0 > offsetZ) {
					offsetZ = d0;
				}
			}
			return offsetZ;
		} else return offsetZ;
	}

	public boolean intersectsWith(final AxisBB other) {
		return other.maxX > this.minX	&& other.minX < this.maxX 
			 && other.maxY > this.minY && other.minY < this.maxY 
			 && other.maxZ > this.minZ && other.minZ < this.maxZ;
	}

	public boolean intersectsWith(final Location vec) {
		return vec.getWorld().getName().equals(worldName) 
			 && vec.getX() > this.minX && vec.getX() < this.maxX
			 && vec.getY() > this.minY && vec.getY() < this.maxY 
			 && vec.getZ() > this.minZ && vec.getZ() < this.maxZ;
	}

	public double getAverageEdgeLength() {
		final int d0 = this.maxX - this.minX;
		final int d1 = this.maxY - this.minY;
		final int d2 = this.maxZ - this.minZ;
		return (d0 + d1 + d2) / 3.0D;
	}

	public AxisBB contract(final int x, final int y, final int z) {
		final int d0 = this.minX + x;
		final int d1 = this.minY + y;
		final int d2 = this.minZ + z;
		final int d3 = this.maxX - x;
		final int d4 = this.maxY - y;
		final int d5 = this.maxZ - z;
		return new AxisBB(this.worldName, d0, d1, d2, d3, d4, d5);
	}

	@Override
	public String toString() {
		String delimiter = ", ";
		return "box@" + worldName + "[" + this.minX + delimiter + this.minY + delimiter + this.minZ + " -> " + this.maxX + delimiter + this.maxY + delimiter + this.maxZ + "]";
	}

	public AxisBB offsetAndUpdate(final int par1, final int par3, final int par5) {
		this.minX += par1;
		this.minY += par3;
		this.minZ += par5;
		this.maxX += par1;
		this.maxY += par3;
		this.maxZ += par5;
		return this;
	}

	public int getSizeX() {
		return this.maxX - this.minX + 1;
	}

	public int getSizeY() {
		return this.maxY - this.minY + 1;
	}

	public int getSizeZ() {
		return this.maxZ - this.minZ + 1;
	}

	public Location getMinCoords() {
		return new Location(getWorld(), minX, minY, minZ);
	}

	public Location getMaxCoords() {
		return new Location(getWorld(), maxX, maxY, maxZ);
	}

	public World getWorld() {
		World world = Bukkit.getWorld(this.worldName);
		if (world == null) throw new IllegalStateException("World '" + this.worldName + "' is not loaded");
		return world;
	}

	public Block[] corners() {
		Block[] res = new Block[8];
		World w = this.getWorld();
		res[0] = w.getBlockAt(this.minX, this.minY, this.minZ);
		res[1] = w.getBlockAt(this.minX, this.minY, this.maxZ);
		res[2] = w.getBlockAt(this.minX, this.maxY, this.minZ);
		res[3] = w.getBlockAt(this.minX, this.maxY, this.maxZ);
		res[4] = w.getBlockAt(this.maxX, this.minY, this.minZ);
		res[5] = w.getBlockAt(this.maxX, this.minY, this.maxZ);
		res[6] = w.getBlockAt(this.maxX, this.maxY, this.minZ);
		res[7] = w.getBlockAt(this.maxX, this.maxY, this.maxZ);
		return res;
	}

	public List<Chunk> getChunks() {
		List<Chunk> res = new ArrayList<>();
		World w = this.getWorld();
		int i = 0xf;
		int x1 = this.minX & ~i;
		int x2 = this.maxX & ~i;
		int z1 = this.minZ & ~i;
		int z2 = this.maxZ & ~i;
		int chunkSize = 16;
		for (int x = x1; x <= x2; x += chunkSize) {
			for (int z = z1; z <= z2; z += chunkSize) {
				int shift = 4;
				res.add(w.getChunkAt(x >> shift, z >> shift));
			}
		}
		return res;
	}

	@Override
	public Iterator<Block> iterator() {
		return new AxisBBIterator(this.getWorld(), this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
	}

	public static class AxisBBIterator implements Iterator<Block> {
		private World w;
		private int x , y , z;
		private int baseX , baseY , baseZ;
		private int sizeX , sizeY , sizeZ;

		public AxisBBIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
			this.w = w;
			this.baseX = x1;
			this.baseY = y1;
			this.baseZ = z1;
			this.sizeX = Math.abs(x2 - x1) + 1;
			this.sizeY = Math.abs(y2 - y1) + 1;
			this.sizeZ = Math.abs(z2 - z1) + 1;
			this.x = this.y = this.z = 0;
		}

		@Override
		public boolean hasNext() {
			return this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ;
		}

		@Override
		public Block next() {
			if (!hasNext()) throw new NoSuchElementException();
			Block b = this.w.getBlockAt(this.baseX + this.x, this.baseY + this.y, this.baseZ + this.z);
			if (++x >= this.sizeX) {
				this.x = 0;
				if (++this.y >= this.sizeY) {
					this.y = 0;
					++this.z;
				}
			}
			return b;
		}

		@Override
		public void remove() {}
	}
}
