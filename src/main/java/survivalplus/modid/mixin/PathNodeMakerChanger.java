package survivalplus.modid.mixin;

import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import survivalplus.modid.util.IPathNodeMakerChanger;

@Mixin(PathNodeMaker.class)
public class PathNodeMakerChanger implements IPathNodeMakerChanger {

    @Shadow protected MobEntity entity;

    @Unique
    public MobEntity getEntity(){
        return this.entity;
    }

}
