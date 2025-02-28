package survivalplus.modid.entity.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class MinerZombieRenderer extends ZombieEntityRenderer {

    public MinerZombieRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ZombieEntityRenderState zombieEntityRenderState) {
        return Identifier.of(SurvivalPlus.MOD_ID,"textures/entity/minerzombie.png");
    }

    public MinerZombieRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legsArmorLayer, EntityModelLayer bodyArmorLayer, EntityModelLayer entityModelLayer, EntityModelLayer entityModelLayer2, EntityModelLayer entityModelLayer3) {
        super(ctx, layer, legsArmorLayer, bodyArmorLayer, entityModelLayer, entityModelLayer2, entityModelLayer3);
    }
}