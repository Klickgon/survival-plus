package survivalplus.modid.util;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> MINERZOMBIE_MINABLE = createTag("minerzombie_mineable");

        public static final TagKey<Block> LUMBERJACKZOMBIE_MINABLE = createTag("lumberjackzombie_mineable");

        public static final TagKey<Block> DIGGINGZOMBIE_MINABLE = createTag("diggingzombie_mineable");

        public static final TagKey<Block> NOT_PASSABLE = createTag("notpassable");

        private static TagKey<Block> createTag(String name){
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(SurvivalPlus.MOD_ID, name));
        }

    }

    /*public static class Items {

        private static TagKey<Item> createTag(String name){
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(SurvivalPlus.MOD_ID, name));
        }

    }*/

    public static class Enchantments {

        public static final TagKey<Enchantment> SWING_MECHANIC_EXCLUSIVE_SET = createTag("swing_mechanic_exclusive_set");

        public static final TagKey<Enchantment> FLAME_EXCLUSIVE_SET = createTag("flame_exclusive_set");

        private static TagKey<Enchantment> createTag(String name){
            return TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(SurvivalPlus.MOD_ID, name));
        }

    }

}
