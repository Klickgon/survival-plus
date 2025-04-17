package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
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
import survivalplus.modid.util.*;
import survivalplus.modid.world.baseassaults.BaseAssault;
import survivalplus.modid.world.baseassaults.BaseAssaultManager;

import java.util.Optional;

import static net.minecraft.block.RespawnAnchorBlock.CHARGES;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityChanger extends PlayerEntity implements IServerPlayerChanger {

    public ServerPlayerEntityChanger(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract void resetStat(Stat<?> stat);

    @Shadow public abstract void increaseStat(Stat<?> stat, int amount);

    @Shadow private @Nullable ServerPlayerEntity.Respawn respawn;

    @Shadow public abstract @Nullable ServerPlayerEntity.Respawn getRespawn();

    @Unique public boolean shouldNotSpawnAtAnchor = false;

    @Unique
    private static Optional<ModRespawnPos> findModRespawnPosition(ServerWorld world, ServerPlayerEntity.Respawn respawn, boolean bl) {
        BlockPos blockPos = respawn.pos();
        float f = respawn.angle();
        boolean bl2 = respawn.forced();
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock && (bl2 || (Integer)blockState.get(RespawnAnchorBlock.CHARGES) > 0) && RespawnAnchorBlock.isNether(world)) {
            Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, blockPos);
            if (!bl2 && bl && optional.isPresent()) {
                world.setBlockState(blockPos, blockState.with(RespawnAnchorBlock.CHARGES, (Integer)blockState.get(RespawnAnchorBlock.CHARGES) - 1), Block.NOTIFY_ALL);
            }
            return optional.map(respawnPos -> ModRespawnPos.fromCurrentPos(respawnPos, blockPos));
        } else if (block instanceof BedBlock && BedBlock.isBedWorking(world)) {
            return BedBlock.findWakeUpPosition(EntityType.PLAYER, world, blockPos, blockState.get(BedBlock.FACING), f)
                    .map(respawnPos -> ModRespawnPos.fromCurrentPos(respawnPos, blockPos));
        } else if (!bl2) {
            return Optional.empty();
        } else {
            boolean bl3 = block.canMobSpawnInside(blockState);
            BlockState blockState2 = world.getBlockState(blockPos.up());
            boolean bl4 = blockState2.getBlock().canMobSpawnInside(blockState2);
            return bl3 && bl4
                    ? Optional.of(new ModRespawnPos(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.1, blockPos.getZ() + 0.5), f))
                    : Optional.empty();
        }
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private boolean noRespawnPointPunishment(ServerPlayerEntity instance){
        return !this.isSpectator() &&
                !(this.isCreative() || ((this.getRespawn() != null && findModRespawnPosition(instance.getServerWorld(), this.getRespawn(), false).isPresent())
                                || this.getServer().getGameRules().getBoolean(ModGamerules.INVENTORY_DROP_W_NO_SPAWN)));
    }

    @Inject(method = "onDeath", at = @At(value = "TAIL"))
    private void statReset(DamageSource damageSource, CallbackInfo ci){
        PlayerData.getPlayerState(this).baseAssaultTimer = Math.max(Math.min(PlayerData.getPlayerState(this).baseAssaultTimer, BaseAssaultManager.BASE_ASSAULT_TIME_NEEDED - 50000) - 48000, 0);
        this.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP));
        this.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
    }

    @Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;", shift = At.Shift.BEFORE), cancellable = true)
    private void sleepFailureInject(BlockPos pos, CallbackInfoReturnable<Either<SleepFailureReason, Unit>> cir){
        if(!this.isCreative()){
            BaseAssault baseAssault = ((IServerWorldChanger)this.getWorld()).getBaseAssaultAt(this.getBlockPos());
            if(baseAssault != null && !(baseAssault.isFinished() || baseAssault.hasStopped()))
                cir.setReturnValue(Either.left(SleepFailureReason.NOT_SAFE));
        }
    }

    @ModifyExpressionValue(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
    private boolean sleepReqCanceler(boolean original){
        return ((IServerWorldChanger)this.getWorld()).notEnoughTimeSinceRest();
    }

    @Inject(method = "getRespawn", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnPositionImplementation(CallbackInfoReturnable<ServerPlayerEntity.Respawn> cir){
        ServerWorld world = this.getServerWorld();
        ServerPlayerEntity.Respawn tempRespawn = PlayerData.getPlayerState(this).tempRespawn;
        if(tempRespawn != null && isValidRespawnAnchor(tempRespawn, world) )
            cir.setReturnValue(tempRespawn);
    }


    @Inject(method = "setSpawnPoint", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnImplementation(ServerPlayerEntity.Respawn respawn, boolean sendMessage, CallbackInfo ci){
        PlayerData pdata = PlayerData.getPlayerState(this);
        if (respawn != null) {
            if(isValidRespawnAnchor(respawn, this.getWorld())){
                boolean bl;
                bl = respawn.equals(pdata.tempRespawn);
                if (sendMessage && !bl) {
                    this.sendMessage(Text.translatable("block.survival-plus.set_spawn_temp"), false);
                }
                pdata.tempRespawn = respawn;
                ci.cancel();
            }
        }
    }

    @Inject(method = "setSpawnPoint", at = @At(value = "TAIL"))
    private void mainSpawnImplementation(ServerPlayerEntity.Respawn respawn, boolean sendMessage, CallbackInfo ci){
        PlayerData pData = PlayerData.getPlayerState(this);
        pData.mainRespawn = this.respawn;
    }

    @Inject(method = "teleportTo", at = @At(value = "TAIL"))
    private void endPortalToOverworldFix(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir){
        PlayerData pData = PlayerData.getPlayerState(this);
        pData.mainRespawn = this.respawn;
    }

    @Unique
    public boolean isValidRespawnAnchor(ServerPlayerEntity.Respawn respawn, World world){
        BlockState state = world.getBlockState(respawn.pos());
        return state.isOf(Blocks.RESPAWN_ANCHOR) && state.get(CHARGES) > 0 && RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, respawn.pos()).isPresent();
    }

    @Unique
    public ServerPlayerEntity.Respawn getMainSpawn(){
        return this.respawn;
    }

    @Unique
    public BlockPos getMainSpawnPoint(){
        return this.respawn == null ? null : this.respawn.pos();
    }

    @Unique
    public void setShouldNotSpawnAtAnchor(boolean bl){
        this.shouldNotSpawnAtAnchor = bl;
    }

    @Unique
    public boolean getShouldNotSpawnAtAnchor(){
        return this.shouldNotSpawnAtAnchor;
    }


}
