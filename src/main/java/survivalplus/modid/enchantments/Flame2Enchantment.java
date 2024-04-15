package survivalplus.modid.enchantments;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.FlameEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class Flame2Enchantment extends FlameEnchantment {
    public Flame2Enchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, slotTypes);
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level){
        if(!user.getWorld().isClient){
            ServerWorld sworld = (ServerWorld) user.getWorld();
            BlockPos tpos = target.getBlockPos();

            sworld.setBlockState(tpos, Blocks.FIRE.getDefaultState());

        }
        super.onTargetDamaged(user, target, level);
    }

    @Override
    public int getMinLevel() {
        return 2;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        if(other == Enchantments.FLAME) return false;
        else return this != other;
    }
}
