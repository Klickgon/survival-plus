package survivalplus.modid.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ReeperModel<T extends Entity> extends CreeperEntityModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;

    public ReeperModel(ModelPart root) {
        super(root);
        this.root = root;
        this.head = root.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, Dilation.NONE), ModelTransform.pivot(0.0F, 6.0F, 0.0F));
        modelPartData.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, Dilation.NONE), ModelTransform.pivot(0.0F, 6.0F, 0.0F));
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, Dilation.NONE);
        modelPartData.addChild("right_hind_leg", modelPartBuilder, ModelTransform.pivot(-2.0F, 18.0F, 4.0F));
        modelPartData.addChild("left_hind_leg", modelPartBuilder, ModelTransform.pivot(2.0F, 18.0F, 4.0F));
        modelPartData.addChild("right_front_leg", modelPartBuilder, ModelTransform.pivot(-2.0F, 18.0F, -4.0F));
        modelPartData.addChild("left_front_leg", modelPartBuilder, ModelTransform.pivot(2.0F, 18.0F, -4.0F));
        return TexturedModelData.of(modelData, 64, 32);
    }

    public ModelPart getPart() {
        return this.root;
    }

    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
        this.leftHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        this.rightHindLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
        this.leftFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
        this.rightFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
    }
}
