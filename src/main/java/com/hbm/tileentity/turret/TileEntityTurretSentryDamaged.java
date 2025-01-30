package com.hbm.tileentity.turret;

import com.hbm.handler.BulletConfiguration;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;

import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityTurretSentryDamaged extends TileEntityTurretSentry {

	@Override
	public boolean hasPower() { //does not need power
		return true;
	}

	@Override
	public boolean isOn() { //is always on
		return true;
	}

	@Override
	public double getTurretYawSpeed() {
		return 3D;
	}

	@Override
	public double getTurretPitchSpeed() {
		return 2D;
	}

	@Override
	public boolean hasThermalVision() {
		return false;
	}
	
	@Override
	public boolean entityAcceptableTarget(Entity e) { //will fire at any living entity
		
		if(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode)
			return false;
		
		return e instanceof EntityLivingBase;
	}

	@Override
	public void updateFiringTick() {
		
		timer++;
		
		if(timer % 10 == 0) {

			BulletConfiguration conf = this.getFirstConfigLoaded();
			
			if(conf != null) {

				Vec3 pos = new Vec3(this.getTurretPos());
				Vec3 vec = Vec3.createVectorHelper(0, 0, 0);
				Vec3 side = Vec3.createVectorHelper(0, 0, 0);

				//this.cachedCasingConfig = conf.casing;
				
				if(shotSide) {
					this.world.playSound(null, pos.xCoord, pos.yCoord, pos.zCoord, HBMSoundHandler.sentry_fire, SoundCategory.BLOCKS, 2.0F, 1.0F);
					this.spawnBullet(conf, 5F);
		
					vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
					vec.rotateAroundZ((float) -this.rotationPitch);
					vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

					side = Vec3.createVectorHelper(0.125 * (shotSide ? 1 : -1), 0, 0);
					side.rotateAroundY((float) -(this.rotationYaw));
		
				} else {
					this.world.playSound(null, pos.xCoord, pos.yCoord, pos.zCoord, HBMSoundHandler.sentry_fire, SoundCategory.BLOCKS, 2.0F, 0.75F);
					if(usesCasings()) {
						if(this.casingDelay() == 0) {
							spawnCasing();
						} else {
							casingDelay = this.casingDelay();
						}
					}
				}
				
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 1F);
				data.setByte("count", (byte) 1);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord), new TargetPoint(world.provider.getDimension(), pos.xCoord, pos.yCoord, pos.zCoord, 50));

				if(shotSide) {
					this.didJustShootLeft = true;
				} else {
					this.didJustShootRight = true;
				}
				shotSide = !shotSide;
			}
		}
	}
}
