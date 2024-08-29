package survivalplus.modid.mixin;

import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponChanger {
/*
    @Inject(method = "createArrowEntity", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void flameEffectInjection(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir, @Local PersistentProjectileEntity persistentProjectileEntity){
        if (EnchantmentHelper.getLevel(ModEnchantments.FLAME_TWO, weaponStack) > 0) {
            IPPEChanger ppc = (IPPEChanger) persistentProjectileEntity;
            ppc.setFlame2(weaponStack); // sets the boolean for the Persistent Projectile, according to the given stack having the Flame 2 enchantment
            persistentProjectileEntity.setOnFireFor(100);
        }
    }*/
}
