package survivalplus.modid.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class ModGamerules {
    public static final GameRules.Key<GameRules.BooleanRule> MOB_SPAWN_PROGRESSION = GameRuleRegistry.register("doMobSpawnProgression", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(true));

    public static void registerModGamerules(){

    }
}

