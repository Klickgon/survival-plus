package survivalplus.modid.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.ModEntities;

public class ModItems {
    public static final Item REEPER_SPAWN_EGG = registerItem("reeper_spawn_egg", new SpawnEggItem(ModEntities.REEPER,0xD2D2D2, 0xB73645, new FabricItemSettings()));
    public static final Item SCORCHED_SKELETON_SPAWN_EGG = registerItem("scorched_skeleton_spawn_egg", new SpawnEggItem(ModEntities.SCORCHEDSKELETON,0x2A2A2A, 0xE6DD00, new FabricItemSettings()));
    public static final Item MINER_ZOMBIE_SPAWN_EGG = registerItem("miner_zombie_spawn_egg", new SpawnEggItem(ModEntities.MINERZOMBIE,0xFB5200,0x3E692D,  new FabricItemSettings()));
    public static final Item DIGGING_ZOMBIE_SPAWN_EGG = registerItem("digging_zombie_spawn_egg", new SpawnEggItem(ModEntities.DIGGINGZOMBIE,0x44382D,0x3E692D, new FabricItemSettings()));
    public static final Item BUILDER_ZOMBIE_SPAWN_EGG = registerItem("builder_zombie_spawn_egg", new SpawnEggItem(ModEntities.BUILDERZOMBIE,0xF9FE00,0x3E692D, new FabricItemSettings()));
    public static final Item LUMBERJACK_ZOMBIE_SPAWN_EGG = registerItem("lumberjack_zombie_spawn_egg", new SpawnEggItem(ModEntities.LUMBERJACKZOMBIE, 0x701818,0x3E692D, new FabricItemSettings()));
    public static final Item LEAPING_SPIDER_SPAWN_EGG = registerItem("leaping_spider_spawn_egg", new SpawnEggItem(ModEntities.LEAPINGSPIDER, 0xFFD30E, 0x172513, new FabricItemSettings()));

    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, new Identifier(SurvivalPlus.MOD_ID, name), item);
    }

    public static void registerModItems(){
    }
}
