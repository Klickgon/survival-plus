package survivalplus.modid.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import survivalplus.modid.enchantments.ModEnchantments;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponChanger {

    @Inject(method = "createArrowEntity", at = @At(value = "RETURN", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void flameEffectInjection(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir, ArrowItem arrowItem2, PersistentProjectileEntity persistentProjectileEntity, int i, int j, int k){
        if (EnchantmentHelper.getLevel(ModEnchantments.FLAME_TWO, weaponStack) > 0) {
            persistentProjectileEntity.setOnFireFor(100);
        }
    }
}
