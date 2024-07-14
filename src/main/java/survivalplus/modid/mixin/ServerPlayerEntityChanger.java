package survivalplus.modid.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.util.ModPlayerStats;

import static net.minecraft.block.RespawnAnchorBlock.CHARGES;

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

    @Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);

    @Shadow public abstract void setSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage);

    @Shadow private @Nullable BlockPos spawnPointPosition;

    @Shadow private RegistryKey<World> spawnPointDimension;

    @Shadow private float spawnAngle;

    @Shadow private boolean spawnForced;

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
        PlayerData.getPlayerState(this).baseAssaultTimer = Math.max(Math.min(PlayerData.getPlayerState(this).baseAssaultTimer, 144000) - 48000, 0);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
    }

    @Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = At.Shift.AFTER), cancellable = true)
    private void sleepFailureInject(BlockPos pos, CallbackInfoReturnable<Either<SleepFailureReason, Unit>> cir){
        if(!this.isCreative() && ((IServerWorldChanger)this.getWorld()).getBaseAssaultAt(this.getBlockPos()) != null){
            cir.setReturnValue(Either.left(SleepFailureReason.NOT_SAFE));
        }
    }

    @Inject(method = "getSpawnPointPosition", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnPositionImplementation(CallbackInfoReturnable<BlockPos> cir){
        ServerWorld world = this.getServerWorld();
        BlockPos tempSpawnPos = PlayerData.getPlayerState(this).tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && PlayerEntity.findRespawnPosition(world, tempSpawnPos, 0.0f, false, true).isPresent()) cir.setReturnValue(tempSpawnPos);
    }

    @Inject(method = "getSpawnPointDimension", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnDimensionImplementation(CallbackInfoReturnable<RegistryKey<World>> cir){
        ServerWorld world = this.getServerWorld();
        PlayerData pdata = PlayerData.getPlayerState(this);
        BlockPos tempSpawnPos = PlayerData.getPlayerState(this).tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && PlayerEntity.findRespawnPosition(world, tempSpawnPos, 0.0f, false, true).isPresent())
            cir.setReturnValue(pdata.tempSpawnDimension);
    }

    @Inject(method = "getSpawnAngle", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnAngleImplementation(CallbackInfoReturnable<Float> cir){
        ServerWorld world = this.getServerWorld();
        PlayerData pdata = PlayerData.getPlayerState(this);
        BlockPos tempSpawnPos = PlayerData.getPlayerState(this).tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && PlayerEntity.findRespawnPosition(world, tempSpawnPos, 0.0f, false, true).isPresent()) cir.setReturnValue(pdata.tempSpawnAngle);
    }

    @Inject(method = "isSpawnForced", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnForcedBooleanImplementation(CallbackInfoReturnable<Boolean> cir){
        ServerWorld world = this.getServerWorld();
        PlayerData pdata = PlayerData.getPlayerState(this);
        BlockPos tempSpawnPos = pdata.tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && PlayerEntity.findRespawnPosition(world, tempSpawnPos, 0.0f, false, true).isPresent()) cir.setReturnValue(pdata.tempSpawnForced);
    }

    @Inject(method = "setSpawnPoint", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnImplementation(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci){
        PlayerData pdata = PlayerData.getPlayerState(this);
        if (pos != null) {
            if(isValidRespawnAnchor(pos, this.getWorld())){
                boolean bl;
                bl = pos.equals(pdata.tempSpawnPosition) && dimension.equals(pdata.tempSpawnDimension);
                if (sendMessage && !bl) {
                    this.sendMessage(Text.translatable("block.survival-plus.set_spawn_temp"));
                }
                pdata.tempSpawnPosition = pos;
                pdata.tempSpawnDimension = dimension;
                pdata.tempSpawnAngle = angle;
                pdata.tempSpawnForced = forced;
                ci.cancel();
            }
        }
        else {
            pdata.tempSpawnPosition = null;
            pdata.tempSpawnDimension = World.OVERWORLD;
            pdata.tempSpawnAngle = 0.0f;
            pdata.tempSpawnForced = false;
        }
    }

    @Inject(method = "setSpawnPoint", at = @At(value = "TAIL"))
    private void mainSpawnImplementation(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci){
        PlayerData pData = PlayerData.getPlayerState(this);
        pData.mainSpawnPosition = this.spawnPointPosition;
        pData.mainSpawnDimension = this.spawnPointDimension;
        pData.mainSpawnAngle = this.spawnAngle;
        pData.mainSpawnForced = this.spawnForced;
    }

    @Unique
    private boolean isValidRespawnAnchor(BlockPos pos, World world){
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.RESPAWN_ANCHOR) && state.get(CHARGES) > 0;
    }

}
