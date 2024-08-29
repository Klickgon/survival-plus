package survivalplus.modid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import survivalplus.modid.util.IPPEChanger;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileChanger extends ProjectileEntity implements IPPEChanger {

    @Unique
    public boolean fromFlame2;

    public PersistentProjectileChanger(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }
        /*
    @Inject(method = "onEntityHit", at = @At(value = "HEAD"))
    public void flameInjection(EntityHitResult entityHitResult, CallbackInfo ci){
        if(this.fromFlame2 && this.isOnFire()) { // Checks if the Bow it was shot from has the Flame II enchantment, if yes, it places a fire block at the entity hit
            World world = this.getWorld();
            BlockPos hitpos = entityHitResult.getEntity().getBlockPos();
            if(world.getBlockState(hitpos).isAir()) world.setBlockState(hitpos, Blocks.FIRE.getDefaultState());
            else if(world.getBlockState(hitpos.up()).isAir()) world.setBlockState(hitpos.up(), Blocks.FIRE.getDefaultState());
        }
    }

    public void setFlame2(ItemStack stack){
        if(stack.isOf(Items.BOW)) this.fromFlame2 = (EnchantmentHelper.getEquipmentLevel(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE.getEntry(ModEnchantments.FLAME_TWO.getValue().)) > 0);
    }

    */



}
