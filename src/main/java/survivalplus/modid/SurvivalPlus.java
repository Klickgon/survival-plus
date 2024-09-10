package survivalplus.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.entity.ModEntities;
import survivalplus.modid.entity.custom.*;
import survivalplus.modid.item.ModItemGroups;
import survivalplus.modid.item.ModItems;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.util.ModPlayerStats;
import survivalplus.modid.world.gen.ModEntityGeneration;

public class SurvivalPlus implements ModInitializer {
	public static final String MOD_ID = "survival-plus";

    public static final Logger LOGGER = LoggerFactory.getLogger("survival-plus");

	private static final RegistryKey<LootTable> FROG_LOOT_TABLE_ID = EntityType.FROG.getLootTableId();

	private static final RegistryKey<LootTable> GOAT_LOOT_TABLE_ID = EntityType.GOAT.getLootTableId();

	private static final RegistryKey<LootTable> SNIFFER_LOOT_TABLE_ID = EntityType.SNIFFER.getLootTableId();

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(ModEntities.REEPER, ReeperEntity.createReeperAttributes());

		FabricDefaultAttributeRegistry.register(ModEntities.BUILDERZOMBIE, BuilderZombieEntity.createZombieAttributes());

		FabricDefaultAttributeRegistry.register(ModEntities.MINERZOMBIE, MinerZombieEntity.createZombieAttributes());

		FabricDefaultAttributeRegistry.register(ModEntities.LUMBERJACKZOMBIE, LumberjackZombieEntity.createLumberZombieAttributes());

		FabricDefaultAttributeRegistry.register(ModEntities.DIGGINGZOMBIE, DiggingZombieEntity.createZombieAttributes());

		FabricDefaultAttributeRegistry.register(ModEntities.SCORCHEDSKELETON, ScorchedSkeletonEntity.createAbstractSkeletonAttributes());

		FabricDefaultAttributeRegistry.register(ModEntities.LEAPINGSPIDER, LeapingSpiderEntity.createSpiderAttributes());

		ModEntityGeneration.addSpawns();

		ModEnchantments.registerModEnchantment();

		ModEntities.registerModEntities();
		ModEntities.registerSpawnRestrictions();

		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		ModGamerules.registerModGamerules();
		ModPlayerStats.registerModPlayerStats();

		LootTableEvents.MODIFY.register((lootTableRegistryKey, builder, lootTableSource, wrapperLookup) -> {
			if(lootTableSource.isBuiltin()){
				if (FROG_LOOT_TABLE_ID.equals(lootTableRegistryKey)) {
					LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(Items.SLIME_BALL))
							.rolls(new UniformLootNumberProvider(new ConstantLootNumberProvider(0.0f), new ConstantLootNumberProvider(1.0f)));
					builder.pool(poolBuilder);
				}
				if (GOAT_LOOT_TABLE_ID.equals(lootTableRegistryKey)) {
					LootPool.Builder poolBuilder = LootPool.builder().with(ItemEntry.builder(Items.WHITE_WOOL))
							.rolls(new UniformLootNumberProvider(new ConstantLootNumberProvider(0.0f), new ConstantLootNumberProvider(1.0f)));
					builder.pool(poolBuilder);
				}
				if (SNIFFER_LOOT_TABLE_ID.equals(lootTableRegistryKey)) {
					LootPool.Builder poolBuilder = LootPool.builder()
							.with(ItemEntry.builder(Items.MOSSY_STONE_BRICKS)).with(ItemEntry.builder(Items.MOSSY_COBBLESTONE)).rolls(new UniformLootNumberProvider(new ConstantLootNumberProvider(0.0f), new ConstantLootNumberProvider(1.0f)));
					builder.pool(poolBuilder);

					poolBuilder = LootPool.builder()
							.with(ItemEntry.builder(Items.MOSS_BLOCK)).rolls(new UniformLootNumberProvider(new ConstantLootNumberProvider(0.0f), new ConstantLootNumberProvider(1.0f)));
					builder.pool(poolBuilder);

					poolBuilder = LootPool.builder()
							.with(ItemEntry.builder(Items.RED_WOOL)).rolls(new UniformLootNumberProvider(new ConstantLootNumberProvider(0.0f), new ConstantLootNumberProvider(2.0f)));
					builder.pool(poolBuilder);
				}
			}
		});
	}
}