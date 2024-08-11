package survivalplus.modid.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import survivalplus.modid.util.IPPEChanger;

@Mixin(BowItem.class)
public class BowItemChanger {


    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void originBowInjection(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, ItemStack itemStack, int i, float f){
        IPPEChanger ppc = (IPPEChanger) this;
        ppc.setFlame2(stack); // sets the boolean for the Persistent Projectile, according to the given stack having the Flame 2 enchantment
    }

}
