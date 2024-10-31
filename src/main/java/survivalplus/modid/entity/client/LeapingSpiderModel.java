/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class LeapingSpiderModel<T extends Entity>
extends SpiderEntityModel {

    private static final String BODY0 = "body0";
    private static final String BODY1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleLeg;
    private final ModelPart leftMiddleLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public LeapingSpiderModel(ModelPart root) {
        super(root);
        this.root = root;
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
        this.rightMiddleLeg = root.getChild(RIGHT_MIDDLE_HIND_LEG);
        this.leftMiddleLeg = root.getChild(LEFT_MIDDLE_HIND_LEG);
        this.rightMiddleFrontLeg = root.getChild(RIGHT_MIDDLE_FRONT_LEG);
        this.leftMiddleFrontLeg = root.getChild(LEFT_MIDDLE_FRONT_LEG);
        this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
        this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(32, 4).cuboid(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 15.0f, -3.0f));
        modelPartData.addChild(BODY0, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), ModelTransform.pivot(0.0f, 15.0f, 0.0f));
        modelPartData.addChild(BODY1, ModelPartBuilder.create().uv(0, 12).cuboid(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f), ModelTransform.pivot(0.0f, 15.0f, 9.0f));
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(18, 0).cuboid(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        ModelPartBuilder modelPartBuilder2 = ModelPartBuilder.create().uv(18, 0).mirrored().cuboid(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        modelPartData.addChild(EntityModelPartNames.RIGHT_HIND_LEG, modelPartBuilder, ModelTransform.pivot(-4.0f, 15.0f, 2.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_HIND_LEG, modelPartBuilder2, ModelTransform.pivot(4.0f, 15.0f, 2.0f));
        modelPartData.addChild(RIGHT_MIDDLE_HIND_LEG, modelPartBuilder, ModelTransform.pivot(-4.0f, 15.0f, 1.0f));
        modelPartData.addChild(LEFT_MIDDLE_HIND_LEG, modelPartBuilder2, ModelTransform.pivot(4.0f, 15.0f, 1.0f));
        modelPartData.addChild(RIGHT_MIDDLE_FRONT_LEG, modelPartBuilder, ModelTransform.pivot(-4.0f, 15.0f, 0.0f));
        modelPartData.addChild(LEFT_MIDDLE_FRONT_LEG, modelPartBuilder2, ModelTransform.pivot(4.0f, 15.0f, 0.0f));
        modelPartData.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, modelPartBuilder, ModelTransform.pivot(-4.0f, 15.0f, -1.0f));
        modelPartData.addChild(EntityModelPartNames.LEFT_FRONT_LEG, modelPartBuilder2, ModelTransform.pivot(4.0f, 15.0f, -1.0f));
        return TexturedModelData.of(modelData, 64, 32);
    }
}

