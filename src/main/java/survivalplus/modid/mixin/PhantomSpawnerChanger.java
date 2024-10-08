package survivalplus.modid.mixin;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.ModPlayerStats;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerChanger {

    @ModifyArg(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/ServerStatHandler;getStat(Lnet/minecraft/stat/Stat;)I"))
    public Stat<Identifier> statChanger(Stat<Identifier> par1){
        return Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT);
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 72000))
    private int phantomSpawnTimerChanger(int constant){
        return 120000;
    }

    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private boolean noBedSpawnPointCheck(ServerPlayerEntity instance){
        BlockPos pos = ((IServerPlayerChanger)instance).getMainSpawnPoint();
        return instance.isSpectator() || (pos != null && instance.getWorld().getBlockState(pos).isIn(BlockTags.BEDS));
    }

}
