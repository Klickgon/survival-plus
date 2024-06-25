package survivalplus.modid.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.util.ModPlayerStats;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityChanger extends PlayerEntity{

    public ServerPlayerEntityChanger(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean isCreative();

    @Shadow @Nullable public abstract BlockPos getSpawnPointPosition();

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract void resetStat(Stat<?> stat);

    @Shadow public abstract void increaseStat(Stat<?> stat, int amount);

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private boolean noRespawnPointPunishment(ServerPlayerEntity instance){
        BlockPos bpos = this.getSpawnPointPosition();
        boolean bl;
        if(bpos == null)
            bl = false;
        else
            bl = ServerPlayerEntity.findRespawnPosition(this.getServerWorld(), bpos, 0.0f, false, true).isPresent() || this.getWorld().getLevelProperties().getGameRules().getBoolean(ModGamerules.INVENTORY_DROP_W_NO_SPAWN);
        return !this.isSpectator() && !(this.isCreative() || bl);
    }

    @Inject(method = "onDeath", at = @At(value = "TAIL"))
    private void statReset(DamageSource damageSource, CallbackInfo ci){
        PlayerData.getPlayerState(this).baseAssaultTimer = Math.max(PlayerData.getPlayerState(this).baseAssaultTimer - 48000, 0);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
    }

    @Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = At.Shift.AFTER), cancellable = true)
    private void sleepFailureInject(BlockPos pos, CallbackInfoReturnable<Either<SleepFailureReason, Unit>> cir){
        if(!this.isCreative() && ((IServerWorldChanger)this.getWorld()).getBaseAssaultAt(this.getBlockPos()) != null){
            cir.setReturnValue(Either.left(SleepFailureReason.NOT_SAFE));
        }
    }

}
