package survivalplus.modid.mixin;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.util.IWorldChanger;

@Mixin(World.class)
public class WorldChanger implements IWorldChanger {

    @Unique
    public int sleepcooldown = 0;

    @Inject(method = "isDay", at = @At(value = "HEAD"), cancellable = true)
    public void sleepWhenever(CallbackInfoReturnable<Boolean> cir){
        if(this.sleepcooldown <= 0) {
            cir.setReturnValue(false);
        }
        else {
            cir.setReturnValue(true);
        }
    }

    public int getSleepCooldown(){
        return sleepcooldown;
    }

    public void setSleepCooldown(int i){
        this.sleepcooldown = i;
    }


}
