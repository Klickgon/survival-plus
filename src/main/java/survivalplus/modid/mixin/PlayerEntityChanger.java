package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.util.IServerWorldChanger;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityChanger extends LivingEntity {

    protected PlayerEntityChanger(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getAttackCooldownProgress", at = @At(value = "RETURN"))
    private float rapidSwingRedirect(float original){
        RegistryWrapper.Impl<Enchantment> impl = this.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        if(EnchantmentHelper.getLevel(impl.getOrThrow(ModEnchantments.RAPID_SWING), this.getMainHandStack()) > 0) return 1.0f;
        return original;
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isOnGround()Z", ordinal = 1))
    private boolean disableSweepAttackOnEnchant(boolean original){
        RegistryWrapper.Impl<Enchantment> impl = this.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return original && !(EnchantmentHelper.getLevel(impl.getOrThrow(ModEnchantments.RAPID_SWING), this.getMainHandStack()) > 0);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
    private boolean sleepDuringDayFix(boolean original){
        return ((IServerWorldChanger)this.getWorld()).notEnoughTimeSinceRest();
    }
}
