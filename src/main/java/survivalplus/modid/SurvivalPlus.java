package survivalplus.modid;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import survivalplus.modid.entity.ModEntities;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.entity.custom.ReeperEntity;

public class SurvivalPlus implements ModInitializer {
	public static final String MOD_ID = "survival-plus";

    public static final Logger LOGGER = LoggerFactory.getLogger("survival-plus");

	@Override
	public void onInitialize() {
	FabricDefaultAttributeRegistry.register(ModEntities.REEPER, ReeperEntity.createReeperAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.BUILDERZOMBIE, BuilderZombieEntity.createZombieAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.MINERZOMBIE, MinerZombieEntity.createZombieAttributes());

	FabricDefaultAttributeRegistry.register(ModEntities.LUMBERJACKZOMBIE, MinerZombieEntity.createZombieAttributes());
	}
}