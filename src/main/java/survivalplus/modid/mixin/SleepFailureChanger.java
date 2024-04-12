package survivalplus.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.minecraft.entity.player.PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW;

@Mixin(PlayerEntity.SleepFailureReason.class)
public abstract class SleepFailureChanger {


    @Final
    @Shadow
    public static PlayerEntity.SleepFailureReason NOT_POSSIBLE_NOW;

    @Inject(method = "getMessage", at = @At(value = "HEAD"), cancellable = true)
    protected void messageRedirect(CallbackInfoReturnable<Text> cir){
        if(this.equals(NOT_POSSIBLE_NOW))cir.setReturnValue(Text.translatable("block.survivalplus.bed.sleepcooldown"));
        // Changes the text translatable tag of the NOT_POSSIBLE_NOW SleepFailureReason from "you can only sleep during the day"
        // to "you already slept recently" (you won't believe how hard it was to figure out)
    }

}
