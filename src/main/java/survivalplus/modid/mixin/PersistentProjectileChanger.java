package survivalplus.modid.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
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
    public ItemStack OriginBow;

    public PersistentProjectileChanger(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onEntityHit", at = @At(value = "HEAD"))
    public void flameInjection(EntityHitResult entityHitResult, CallbackInfo ci){
        if(this.OriginBow != null){ // keep the null check, otherwise the game has the chance to crash on hit
            if(EnchantmentHelper.getLevel(ModEnchantments.FLAME_TWO, this.OriginBow) > 0) { // Checks if the Bow it was shot from has the Flame II enchantment, if yes, it places a fire block at the entity hit
               this.getOwner().getWorld().setBlockState(entityHitResult.getEntity().getBlockPos(), Blocks.FIRE.getDefaultState());
            }
        }
    }

    public void setOriginBow(ItemStack stack){
        if(stack.isOf(Items.BOW)) this.OriginBow = stack;
    }
}
