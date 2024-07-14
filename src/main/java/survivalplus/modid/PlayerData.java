package survivalplus.modid;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import survivalplus.modid.world.baseassaults.BaseAssaultWaves;

public class PlayerData extends StateSaverAndLoader{
    public int baseAssaultTimer = 0;
    public byte[] generatedWave = BaseAssaultWaves.BASEASSAULT_TWELVE;
    public BlockPos tempSpawnPosition = null;
    public RegistryKey<World> tempSpawnDimension = World.OVERWORLD;
    public float tempSpawnAngle = 0.0f;
    public boolean tempSpawnForced = false;
    public BlockPos mainSpawnPosition = null;
    public RegistryKey<World> mainSpawnDimension = World.OVERWORLD;
    public float mainSpawnAngle = 0.0f;
    public boolean mainSpawnForced = false;
}
