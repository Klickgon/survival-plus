/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class ScorchedSkeletonModel<T extends SkeletonEntityRenderState>
extends SkeletonEntityModel<T> {
    public ScorchedSkeletonModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-2.0f, 12.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(2.0f, 12.0f, 0.0f));
        return TexturedModelData.of(modelData, 64, 32);
    }

}

