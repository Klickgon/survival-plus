package survivalplus.modid.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.FlameEnchantment;
import net.minecraft.entity.EquipmentSlot;

public class Flame2Enchantment extends FlameEnchantment {

    public Flame2Enchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, slotTypes);
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
