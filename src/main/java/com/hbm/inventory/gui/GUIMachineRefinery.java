package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineRefinery;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineRefinery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIMachineRefinery extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_refinery.png");
	private TileEntityMachineRefinery refinery;

	public GUIMachineRefinery(InventoryPlayer invPlayer, TileEntityMachineRefinery tedf) {
		super(new ContainerMachineRefinery(invPlayer, tedf));
		refinery = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		refinery.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 70 - 52, 34, 52);
		refinery.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 80, guiTop + 70 - 52, 16, 52);
		refinery.tanks[2].renderTankInfo(this, mouseX, mouseY, guiLeft + 98, guiTop + 70 - 52, 16, 52);
		refinery.tanks[3].renderTankInfo(this, mouseX, mouseY, guiLeft + 116, guiTop + 70 - 52, 16, 52);
		refinery.tanks[4].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 70 - 52, 16, 52);
		
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 70 - 52, 16, 52, refinery.power, TileEntityMachineRefinery.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.refinery.hasCustomInventoryName() ? this.refinery.getInventoryName() : I18n.format(this.refinery.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int j = (int)refinery.getPowerScaled(52);
		drawTexturedModalRect(guiLeft + 8, guiTop + 70 - j, 176, 52 - j, 16, j);

		refinery.tanks[0].renderTank(guiLeft + 26, guiTop + 70, this.zLevel, 34, 52);
		refinery.tanks[1].renderTank(guiLeft + 80, guiTop + 70, this.zLevel, 16, 52);
		refinery.tanks[2].renderTank(guiLeft + 98, guiTop + 70, this.zLevel, 16, 52);
		refinery.tanks[3].renderTank(guiLeft + 116, guiTop + 70, this.zLevel, 16, 52);
		refinery.tanks[4].renderTank(guiLeft + 134, guiTop + 70, this.zLevel, 16, 52);
		
		//refinery.tanks[0].renderTank(this, guiLeft + 26, guiTop + 70, refinery.tanks[0].getTankType().textureX() * FluidTank.x, refinery.tanks[0].getTankType().textureY() * FluidTank.y, 16, 52);
	//	refinery.tanks[0].renderTank(this, guiLeft + 26 + 16, guiTop + 70, refinery.tanks[0].getTankType().textureX() * FluidTank.x, refinery.tanks[0].getTankType().textureY() * FluidTank.y, 16, 52);
	//	refinery.tanks[0].renderTank(this, guiLeft + 26 + 32, guiTop + 70, refinery.tanks[0].getTankType().textureX() * FluidTank.x, refinery.tanks[0].getTankType().textureY() * FluidTank.y, 2, 52);

		//refinery.tanks[1].renderTank(this, guiLeft + 80, guiTop + 70, refinery.tanks[1].getTankType().textureX() * FluidTank.x, refinery.tanks[1].getTankType().textureY() * FluidTank.y, 16, 52);

	//	refinery.tanks[2].renderTank(this, guiLeft + 98, guiTop + 70, refinery.tanks[2].getTankType().textureX() * FluidTank.x, refinery.tanks[2].getTankType().textureY() * FluidTank.y, 16, 52);

	//	refinery.tanks[3].renderTank(this, guiLeft + 116, guiTop + 70, refinery.tanks[3].getTankType().textureX() * FluidTank.x, refinery.tanks[3].getTankType().textureY() * FluidTank.y, 16, 52);

	//	refinery.tanks[4].renderTank(this, guiLeft + 134, guiTop + 70, refinery.tanks[4].getTankType().textureX() * FluidTank.x, refinery.tanks[4].getTankType().textureY() * FluidTank.y, 16, 52);
	}
}
