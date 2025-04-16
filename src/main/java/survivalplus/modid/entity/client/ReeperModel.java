package survivalplus.modid.entity.client;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.entity.Entity;

public class ReeperModel<T extends Entity> extends CreeperEntityModel {

    public ReeperModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData(){
        return getTexturedModelData(Dilation.NONE);
    }
}
