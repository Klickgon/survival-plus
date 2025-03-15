package survivalplus.modid.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import survivalplus.modid.entity.ModEntities;


public class ModEntityGeneration {

    public static void addSpawns(){
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.SCORCHEDSKELETON, 25, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.MINERZOMBIE, 35, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.LUMBERJACKZOMBIE, 35, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.DIGGINGZOMBIE, 35, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.BUILDERZOMBIE, 35, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.REEPER, 25, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, ModEntities.LEAPINGSPIDER, 35, 1, 3);
    }
}
