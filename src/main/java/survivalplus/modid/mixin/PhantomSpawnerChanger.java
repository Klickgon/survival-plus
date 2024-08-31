package survivalplus.modid.mixin;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.ModPlayerStats;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerChanger {

    @ModifyArg(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/ServerStatHandler;getStat(Lnet/minecraft/stat/Stat;)I"))
    public Stat statChanger(Stat par1){
        return Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT);
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 72000))
    private int phantomSpawnTimerChanger(int constant){
        return 120000;
    }

    @Inject(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getPlayers()Ljava/util/List;"), cancellable = true)
    private void noBedSpawnPointCheck(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir){
        for (ServerPlayerEntity player : world.getPlayers()){
            if (!world.getBlockState(((IServerPlayerChanger)player).getMainSpawnPoint()).isIn(BlockTags.BEDS)) {
                cir.setReturnValue(0);
            }
        }
    }

}
