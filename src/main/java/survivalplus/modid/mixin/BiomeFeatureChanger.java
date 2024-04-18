package survivalplus.modid.mixin;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalplus.modid.entity.ModEntities;

@Mixin(DefaultBiomeFeatures.class)
public abstract class BiomeFeatureChanger {

    @Inject(method = "addMonsters", at = @At("TAIL"))
    private static void mobSpawnInjection(SpawnSettings.Builder builder, int zombieWeight, int zombieVillagerWeight, int skeletonWeight, boolean drowned, CallbackInfo ci){
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(ModEntities.BUILDERZOMBIE, 50, 1, 3));
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(ModEntities.LUMBERJACKZOMBIE, 60, 1, 3));
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(ModEntities.MINERZOMBIE, 60, 1, 3));
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(ModEntities.DIGGINGZOMBIE, 60, 1, 3));
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(ModEntities.REEPER, 50, 1, 3));
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(ModEntities.SCORCHEDSKELETON, 60, 1, 3));
    }

}
