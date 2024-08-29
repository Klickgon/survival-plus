package survivalplus.modid.enchantments;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.enchantment_effects.FlameTwoEnchantmentEffect;

public class ModEnchantments {

    public static RegistryKey<Enchantment> FLAME_TWO = of("flame_two");

    public static RegistryKey<Enchantment> RAPID_SWING = of("rapid_swing");

    public static final MapCodec<FlameTwoEnchantmentEffect> FLAME_TWO_ENCHANTMENT_EFFECT = register("flame_two", FlameTwoEnchantmentEffect.CODEC);

    public static final MapCodec<FlameTwoEnchantmentEffect> RAPID_SWING_ENCHANTMENT_EFFECT = register("rapid_swing", FlameTwoEnchantmentEffect.CODEC);

    private static RegistryKey<Enchantment> of(String name) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(SurvivalPlus.MOD_ID, name));
    }

    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String name, MapCodec<T> codec){
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, name, codec);
    }

    public static void registerModEnchantment(){
    }
}
