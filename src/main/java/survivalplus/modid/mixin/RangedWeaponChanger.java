package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.util.IPPEChanger;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponChanger {

    @Inject(method = "createArrowEntity", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void flameEffectInjection(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir, @Local PersistentProjectileEntity persistentProjectileEntity){
        if (EnchantmentHelper.getLevel(ModEnchantments.FLAME_TWO, weaponStack) > 0) {
            IPPEChanger ppc = (IPPEChanger) persistentProjectileEntity;
            ppc.setFlame2(weaponStack); // sets the boolean for the Persistent Projectile, according to the given stack having the Flame 2 enchantment
            persistentProjectileEntity.setOnFireFor(100);
        }
    }
}
