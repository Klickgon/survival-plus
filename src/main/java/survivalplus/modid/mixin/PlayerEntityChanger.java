package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalplus.modid.enchantments.ModEnchantments;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityChanger extends LivingEntity {

    protected PlayerEntityChanger(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getAttackCooldownProgress", at = @At(value = "RETURN"))
    private float rapidSwingRedirect(float original){
        if(EnchantmentHelper.getLevel(ModEnchantments.RAPID_SWING, this.getMainHandStack()) > 0) return 1.0f;
        return original;
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isOnGround()Z", ordinal = 1))
    private boolean disableSweepAttackOnEnchant(boolean original){
        return original && !(EnchantmentHelper.getLevel(ModEnchantments.RAPID_SWING, this.getMainHandStack()) > 0);
    }
}