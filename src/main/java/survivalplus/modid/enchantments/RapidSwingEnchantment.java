package survivalplus.modid.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.tag.ItemTags;

public class RapidSwingEnchantment extends Enchantment {


    public RapidSwingEnchantment() {
        super(Enchantment.properties(ItemTags.SWORD_ENCHANTABLE, 1, 1, Enchantment.constantCost(30),
                Enchantment.constantCost(55), 5, EquipmentSlot.MAINHAND));
    }


    @Override
    protected boolean canAccept(Enchantment other) {
        if(other == Enchantments.SWEEPING_EDGE) return false;
        return this != other;
    }
}
