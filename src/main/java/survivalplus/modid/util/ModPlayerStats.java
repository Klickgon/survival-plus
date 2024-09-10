package survivalplus.modid.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModPlayerStats {

    public static final Identifier TIME_WITHOUT_CUSTOM_RESPAWNPOINT = Identifier.of(SurvivalPlus.MOD_ID, "timewithoutcustomrespawnpoint");

    public static final Identifier TIME_SINCE_SLEEP = Identifier.of(SurvivalPlus.MOD_ID, "timesincesleep");

    public static final Identifier BASEASSAULTS_WON = Identifier.of(SurvivalPlus.MOD_ID, "baseassaultswon");

    public static void registerModPlayerStats(){
        Registry.register(Registries.CUSTOM_STAT, "timewithoutcustomrespawnpoint", TIME_WITHOUT_CUSTOM_RESPAWNPOINT);
        Stats.CUSTOM.getOrCreateStat(TIME_WITHOUT_CUSTOM_RESPAWNPOINT, StatFormatter.TIME);
        Registry.register(Registries.CUSTOM_STAT, "timesincesleep", TIME_SINCE_SLEEP);
        Stats.CUSTOM.getOrCreateStat(TIME_SINCE_SLEEP, StatFormatter.TIME);
        Registry.register(Registries.CUSTOM_STAT, "baseassaultswon", BASEASSAULTS_WON);
        Stats.CUSTOM.getOrCreateStat(BASEASSAULTS_WON, StatFormatter.DEFAULT);
    }
}

