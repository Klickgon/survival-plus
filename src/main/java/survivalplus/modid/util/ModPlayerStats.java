package survivalplus.modid.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModPlayerStats {
    public static final Identifier TIME_WITHOUT_CUSTOM_RESPAWNPOINT = new Identifier(SurvivalPlus.MOD_ID, "timewithoutcustomrespawnpoint");
    public static final Identifier TIME_SINCE_SLEEP = new Identifier(SurvivalPlus.MOD_ID, "timesincesleep");
    public static final Identifier TIME_SINCE_LAST_BASEASSAULT = new Identifier(SurvivalPlus.MOD_ID, "timesincelastbaseassault");
    public static final Identifier BASEASSAULTS_WON = new Identifier(SurvivalPlus.MOD_ID, "baseassaultswon");

    public static void registerModPlayerStats(){
        Registry.register(Registries.CUSTOM_STAT, "timewithoutcustomrespawnpoint", TIME_WITHOUT_CUSTOM_RESPAWNPOINT);
        Registry.register(Registries.CUSTOM_STAT, "timesincesleep", TIME_SINCE_SLEEP);
        Registry.register(Registries.CUSTOM_STAT, "timesincelastbaseassault", TIME_SINCE_LAST_BASEASSAULT);
        Registry.register(Registries.CUSTOM_STAT, "baseassaultswon", BASEASSAULTS_WON);
    }
}

