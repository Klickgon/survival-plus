package survivalplus.modid;

import survivalplus.modid.world.baseassaults.BaseAssaultWaves;

public class PlayerData extends StateSaverAndLoader{
    public int baseAssaultTimer = 0;
    public byte[] generatedWave = BaseAssaultWaves.BASEASSAULT_TWELVE;
}
