package survivalplus.modid.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.entity.custom.ReeperEntity;

public class ModEntities {
    public static final EntityType<ReeperEntity> REEPER = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SurvivalPlus.MOD_ID,"reeper"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ReeperEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.7f)).build());
    public static final EntityType<BuilderZombieEntity> BUILDERZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SurvivalPlus.MOD_ID,"builderzombie"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, BuilderZombieEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.7f)).build());
    public static final EntityType<MinerZombieEntity> MINERZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(SurvivalPlus.MOD_ID,"minerzombie"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MinerZombieEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.7f)).build());
}
