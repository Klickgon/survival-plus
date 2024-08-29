package survivalplus.modid.entity.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.custom.ReeperEntity;

public class ReeperRenderer extends MobEntityRenderer<ReeperEntity, ReeperModel<ReeperEntity>> {
    public ReeperRenderer(EntityRendererFactory.Context context) {
        super(context, new ReeperModel<>(context.getPart(ModModelLayers.REEPER)), 0.5f);
    }
    @Override
    public Identifier getTexture(ReeperEntity entity) {
        return Identifier.of(SurvivalPlus.MOD_ID,"textures/entity/reeper.png");
    }

    protected void scale(ReeperEntity reeperEntity, MatrixStack matrixStack, float f) {
        float g = reeperEntity.getClientFuseTime(f);
        float h = 1.0f + MathHelper.sin(g * 100.0f) * g * 0.01f;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        matrixStack.scale(i, j, i);
    }

    protected float getAnimationCounter(ReeperEntity reeperEntity, float f) {
        float g = reeperEntity.getClientFuseTime(f);
        if ((int)(g * 10.0f) % 2 == 0) {
            return 0.0f;
        }
        return MathHelper.clamp(g, 0.5f, 1.0f);
    }

}
