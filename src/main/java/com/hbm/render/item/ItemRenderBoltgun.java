package com.hbm.render.item;

import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;

public class ItemRenderBoltgun extends TEISRBase {
	
	@Override
	public void renderByItem(ItemStack itemStackIn) {
		
		GL11.glPushMatrix();
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.boltgun_tex);
		
		switch(type) {

			case FIRST_PERSON_LEFT_HAND:
			case FIRST_PERSON_RIGHT_HAND:

				if(type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
					GL11.glTranslated(0.3, 0.9, -0.3);
					GL11.glRotated(205, 0, 0, 1);
					GL11.glTranslated(-0.2, 1.1, 0.8);
					GL11.glRotated(-25, 0, 0, 1);
					GL11.glRotated(180, 1, 0, 0);
				} else {
					GL11.glTranslated(0, 0, 0.9);
					GL11.glRotated(0, 0, 0, 1);
				}
				double s0 = 0.25D;
				GL11.glScaled(s0, s0, s0);
				GL11.glRotatef(80F, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-20F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(1.0F, 0.5F, 3.0F);
				if(type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
					GL11.glRotated(205, 0, 0, 1);
					GL11.glTranslated(0.2, 1.1, 0.8);
					GL11.glRotated(-25, 0, 0, 1);
				}
			
				GL11.glPushMatrix();
				double[] anim = HbmAnimations.getRelevantTransformation("RECOIL", type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
				GL11.glTranslated(0, 0, -anim[0]);
				if(anim[0] != 0) player.isSwingInProgress = false;
				ResourceManager.boltgun.renderPart("Barrel");
				GL11.glPopMatrix();
			
				break;

			case THIRD_PERSON_LEFT_HAND:
			case THIRD_PERSON_RIGHT_HAND:
			case GROUND:
			case FIXED:
			case HEAD:

				GL11.glScalef(0.375F, 0.375F, 0.375F);
				GL11.glTranslated(1.75, -0.5, 0.4);
				GL11.glRotated(180, 1, 0, 1);

			case GUI:

				GL11.glTranslated(0.26, 0.23, 0);
				GL11.glRotated(90, 0, 1, 0);
				GL11.glRotated(45, 1, 0, 0);
				GL11.glScaled(0.2, 0.2, 0.2);
			
		default: break;
		}

		ResourceManager.boltgun.renderPart("Gun");
		if(type != type.FIRST_PERSON_RIGHT_HAND && type != type.FIRST_PERSON_LEFT_HAND) {
			ResourceManager.boltgun.renderPart("Barrel");
		}
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glPopMatrix();
	}
}
