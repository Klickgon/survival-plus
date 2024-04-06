package survivalplus.modid.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModTags {

    public static class Blocks {
        public static final TagKey<Block> MINERZOMBIE_MINABLE = createTag("minerzombie_mineable");

        private static TagKey<Block> createTag(String name){
            return TagKey.of(RegistryKeys.BLOCK, new Identifier(SurvivalPlus.MOD_ID, name));
        }

    }

    public static class Items {

        private static TagKey<Item> createTag(String name){
            return TagKey.of(RegistryKeys.ITEM, new Identifier(SurvivalPlus.MOD_ID, name));
        }

    }

}
