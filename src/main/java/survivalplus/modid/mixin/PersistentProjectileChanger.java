package survivalplus.modid.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.util.IPPEChanger;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileChanger extends ProjectileEntity implements IPPEChanger {

    @Unique
    public boolean fromFlame2;

    public PersistentProjectileChanger(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onEntityHit", at = @At(value = "HEAD"))
    public void flameInjection(EntityHitResult entityHitResult, CallbackInfo ci){
        if(this.fromFlame2 && this.isOnFire()) { // Checks if the Bow it was shot from has the Flame II enchantment, if yes, it places a fire block at the entity hit
            this.getWorld().setBlockState(entityHitResult.getEntity().getBlockPos(), Blocks.FIRE.getDefaultState());
        }
    }

    public void setFlame2(ItemStack stack){
        if(stack.isOf(Items.BOW)) this.fromFlame2 = (EnchantmentHelper.getLevel(ModEnchantments.FLAME_TWO, stack) > 0);
    }

    @Inject(method = "applyEnchantmentEffects", at = @At(value = "TAIL"))
    public void flameInjection2(LivingEntity entity, float damageModifier, CallbackInfo ci){
        // For non-Player Entities, checks if equipped bow has Flame2. If yes, set Arrow on Fire and set field fromFlame2 to true, so it can set the impact point on fire
        if(EnchantmentHelper.getEquipmentLevel(ModEnchantments.FLAME_TWO, entity) > 0) {
            this.setOnFireFor(100);
            this.fromFlame2 = true;
        }
    }

}
