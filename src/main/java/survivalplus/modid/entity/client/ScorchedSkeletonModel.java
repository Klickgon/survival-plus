/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class ScorchedSkeletonModel<T extends SkeletonEntityRenderState>
extends SkeletonEntityModel<T> {
    public ScorchedSkeletonModel(ModelPart modelPart) {
        super(modelPart);
    }

}

