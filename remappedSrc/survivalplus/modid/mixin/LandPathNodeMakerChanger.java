package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.util.IPathNodeMakerChanger;
import survivalplus.modid.util.ModTags;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerChanger extends PathNodeMaker {

    @Inject(method = "getPathNode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/LandPathNodeMaker;getNodeWith(IIILnet/minecraft/entity/ai/pathing/PathNodeType;F)Lnet/minecraft/entity/ai/pathing/PathNode;"), cancellable = true)
    public void unpassableBlockFix(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType, CallbackInfoReturnable<PathNode> cir, @Local(ordinal = 1) PathNodeType pathNodeType){
        if(((IPathNodeMakerChanger)this).getEntity().getWorld().getBlockState(new BlockPos(x,y,z)).isIn(ModTags.Blocks.NOT_PASSABLE) && pathNodeType != PathNodeType.DOOR_OPEN) cir.setReturnValue(null);
    }

}
