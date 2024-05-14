package survivalplus.modid.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;


public class ModItemGroups {
    public static final ItemGroup SURVIVAL_PLUS_ITEMGROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(SurvivalPlus.MOD_ID, "survival-plus"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.survival-plus"))
                    .icon(() -> new ItemStack(Items.MOSSY_STONE_BRICKS)).entries((displayContext, entries) -> {
                        entries.add(ModItems.REEPER_SPAWN_EGG);
                        entries.add(ModItems.SCORCHED_SKELETON_SPAWN_EGG);
                        entries.add(ModItems.BUILDER_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.DIGGING_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.MINER_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.LUMBERJACK_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.LEAPING_SPIDER_SPAWN_EGG);
                    }).build());



    public static void registerItemGroups(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.REEPER_SPAWN_EGG);});

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.SCORCHED_SKELETON_SPAWN_EGG);});

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.BUILDER_ZOMBIE_SPAWN_EGG);});

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.DIGGING_ZOMBIE_SPAWN_EGG);});

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.MINER_ZOMBIE_SPAWN_EGG);});

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.LUMBERJACK_ZOMBIE_SPAWN_EGG);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
            content.add(ModItems.LEAPING_SPIDER_SPAWN_EGG);
        });
    }
}
