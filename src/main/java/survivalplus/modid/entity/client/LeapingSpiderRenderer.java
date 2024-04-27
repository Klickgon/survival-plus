/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.custom.LeapingSpiderEntity;

@Environment(value=EnvType.CLIENT)
public class LeapingSpiderRenderer<T extends LeapingSpiderEntity>
extends MobEntityRenderer<T, LeapingSpiderModel<T>> {
    private static final Identifier TEXTURE = new Identifier(SurvivalPlus.MOD_ID,"textures/entity/leapingspider.png");

    public LeapingSpiderRenderer(EntityRendererFactory.Context context) {
        this(context, EntityModelLayers.SPIDER);
    }

    public LeapingSpiderRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
        super(ctx, new LeapingSpiderModel(ctx.getPart(layer)), 0.8f);
        this.addFeature(new LeapingSpiderEyesFeatureRenderer<>(this));
    }

    @Override
    protected float getLyingAngle(T leapingSpider) {
        return 180.0f;
    }

    @Override
    public Identifier getTexture(T leapingSpider) {
        return TEXTURE;
    }

}

