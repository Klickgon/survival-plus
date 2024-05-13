package survivalplus.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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
	}
}