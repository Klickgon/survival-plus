/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

@Environment(value=EnvType.CLIENT)
public class LeapingSpiderEyesFeatureRenderer<T extends EntityRenderState, M extends EntityModel<T>>
extends EyesFeatureRenderer<T, M> {

    public LeapingSpiderEyesFeatureRenderer(FeatureRendererContext<T, M> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return RenderLayer.getEyes(Identifier.of(SurvivalPlus.MOD_ID,"textures/entity/leapingspider_eyes.png"));
    }
}

