package com.hbm.items.tool;

import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBoltgun extends Item implements IAnimatedItem {

	public ItemBoltgun(String s) {
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.setMaxStackSize(1);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ToolType.BOLT.register(new ItemStack(this));
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		
		World world = player.world;
		if(!entity.isEntityAlive()) return false;
		
		ItemStack[] bolts = new ItemStack[] { /*new ItemStack(ModItems.bolt_spike), Mats.MAT_STEEL.make(ModItems.bolt), Mats.MAT_TUNGSTEN.make(ModItems.bolt), Mats.MAT_DURA.make(ModItems.bolt)*/};
		
		for(ItemStack bolt : bolts) {
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack slot = player.inventory.getStackInSlot(i);
				
				if(!slot.isEmpty()) {
					if(slot.getItem() == bolt.getItem() && slot.getItemDamage() == bolt.getItemDamage()) {
						if(!world.isRemote) {
							world.playSound(player, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.boltgun, SoundCategory.PLAYERS, 1.0F, 1.0F);
							player.inventory.decrStackSize(i, 1);
							player.inventoryContainer.detectAndSendChanges();
							EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, DamageSource.causePlayerDamage(player).setDamageBypassesArmor(), 10F);
							
							if(!entity.isEntityAlive() && entity instanceof EntityPlayer) {
								AdvancementManager.grantAchievement((EntityPlayer) entity, AdvancementManager.achGoFish);
							}

							NBTTagCompound data = new NBTTagCompound();
							data.setString("type", "vanillaExt");
							data.setString("mode", "largeexplode");
							data.setFloat("size", 1F);
							data.setByte("count", (byte)1);
							PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, entity.posX, entity.posY + entity.height / 2 - entity.getYOffset(), entity.posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), entity.posX, entity.posY, entity.posZ, 50));
						} else {
							// doing this on the client outright removes the packet delay and makes the animation silky-smooth
							NBTTagCompound d0 = new NBTTagCompound();
							d0.setString("type", "anim");
							d0.setString("mode", "generic");
							MainRegistry.proxy.effectNT(d0);
						}
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		Block b = world.getBlockState(pos).getBlock();
		
		if(b instanceof IToolable && ((IToolable)b).onScrew(world, player, pos.getX(), pos.getY(), pos.getZ(), facing, hitX, hitY, hitZ, hand, ToolType.BOLT)) {

			if(!world.isRemote) {

				world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.boltgun, SoundCategory.PLAYERS, 1.0F, 1.0F);
				player.inventoryContainer.detectAndSendChanges();
				ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
				double off = 0.25;

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 1F);
				data.setByte("count", (byte)1);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, pos.getX() + hitX + dir.offsetX * off, pos.getY() + hitY + dir.offsetY * off, pos.getZ() + hitZ + dir.offsetZ * off), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));

				NBTTagCompound d0 = new NBTTagCompound();
				d0.setString("type", "anim");
				d0.setString("mode", "generic");
				PacketDispatcher.wrapper.sendTo(new AuxParticlePacketNT(d0, 0, 0, 0), (EntityPlayerMP) player);
			}
			
			return EnumActionResult.FAIL;
		}
		
		return EnumActionResult.FAIL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BusAnimation getAnimation(NBTTagCompound data, ItemStack stack) {
		return new BusAnimation()
				.addBus("RECOIL", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(1, 0, 1, 50))
						.addKeyframe(new BusAnimationKeyframe(0, 0, 1, 100)));
	}
}
