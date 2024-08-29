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
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

@Environment(value=EnvType.CLIENT)
public class BrunsFeatureRenderer<T extends Entity, M extends EntityModel<T>>
extends EyesFeatureRenderer<T, M> {
    private static final RenderLayer BURNS = RenderLayer.getEyes(Identifier.of(SurvivalPlus.MOD_ID,"textures/entity/scorchedskeletonburns.png"));

    public BrunsFeatureRenderer(FeatureRendererContext featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return BURNS;
    }
}

