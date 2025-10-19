/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.custom.LeapingSpiderEntity;

@Environment(value=EnvType.CLIENT)
public class LeapingSpiderRenderer extends SpiderEntityRenderer<LeapingSpiderEntity> {

    public LeapingSpiderRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.addFeature(new LeapingSpiderEyesFeatureRenderer<>(this));
    }

    public LeapingSpiderRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
        super(ctx, layer);
    }

    public Identifier getTexture(LivingEntityRenderState state) {
        return Identifier.of(SurvivalPlus.MOD_ID,"textures/entity/leapingspider.png");
    }
}

