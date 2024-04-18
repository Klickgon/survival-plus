package survivalplus.modid;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.entity.ModEntities;
import survivalplus.modid.entity.custom.*;

public class SurvivalPlus implements ModInitializer {
	public static final String MOD_ID = "survival-plus";

    public static final Logger LOGGER = LoggerFactory.getLogger("survival-plus");

	@Override
	public void onInitialize() {
	FabricDefaultAttributeRegistry.register(ModEntities.REEPER, ReeperEntity.createReeperAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.BUILDERZOMBIE, BuilderZombieEntity.createZombieAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.MINERZOMBIE, MinerZombieEntity.createZombieAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.LUMBERJACKZOMBIE, LumberjackZombieEntity.createZombieAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.DIGGINGZOMBIE, DiggingZombieEntity.createZombieAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.SCORCHEDSKELETON, ScorchedSkeletonEntity.createAbstractSkeletonAttributes());

	ModEnchantments.registerModEnchantment();

	ModEntities.registerModEntities();
	ModEntities.registerSpawnRestrictions();
	}
}