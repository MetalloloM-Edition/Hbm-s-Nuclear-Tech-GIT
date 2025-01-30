package com.hbm.blocks.turret;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretSentryDamaged;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TurretSentryDamaged extends BlockDummyable {

	public TurretSentryDamaged(Material materialIn, String s){
		super(materialIn, s);
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 0, 0, 0, 0, 0, 0 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(meta >= 12)
			return new TileEntityTurretSentryDamaged();
		return new TileEntityProxyCombo(true, true, false);
	}


}
