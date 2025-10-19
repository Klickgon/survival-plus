package survivalplus.modid.mixin;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorChanger {

    @Inject(method = "isNether", at = @At(value = "HEAD"), cancellable = true)
    private static void respawnAnchorDimensionRestrictionRemoval(World world, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
    }
}
