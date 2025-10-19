package survivalplus.modid.enchantment_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record FlameTwoEnchantmentEffect(EnchantmentLevelBasedValue levelBasedValue) implements EnchantmentEntityEffect {

    public static final MapCodec<FlameTwoEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EnchantmentLevelBasedValue.CODEC.fieldOf("levelBasedValue").forGetter(FlameTwoEnchantmentEffect::levelBasedValue))
                    .apply(instance, FlameTwoEnchantmentEffect::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity target, Vec3d pos) {
        if(target instanceof LivingEntity livingtarget && context.owner() != null && target.isOnFire()){
            BlockPos targetPos = livingtarget.getBlockPos();
            if(world.getBlockState(targetPos).isAir())
                world.setBlockState(targetPos, Blocks.FIRE.getDefaultState());
            else if(world.getBlockState(targetPos.up()).isAir())
                world.setBlockState(targetPos.up(), Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
