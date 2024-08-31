package survivalplus.modid.enchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.enchantment.effect.entity.IgniteEnchantmentEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import survivalplus.modid.enchantment_effects.FlameTwoEnchantmentEffect;
import survivalplus.modid.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    private static void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... condition){
        entries.add(key, builder.build(key.getValue()), condition);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        RegistryWrapper<Item> itemLookUp = registries.getWrapperOrThrow(RegistryKeys.ITEM);
        RegistryWrapper<Enchantment> enchantmentLookUp = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

        register(entries, ModEnchantments.FLAME_TWO, Enchantment.builder(Enchantment.definition(
                itemLookUp.getOrThrow(ItemTags.BOW_ENCHANTABLE),
                2, // Enchantment weight
                1, // Max Level
                Enchantment.constantCost(30), // Cost per Level (base)
                Enchantment.constantCost(60), // Cost per Level (max)
                8, // Anvil Cost
                AttributeModifierSlot.MAINHAND
            ))
                .addEffect(EnchantmentEffectComponentTypes.PROJECTILE_SPAWNED,
                new IgniteEnchantmentEffect(EnchantmentLevelBasedValue.constant(100.0f)))

                .addEffect(EnchantmentEffectComponentTypes.POST_ATTACK,
                EnchantmentEffectTarget.ATTACKER,
                EnchantmentEffectTarget.VICTIM,
                new FlameTwoEnchantmentEffect(EnchantmentLevelBasedValue.constant(1.0f)))

                .exclusiveSet(enchantmentLookUp.getOrThrow(ModTags.Enchantments.FLAME_EXCLUSIVE_SET))
        );

        register(entries, ModEnchantments.RAPID_SWING, Enchantment.builder(Enchantment.definition(
                itemLookUp.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                2, // Enchantment weight
                1, // Max Level
                Enchantment.constantCost(30), // Cost per Level (base)
                Enchantment.constantCost(60), // Cost per Level (max)
                8, // Anvil Cost
                AttributeModifierSlot.MAINHAND
                ))

                .exclusiveSet(enchantmentLookUp.getOrThrow(ModTags.Enchantments.SWING_MECHANIC_EXCLUSIVE_SET))
        );
    }

    @Override
    public String getName() {
        return "Enchantment Generator";
    }
}
