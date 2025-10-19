package survivalplus.modid.entity.client;

import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ReeperRenderer extends CreeperEntityRenderer {

    public ReeperRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public Identifier getTexture(CreeperEntityRenderState creeperEntityRenderState) {
        return Identifier.of(SurvivalPlus.MOD_ID, "textures/entity/reeper.png");
    }
}