/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

@Environment(value=EnvType.CLIENT)
public class ScorchedSkeletonRenderer
extends SkeletonEntityRenderer {

    public ScorchedSkeletonRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(SkeletonEntityRenderState renderState) {
        return Identifier.of(SurvivalPlus.MOD_ID,"textures/entity/scorchedskeleton.png");
    }


}

