package com.hbm.entity.missile;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityMissileStealth extends EntityMissileBaseNT {

	public EntityMissileStealth(World world) { super(world); }
	public EntityMissileStealth(World world, float x, float y, float z, int a, int b) { super(world, x, y, z, a, b); }

	@Override
	public List<ItemStack> getDebris() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		//list.add(new ItemStack(ModItems.bolt, 4, Mats.MAT_STEEL.id));
		return list;
	}

	@Override public ItemStack getMissileItemForInfo() { return new ItemStack(ModItems.missile_stealth); }
	@Override public boolean canBeSeenBy(Object radar) { return false; }
	
	@Override public void onImpact() { ExplosionLarge.explode(world, posX, posY, posZ, 20F, true, false, false); }
	@Override public ItemStack getDebrisRareDrop() { return DictFrame.fromOne(ModItems.powder_ash, EnumAshType.MISC); }

}
