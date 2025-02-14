package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityCondenserPowered;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;


public class RenderCondenser extends TileEntitySpecialRenderer<TileEntityCondenserPowered> {

	@Override
	public void render(TileEntityCondenserPowered te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		switch(te.getBlockMetadata() - 10) {
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
		
		TileEntityCondenserPowered condenser = (TileEntityCondenserPowered) te;
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.condenser_powered_tex);
		ResourceManager.condenser_powered.renderPart("Condenser");

		float rot = condenser.lastSpin + (condenser.spin - condenser.lastSpin) /* f*/; //С "* f" я не придумал что сделать
		
		GL11.glPushMatrix();
		GL11.glTranslated(0,1.5, 0);
		GL11.glRotatef(rot, 1, 0, 0);
		GL11.glTranslated(0, -1.5, 0);
		ResourceManager.condenser_powered.renderPart("Fan1");
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glTranslated(0,1.5, 0);
		GL11.glRotatef(rot, -1, 0, 0);
		GL11.glTranslated(0, -1.5, 0);
		ResourceManager.condenser_powered.renderPart("Fan2");
		GL11.glPopMatrix();
		
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}

/*
	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(-1, -1, 0);
				GL11.glScaled(2.75, 2.75, 2.75);
			}
			public void renderCommon() {
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glTranslated(0.5, 0, 0);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.condenser_powered_tex); ResourceManager.condenser_powered.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		};
	}
 */
}
