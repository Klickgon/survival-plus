package survivalplus.modid.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModPlayerStats {
    public static final Identifier TIME_WITHOUT_ALL_PLAYERS_WITH_RESPAWNPOINT = new Identifier(SurvivalPlus.MOD_ID, "timewithoutallplayerswithrespawnpoint");
    public static final Identifier TIME_SINCE_SLEEP = new Identifier(SurvivalPlus.MOD_ID, "timesincesleep");

    public static void registerModPlayerStats(){
        Registry.register(Registries.CUSTOM_STAT, "timewithoutallplayerswithrespawnpoint", TIME_WITHOUT_ALL_PLAYERS_WITH_RESPAWNPOINT);
        Registry.register(Registries.CUSTOM_STAT, "timesincesleep", TIME_SINCE_SLEEP);
    }
}

