package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.Random;

import api.hbm.tile.ILoadedTile;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.inventory.fluid.tank.FluidTankNTM;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidTank;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityCondenserPowered extends TileEntityCondenser implements IEnergyReceiverMK2, IConfigurableMachine {
	
	public long power;
	public float spin;
	public float lastSpin;
	public FluidTankNTM[] tanksNew;
	public FluidTank[] tanks;

	@Override
	public boolean isLoaded() {
		return false;
	}

	//Configurable values
	public static long maxPower = 10_000_000;
	public static int inputTankSizeP = 1_000_000;
	public static int outputTankSizeP = 1_000_000;
	public static int powerConsumption = 10;

	public TileEntityCondenserPowered() {
		tanksNew = new FluidTankNTM[2];
		tanksNew[0] = new FluidTankNTM(Fluids.SPENTSTEAM, inputTankSizeP);
		tanksNew[1] = new FluidTankNTM(Fluids.WATER, outputTankSizeP);
	}
	
	@Override
	public String getConfigName() {
		return "condenserPowered";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "L:maxPower", maxPower);
		inputTankSizeP = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeP);
		outputTankSizeP = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeP);
		powerConsumption = IConfigurableMachine.grab(obj, "I:powerConsumption", powerConsumption);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:maxPower").value(maxPower);
		writer.name("I:inputTankSize").value(inputTankSizeP);
		writer.name("I:outputTankSize").value(outputTankSizeP);
		writer.name("I:powerConsumption").value(powerConsumption);
	}

	@Override
	public void update() {
		super.update();
		
		if(world.isRemote) {

			this.lastSpin = this.spin;
			
			if(this.waterTimer > 0) {
				this.spin += 30F;
				
				if(this.spin >= 360F) {
					this.spin -= 360F;
					this.lastSpin -= 360F;
				}
				
				if(world.getTotalWorldTime() % 4 == 0) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
					Random rand = world.rand;

					world.spawnParticle(EnumParticleTypes.CLOUD,
							pos.getX() + 0.5 + dir.offsetX * (rand.nextDouble() + 1.5) + rand.nextGaussian() * 0.2,
							pos.getY() + 1.5,
							pos.getZ() + 0.5 + dir.offsetZ * 1.5,
							-dir.offsetX * 0.1, 0, -dir.offsetZ * 0.1);

					world.spawnParticle(EnumParticleTypes.CLOUD,
							pos.getX() + 0.5 - dir.offsetX * (rand.nextDouble() + 1.5) + rand.nextGaussian() * 0.2,
							pos.getY() + 1.5,
							pos.getZ() + 0.5 - dir.offsetZ * 1.5,
							dir.offsetX * 0.1, 0, dir.offsetZ * 0.1);
				}
			}
		}
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		this.power = nbt.getLong("power");
		this.tanksNew[0].readFromNBT(nbt, "0");
		this.tanksNew[1].readFromNBT(nbt, "1");
		this.waterTimer = nbt.getByte("timer");
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		tanksNew[0].readFromNBT(nbt, "water");
		tanksNew[1].readFromNBT(nbt, "steam");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		tanksNew[0].writeToNBT(nbt, "water");
		tanksNew[1].writeToNBT(nbt, "steam");
		return nbt;
	}
/*
	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(this.tanks[0].getTankType(), world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	@Override
	public void sendFluidToAll(FluidTank tank, TileEntity te) {
		for(DirPos pos : getConPos()) {
			this.sendFluid(this.tanks[1], world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}
*/
	public DirPos[] getConPos() {
		
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		
		return new DirPos[] {
				new DirPos(pos.add(-dir.offsetX * 4, 1, -dir.offsetZ * 4), dir.getOpposite()),
				new DirPos(pos.add( -dir.offsetX - rot.offsetX * 4, 1,-dir.offsetZ - rot.offsetZ * 4), rot.getOpposite()),
				new DirPos(pos.add(dir.offsetX * 2 + rot.offsetX, 0, dir.offsetZ * 2 + rot.offsetZ), dir),
				new DirPos(pos.add(dir.offsetX * 2 + rot.offsetX, 0, dir.offsetZ * 2 + rot.offsetZ), dir),
				new DirPos(pos.add(-dir.offsetX * 2 + rot.offsetX, 1, -dir.offsetZ * 2 + rot.offsetZ), dir.getOpposite()),
				new DirPos(pos.add(-dir.offsetX * 2 + rot.offsetX, 1, -dir.offsetZ * 2 + rot.offsetZ), dir.getOpposite())
		};
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 3,
					pos.getY(),
					pos.getZ() - 3,
					pos.getX() + 4,
					pos.getY() + 3,
					pos.getZ() + 4
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return this.maxPower;
	}
}
