package survivalplus.modid.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import survivalplus.modid.entity.ModEntities;


public class ModEntityGeneration {

    public static void addSpawns(){
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.SCORCHEDSKELETON, 25, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.MINERZOMBIE, 40, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.LUMBERJACKZOMBIE, 35, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.DIGGINGZOMBIE, 30, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.BUILDERZOMBIE, 25, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.REEPER, 20, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.LEAPINGSPIDER, 40, 1, 3);
    }
}
