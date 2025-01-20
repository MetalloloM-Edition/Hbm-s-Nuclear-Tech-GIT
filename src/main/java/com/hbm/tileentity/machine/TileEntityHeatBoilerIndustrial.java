package com.hbm.tileentity.machine;

import java.io.IOException;

import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.tile.IHeatSource;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.INBTPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityHeatBoilerIndustrial extends TileEntityLoadedBase implements INBTPacketReceiver, ITickable, IConfigurableMachine, IFluidStandardTransceiver, IFFtoNTMF {

	public FluidTank[] tanks;
	public Fluid[] types = new Fluid[2];
	public FluidTankNTM[] tanksNew;
	public int heat;
	public static int maxHeat = 12_800_000; //the heat required to turn 64k of water into steam
	public static double diffusion = 0.1D;
	//private AudioWrapper audio;
    //public int audioTime;
	private static boolean converted = false;

	public TileEntityHeatBoilerIndustrial() {
		super();
		tanksNew = new FluidTankNTM[2];
		this.tanksNew[0] = new FluidTankNTM(Fluids.WATER, 16_000);
		this.tanksNew[1] = new FluidTankNTM(Fluids.STEAM, 16_000 * 100);

		tanks = new FluidTank[2];

		tanks[0] = new FluidTank(FluidRegistry.WATER, 0, 64000);
		types[0] = FluidRegistry.WATER;

		tanks[1] = new FluidTank(ModForgeFluids.steam, 0, 6400000);
		types[1] = ModForgeFluids.steam;

	}
	
	@Override
	public void update() {

		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluids(types, tanks, tanksNew);
				converted = true;
			}
			setupTanks();
			updateConnections();
			tryPullHeat();
			tryConvert();

			for(DirPos pos : getConPos()) {
				if(tanksNew[1].getFill() > 0) this.sendFluid(tanksNew[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			}

			NBTTagCompound data = new NBTTagCompound();

			for(int i = 0; i < 2; i++)
				tanksNew[i].writeToNBT(data, "tank" + i);

			INBTPacketReceiver.networkPack(this, data, 50);
		} /*else {
			
			if(this.isOn) audioTime = 20;
			
			if(audioTime > 0) {
				
				audioTime--;
				
				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.keepAlive();
				
			} else {
				
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}*/
	}
/*
	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound("hbm:block.boiler", xCoord, yCoord, zCoord, 0.125F, 10F, 1.0F, 20);
	}

	@Override
	public void onChunkUnload() {

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}
*/
	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		for(int i = 0; i < 2; i++)
			tanksNew[i].readFromNBT(nbt, "tank" + i);
		this.heat = nbt.getInteger("heat");
	}

	protected void setupTanks() {

		if(tanksNew[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanksNew[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {
				FT_Heatable.HeatingStep entry = trait.getFirstStep();
				tanksNew[1].setTankType(entry.typeProduced);
				tanksNew[1].changeTankSize(tanksNew[0].getMaxFill() * entry.amountProduced / entry.amountReq);
				return;
			}
		}

		tanksNew[0].setTankType(Fluids.NONE);
		tanksNew[1].setTankType(Fluids.NONE);
	}

	protected void tryPullHeat() {

		if(this.heat >= TileEntityHeatBoiler.maxHeat) return;
		BlockPos blockBelow = pos.down();
		TileEntity con = world.getTileEntity(blockBelow);

		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	AxisAlignedBB bb = null;
/*
	protected void setupTanks() {
		
		if(tanks[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(HeatingType.BOILER) > 0) {
				HeatingStep entry = trait.getFirstStep();
				tanks[1].setTankType(entry.typeProduced);
				tanks[1].changeTankSize(tanks[0].getMaxFill() * entry.amountProduced / entry.amountReq);
				return;
			}
		}

		tanks[0].setTankType(Fluids.NONE);
		tanks[1].setTankType(Fluids.NONE);
	}
*/
	protected void tryConvert() {

		if(tanksNew[0].getTankType().hasTrait(FT_Heatable.class)) {
			FT_Heatable trait = tanksNew[0].getTankType().getTrait(FT_Heatable.class);
			if(trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {

				FT_Heatable.HeatingStep entry = trait.getFirstStep();
				int inputOps = this.tanksNew[0].getFill() / entry.amountReq;
				int outputOps = (this.tanksNew[1].getMaxFill() - this.tanksNew[1].getFill()) / entry.amountProduced;
				int heatOps = this.heat / entry.heatReq;

				int ops = Math.min(inputOps, Math.min(outputOps, heatOps));

				this.tanksNew[0].setFill(this.tanksNew[0].getFill() - entry.amountReq * ops);
				this.tanksNew[1].setFill(this.tanksNew[1].getFill() + entry.amountProduced * ops);
				this.heat -= entry.heatReq * ops;
			}
		}
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}
/*
	private void sendFluid() {
		
		for(DirPos pos : getConPos()) {
			this.sendFluid(tanks[1], world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir().getOpposite());
		}
	}
*/
	protected DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(pos.getX() + dir.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2, dir),
				new DirPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
				new DirPos(pos.getX(), pos.getY() + 5, pos.getZ(), Library.POS_Y)
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(!converted){
			FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
			for(int i=0; i<tanks.length; i++){
				if(tanks[i].getFluid() != null){
					types[i] = tanks[i].getFluid().getFluid();
				} else {
					types[i] = null;
				}
			}
		} else {
			for (int i = 0; i < tanksNew.length; i++)
				tanksNew[i].readFromNBT(nbt, "tank" + i);
			if(nbt.hasKey("tanks")) nbt.removeTag("tanks");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(!converted){
			for(int i=0; i<tanks.length; i++){
				if(types[i] != null){
					tanks[i].setFluid(new FluidStack(types[i], tanks[i].getFluidAmount()));
				} else {
					tanks[i].setFluid(null);
				}
			}
			nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
		} else {
			for (int i = 0; i < tanksNew.length; i++)
				tanksNew[i].writeToNBT(nbt, "tank" + i);
		}
		return nbt;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {};
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanksNew[1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanksNew[0]};
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 1,
					pos.getY(),
					pos.getZ() - 1,
					pos.getX() + 2,
					pos.getY() + 5,
					pos.getZ() + 2
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
	public String getConfigName() {
		return "boilerIndustrial";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		maxHeat = IConfigurableMachine.grab(obj, "I:maxHeat", maxHeat);
		diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:maxHeat").value(maxHeat);
		writer.name("D:diffusion").value(diffusion);
		writer.endObject();
	}
}
