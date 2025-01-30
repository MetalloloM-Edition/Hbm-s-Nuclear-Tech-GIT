package com.hbm.blocks.turret;

import java.util.Random;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretSentry;


import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class TurretSentry extends BlockDummyable {

	public TurretSentry(Material materialIn, String s){
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(meta >= 12)
			return new TileEntityTurretSentry();
		return new TileEntityProxyCombo(true, true, false);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		return super.standardOpenBehavior(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, 0);
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 0, 0, 0, 0 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	Random rand = new Random();
/*
	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int meta) {

		TileEntityTurretSentry sentry = (TileEntityTurretSentry) world.getTileEntity(x, y, z);

		if(sentry != null) {
			for(int i = 0; i < sentry.getSizeInventory(); ++i) {
				ItemStack itemstack = sentry.getStackInSlot(i);

				if(itemstack != null) {
					float oX = this.rand.nextFloat() * 0.8F + 0.1F;
					float oY = this.rand.nextFloat() * 0.8F + 0.1F;
					float oZ = this.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.stackSize > 0) {
						int toDrop = this.rand.nextInt(21) + 10;

						if(toDrop > itemstack.stackSize) {
							toDrop = itemstack.stackSize;
						}

						itemstack.stackSize -= toDrop;
						EntityItem entityitem = new EntityItem(world, x + oX, y + oY, z + oZ, new ItemStack(itemstack.getItem(), toDrop, itemstack.getItemDamage()));

						if(itemstack.hasTagCompound()) {
							entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
						}

						float jump = 0.05F;
						entityitem.motionX = (float) this.rand.nextGaussian() * jump;
						entityitem.motionY = (float) this.rand.nextGaussian() * jump + 0.2F;
						entityitem.motionZ = (float) this.rand.nextGaussian() * jump;
						world.spawnEntityInWorld(entityitem);
					}
				}
			}

			world.func_147453_f(x, y, z, b);
		}

		super.breakBlock(world, x, y, z, b, meta);
	}
*/
}
