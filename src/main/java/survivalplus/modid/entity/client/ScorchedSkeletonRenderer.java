/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

@Environment(value=EnvType.CLIENT)
public class ScorchedSkeletonRenderer
extends BipedEntityRenderer<AbstractSkeletonEntity, ScorchedSkeletonModel<AbstractSkeletonEntity>> {
    private static final Identifier TEXTURE = new Identifier(SurvivalPlus.MOD_ID,"textures/entity/scorchedskeleton.png");

    public ScorchedSkeletonRenderer(EntityRendererFactory.Context context) {
        this(context, EntityModelLayers.SKELETON, EntityModelLayers.SKELETON_INNER_ARMOR, EntityModelLayers.SKELETON_OUTER_ARMOR);
    }

    public ScorchedSkeletonRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legArmorLayer, EntityModelLayer bodyArmorLayer) {
        super(ctx, new ScorchedSkeletonModel(ctx.getPart(layer)), 0.5f);
        this.addFeature(new ArmorFeatureRenderer(this, new ScorchedSkeletonModel(ctx.getPart(legArmorLayer)), new ScorchedSkeletonModel(ctx.getPart(bodyArmorLayer)), ctx.getModelManager()));
    }

    @Override
    public Identifier getTexture(AbstractSkeletonEntity abstractSkeletonEntity) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(AbstractSkeletonEntity abstractSkeletonEntity) {
        return abstractSkeletonEntity.isShaking();
    }

}

