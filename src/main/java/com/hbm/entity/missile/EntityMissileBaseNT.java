package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.projectile.EntityThrowableInterp;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissileStandard;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.TrackerUtil;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class EntityMissileBaseNT extends EntityThrowableInterp implements IChunkLoader, IRadarDetectableNT {

	public static final DataParameter<Byte> pr3 = EntityDataManager.createKey(EntityMissileBaseNT.class, DataSerializers.BYTE);
	
	public int startX;
	public int startZ;
	public int targetX;
	public int targetZ;
	public double velocity;
	public double decelY;
	public double accelXZ;
	public boolean isCluster = false;
	private Ticket loaderTicket;
	public int health = 50;

	public EntityMissileBaseNT(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		startX = (int) posX;
		startZ = (int) posZ;
		targetX = (int) posX;
		targetZ = (int) posZ;
	}

	public EntityMissileBaseNT(World world, float x, float y, float z, int a, int b) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.setLocationAndAngles(x, y, z, 0, 0);
		startX = (int) x;
		startZ = (int) z;
		targetX = a;
		targetZ = b;
		this.motionY = 2;
		
		Vec3 vector = Vec3.createVectorHelper(targetX - startX, 0, targetZ - startZ);
		accelXZ = decelY = 1 / vector.lengthVector();
		decelY *= 2;
		velocity = 0;
		
		this.rotationYaw = (float) (Math.atan2(targetX - posX, targetZ - posZ) * 180.0D / Math.PI);

		this.setSize(1.5F, 1.5F);
	}
	
	/** Auto-generates radar blip level and all that from the item */
	public abstract ItemStack getMissileItemForInfo();
	
	@Override
	public boolean canBeSeenBy(Object radar) {
		return true;
	}
	
	@Override
	public boolean paramsApplicable(RadarScanParams params) {
		if(!params.scanMissiles) return false;
		return true;
	}
	
	@Override
	public boolean suppliesRedstone(RadarScanParams params) {
		if(params.smartMode && this.motionY >= 0) return false;
		return true;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, Type.ENTITY));
		this.getDataManager().register(pr3, new Byte((byte) 5));
	}

	@Override
	protected double motionMult() {
		return velocity;
	}

	@Override
	public boolean doesImpactEntities() {
		return false;
	}
	
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		super.onUpdate();
		
		if(velocity < 4) velocity += MathHelper.clamp(this.ticksExisted / 60D * 0.05D, 0, 0.05);
		
		if(!world.isRemote) {

			if(hasPropulsion()) {
				this.motionY -= decelY * velocity;
	
				Vec3 vector = Vec3.createVectorHelper(targetX - startX, 0, targetZ - startZ);
				vector = vector.normalize();
				vector.xCoord *= accelXZ;
				vector.zCoord *= accelXZ;
	
				if(motionY > 0) {
					motionX += vector.xCoord * velocity;
					motionZ += vector.zCoord * velocity;
				}
	
				if(motionY < 0) {
					motionX -= vector.xCoord * velocity;
					motionZ -= vector.zCoord * velocity;
				}
			} else {
				motionX *= 0.99;
				motionZ *= 0.99;

				if(motionY > -1.5)
					motionY -= 0.05;
			}
	
			if(motionY < -velocity && this.isCluster) {
				cluster();
				this.setDead();
				return;
			}
			
			this.rotationYaw = (float) (Math.atan2(targetX - posX, targetZ - posZ) * 180.0D / Math.PI);
			float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			for(this.rotationPitch = (float) (Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);
			EntityTrackerEntry tracker = TrackerUtil.getTrackerEntry((WorldServer) world, this.getEntityId());
			if(tracker != null){
				// fuck you, mojang
				int lastYaw = ObfuscationReflectionHelper.getPrivateValue(EntityTrackerEntry.class, tracker, "field_73127_g");
				lastYaw += 100;
				ObfuscationReflectionHelper.setPrivateValue(EntityTrackerEntry.class, tracker, lastYaw, "field_73127_g");
			}
			
			loadNeighboringChunks((int) Math.floor(posX / 16), (int) Math.floor(posZ / 16));
		} else {
			this.spawnContrail();
		}
		
		while(this.rotationPitch - this.prevRotationPitch >= 180.0F) this.prevRotationPitch += 360.0F;
		while(this.rotationYaw - this.prevRotationYaw < -180.0F) this.prevRotationYaw -= 360.0F;
		while(this.rotationYaw - this.prevRotationYaw >= 180.0F) this.prevRotationYaw += 360.0F;
	}
	
	public boolean hasPropulsion() {
		return true;
	}
	
	protected void spawnContrail() {
		this.spawnContraolWithOffset(0, 0, 0);
	}
	
	protected void spawnContraolWithOffset(double offsetX, double offsetY, double offsetZ) {
		Vec3 vec = Vec3.createVectorHelper(this.lastTickPosX - this.posX, this.lastTickPosY - this.posY, this.lastTickPosZ - this.posZ);
		double len = vec.lengthVector();
		vec = vec.normalize();
		Vec3 thrust = Vec3.createVectorHelper(0, 1, 0);
		thrust.rotateAroundZ(this.rotationPitch * (float) Math.PI / 180F);
		thrust.rotateAroundY((this.rotationYaw + 90) * (float) Math.PI / 180F);
		
		for(int i = 0; i < Math.max(Math.min(len, 10), 1); i++) {
			double j = i - len;
			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("posX", posX - vec.xCoord * j + offsetX);
			data.setDouble("posY", posY - vec.yCoord * j + offsetY);
			data.setDouble("posZ", posZ - vec.zCoord * j + offsetZ);
			data.setString("type", "missileContrail");
			data.setFloat("scale", this.getContrailScale());
			data.setDouble("moX", -thrust.xCoord);
			data.setDouble("moY", -thrust.yCoord);
			data.setDouble("moZ", -thrust.zCoord);
			data.setInteger("maxAge", 60 + rand.nextInt(20));
			MainRegistry.proxy.effectNT(data);
		}
	}
	
	protected float getContrailScale() {
		return 1F;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		motionX = nbt.getDouble("moX");
		motionY = nbt.getDouble("moY");
		motionZ = nbt.getDouble("moZ");
		posX = nbt.getDouble("poX");
		posY = nbt.getDouble("poY");
		posZ = nbt.getDouble("poZ");
		decelY = nbt.getDouble("decel");
		accelXZ = nbt.getDouble("accel");
		targetX = nbt.getInteger("tX");
		targetZ = nbt.getInteger("tZ");
		startX = nbt.getInteger("sX");
		startZ = nbt.getInteger("sZ");
		velocity = nbt.getDouble("veloc");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setDouble("moX", motionX);
		nbt.setDouble("moY", motionY);
		nbt.setDouble("moZ", motionZ);
		nbt.setDouble("poX", posX);
		nbt.setDouble("poY", posY);
		nbt.setDouble("poZ", posZ);
		nbt.setDouble("decel", decelY);
		nbt.setDouble("accel", accelXZ);
		nbt.setInteger("tX", targetX);
		nbt.setInteger("tZ", targetZ);
		nbt.setInteger("sX", startX);
		nbt.setInteger("sZ", startZ);
		nbt.setDouble("veloc", velocity);
	}
	
	public boolean canBeCollidedWith() {
		return true;
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(this.isEntityInvulnerable(source)) {
			return false;
		} else {
			if(this.health > 0 && !this.world.isRemote) {
				health -= amount;

				if(this.health <= 0) {
					this.killMissile();
				}
			}

			return true;
		}
	}

	protected void killMissile() {
		if(!this.isDead) {
			this.setDead();
			ExplosionLarge.explode(world, posX, posY, posZ, 5, true, false, true);
			ExplosionLarge.spawnShrapnelShower(world, posX, posY, posZ, motionX, motionY, motionZ, 15, 0.075);
			ExplosionLarge.spawnMissileDebris(world, posX, posY, posZ, motionX, motionY, motionZ, 0.25, getDebris(), getDebrisRareDrop());
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	@Override
	protected void onImpact(RayTraceResult p_70184_1_) {
		if(p_70184_1_ != null && p_70184_1_.typeOfHit == RayTraceResult.Type.BLOCK) {
			this.onImpact();
			this.setDead();
		}
	}

	public abstract void onImpact();
	public abstract List<ItemStack> getDebris();
	public abstract ItemStack getDebrisRareDrop();
	public void cluster() { }

	@Override
	public double getGravityVelocity() {
		return 0.0D;
	}

	@Override
	protected float getAirDrag() {
		return 1F;
	}

	@Override
	protected float getWaterDrag() {
		return 1F;
	}
	
	@Override
	public void init(Ticket ticket) {
		if(!world.isRemote) {

			if(ticket != null) {

				if(loaderTicket == null) {

					loaderTicket = ticket;
					loaderTicket.bindEntity(this);
					loaderTicket.getModData();
				}

				ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
			}
		}
	}

	List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();

	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
		if(!world.isRemote && loaderTicket != null) {
			
			clearChunkLoader();

			loadedChunks.clear();
			loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));
			//loadedChunks.add(new ChunkCoordIntPair(newChunkX + (int) Math.floor((this.posX + this.motionX * this.motionMult()) / 16D), newChunkZ + (int) Math.floor((this.posZ + this.motionZ * this.motionMult()) / 16D)));

			for(ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.forceChunk(loaderTicket, chunk);
			}
		}
	}
	
	@Override
	public void setDead() {
		super.setDead();
		this.clearChunkLoader();
	}
	
	public void clearChunkLoader() {
		if(!world.isRemote && loaderTicket != null) {
			for(ChunkPos chunk : loadedChunks) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}
		}
	}
	
	@Override
	public String getUnlocalizedName() {
		ItemStack item = this.getMissileItemForInfo();
		if(item != null && item.getItem() instanceof ItemMissileStandard) {
			ItemMissileStandard missile = (ItemMissileStandard) item.getItem();
			switch(missile.tier) {
			case TIER0: return "radar.target.tier0";
			case TIER1: return "radar.target.tier1";
			case TIER2: return "radar.target.tier2";
			case TIER3: return "radar.target.tier3";
			case TIER4: return "radar.target.tier4";
			default: return "Unknown";
			}
		}
		
		return "Unknown";
	}
	
	@Override
	public int getBlipLevel() {
		ItemStack item = this.getMissileItemForInfo();
		if(item != null && item.getItem() instanceof ItemMissileStandard) {
			ItemMissileStandard missile = (ItemMissileStandard) item.getItem();
			switch(missile.tier) {
			case TIER0: return IRadarDetectableNT.TIER0;
			case TIER1: return IRadarDetectableNT.TIER1;
			case TIER2: return IRadarDetectableNT.TIER2;
			case TIER3: return IRadarDetectableNT.TIER3;
			case TIER4: return IRadarDetectableNT.TIER4;
			default: return IRadarDetectableNT.SPECIAL;
			}
		}
		
		return IRadarDetectableNT.SPECIAL;
	}
}
