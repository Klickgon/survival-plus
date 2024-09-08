package survivalplus.modid.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.custom.*;

public class ModEntities {



    public static final EntityType<ReeperEntity> REEPER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"reeper"),
            EntityType.Builder.create(ReeperEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.7f).build());

    public static final EntityType<BuilderZombieEntity> BUILDERZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"builderzombie"),
            EntityType.Builder.create(BuilderZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).build());

    public static final EntityType<MinerZombieEntity> MINERZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"minerzombie"),
            EntityType.Builder.create(MinerZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).build());

    public static final EntityType<LumberjackZombieEntity> LUMBERJACKZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"lumberjackzombie"),
            EntityType.Builder.create(LumberjackZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).build());

    public static final EntityType<DiggingZombieEntity> DIGGINGZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"diggingzombie"),
            EntityType.Builder.create(DiggingZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).build());

    public static final EntityType<ScorchedSkeletonEntity> SCORCHEDSKELETON = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"scorchedskeleton"),
            EntityType.Builder.create(ScorchedSkeletonEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.99f).build());

    public static final EntityType<LeapingSpiderEntity> LEAPINGSPIDER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(SurvivalPlus.MOD_ID,"leapingspider"),
            EntityType.Builder.create(LeapingSpiderEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.4f, 0.9f).build());

    public static void registerSpawnRestrictions() {
        SpawnRestriction.register(ModEntities.DIGGINGZOMBIE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DiggingZombieEntity::canSpawn);
        SpawnRestriction.register(ModEntities.SCORCHEDSKELETON, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ScorchedSkeletonEntity::canSpawn);
        SpawnRestriction.register(ModEntities.MINERZOMBIE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MinerZombieEntity::canSpawn);
        SpawnRestriction.register(ModEntities.LUMBERJACKZOMBIE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, LumberjackZombieEntity::canSpawn);
        SpawnRestriction.register(ModEntities.BUILDERZOMBIE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BuilderZombieEntity::canSpawn);
        SpawnRestriction.register(ModEntities.REEPER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ReeperEntity::canSpawn);
        SpawnRestriction.register(ModEntities.LEAPINGSPIDER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, LeapingSpiderEntity::canSpawn);
    }



    public static void registerModEntities(){
    }
}
