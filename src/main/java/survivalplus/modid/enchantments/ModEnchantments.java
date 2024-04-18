package survivalplus.modid.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModEnchantments {

    public static Enchantment FLAME_TWO = register("flame_two", new Flame2Enchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));

    private static Enchantment register(String name, Enchantment enchantment) {
    return Registry.register(Registries.ENCHANTMENT, new Identifier(SurvivalPlus.MOD_ID, name), enchantment);
    }

    public static void registerModEnchantment(){
    }
}
