package survivalplus.modid.entity.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.entity.custom.ReeperEntity;

public class BuilderZombieRenderer extends MobEntityRenderer<BuilderZombieEntity, BuilderZombieModel<BuilderZombieEntity>> {
    public BuilderZombieRenderer(EntityRendererFactory.Context context) {
        super(context, new BuilderZombieModel<>(context.getPart(ModModelLayers.BUILDERZOMBIE)), 0.5f);
    }
    @Override
    public Identifier getTexture(BuilderZombieEntity entity) {
        return new Identifier(SurvivalPlus.MOD_ID,"textures/entity/builderzombie.png");
    }
}
