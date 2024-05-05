package survivalplus.modid.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.util.IWorldChanger;

@Mixin(World.class)
public abstract class WorldChanger implements IWorldChanger {

    @Unique
    public boolean enoughTimesSinceRest = false;


    @Inject(method = "isDay", at = @At(value = "HEAD"), cancellable = true)
    public void sleepWhenever(CallbackInfoReturnable<Boolean> cir){
                cir.setReturnValue(!enoughTimesSinceRest);
    }

    @Override
    public void setEnoughTimeSinceRest(boolean b) {
        this.enoughTimesSinceRest = b;
    }
}
