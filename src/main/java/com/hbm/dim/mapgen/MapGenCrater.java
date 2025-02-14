package com.hbm.dim.mapgen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class MapGenCrater extends MapGenBase {
    
	private int chancePerChunk = 100;
	private int minSize = 8;
	private int maxSize = 64;

	public Block regolith;
	public Block rock;

	// Note that the chance is effectively squared, so make it lower than you normally would
	public MapGenCrater(int chancePerChunk) {
		this.chancePerChunk = chancePerChunk;
	}

	public void setSize(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;

		this.range = (maxSize / 8) + 1;
	}

	private double depthFunc(double x, double rad, double depth) {
		return -Math.pow(x, 4) / Math.pow(rad, 4) * depth + depth;
	}

	// This function is looped over from -this.range to +this.range on both XZ axes.
	@Override
	protected void recursiveGenerate(World world, int offsetX, int offsetZ, int chunkX, int chunkZ, ChunkPrimer chunkPrimer) {

		if (rand.nextInt(chancePerChunk) == Math.abs(offsetX) % chancePerChunk && rand.nextInt(chancePerChunk) == Math.abs(offsetZ) % chancePerChunk) {

			double radius = rand.nextInt(maxSize - minSize) + minSize;
			double depth = radius * 0.35D;

			int xCoord = -offsetX + chunkX;
			int zCoord = -offsetZ + chunkZ;

			for (int bx = 15; bx >= 0; bx--) { // bx, bz is the coordinate of the block we're modifying, relative to the generating chunk origin
				for (int bz = 15; bz >= 0; bz--) {
					for (int y = 254; y >= 0; y--) {
						IBlockState currentState = chunkPrimer.getBlockState(bx, y, bz);

						if (currentState != null && (currentState.isOpaqueCube() || currentState.getMaterial().isLiquid())) {
							// x, z are the coordinates relative to the target virtual chunk origin
							int x = xCoord * 16 + bx;
							int z = zCoord * 16 + bz;

							// y is at the current height now
							double r = Math.sqrt(x * x + z * z);

							if (r - rand.nextInt(3) <= radius) {
								// Carve out to intended depth
								int dep = (int) MathHelper.clamp(depthFunc(r, radius, depth), 0, y - 1);
								for (int i = 0; i < dep; i++) {
									chunkPrimer.setBlockState(bx, y - i, bz, Blocks.AIR.getDefaultState());
								}

								y -= dep;

								dep = Math.min(3, y - 1);

								// Fill back in
								if (r + rand.nextInt(3) <= radius / 3D) {
									for (int i = 0; i < dep; i++) {
										chunkPrimer.setBlockState(bx, y - i, bz, regolith.getDefaultState());
									}
								} else {
									for (int i = 0; i < dep; i++) {
										chunkPrimer.setBlockState(bx, y - i, bz, rock.getDefaultState());
									}
								}
							}

							break;
						}
					}
				}
			}
		}
	}
}
