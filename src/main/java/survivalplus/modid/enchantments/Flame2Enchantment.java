package survivalplus.modid.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.tag.ItemTags;

public class Flame2Enchantment extends Enchantment {

    public Flame2Enchantment() {
        super(Enchantment.properties(ItemTags.BOW_ENCHANTABLE, 1, 1, Enchantment.constantCost(30),
                Enchantment.constantCost(55), 5, EquipmentSlot.MAINHAND));
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        if(other == Enchantments.FLAME) return false;
        return this != other;
    }
}
