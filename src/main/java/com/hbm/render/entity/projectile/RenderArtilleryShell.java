package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

public class RenderArtilleryShell extends Render<EntityArtilleryShell> {

    public static final IRenderFactory<EntityArtilleryShell> FACTORY = man -> new RenderArtilleryShell(man);

    protected RenderArtilleryShell(RenderManager renderManager){
        super(renderManager);
    }
    @Override
    public void doRender(EntityArtilleryShell shell, double x, double y, double z, float f0, float f1) {

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(shell.prevRotationYaw + (shell.rotationYaw - shell.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(shell.prevRotationPitch + (shell.rotationPitch - shell.prevRotationPitch) * f1 - 90, 0.0F, 0.0F, 1.0F);

        float scale = 5F;
        GL11.glScalef(scale * 0.5F, scale, scale * 0.5F);

        this.bindEntityTexture(shell);

        boolean fog = GL11.glIsEnabled(GL11.GL_FOG);

        if(fog) GL11.glDisable(GL11.GL_FOG);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        ResourceManager.projectiles.renderPart("Grenade");
        GL11.glShadeModel(GL11.GL_FLAT);
        if(fog) GL11.glEnable(GL11.GL_FOG);

        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityArtilleryShell entity) {
        return ResourceManager.grenade_tex;
    }
}
