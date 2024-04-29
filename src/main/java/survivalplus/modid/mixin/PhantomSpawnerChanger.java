package survivalplus.modid.mixin;

import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import survivalplus.modid.util.ModPlayerStats;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerChanger {

    @ModifyArg(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/ServerStatHandler;getStat(Lnet/minecraft/stat/Stat;)I"))
    public Stat statChanger(Stat par1){
        return Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT);
    }
}
