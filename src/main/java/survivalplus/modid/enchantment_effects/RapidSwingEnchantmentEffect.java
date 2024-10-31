package survivalplus.modid.enchantment_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record RapidSwingEnchantmentEffect(EnchantmentLevelBasedValue levelBasedValue) implements EnchantmentEntityEffect {

    public static final MapCodec<RapidSwingEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EnchantmentLevelBasedValue.CODEC.fieldOf("levelBasedValue").forGetter(RapidSwingEnchantmentEffect::levelBasedValue))
                    .apply(instance, RapidSwingEnchantmentEffect::new)
    );

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity target, Vec3d pos) {
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
