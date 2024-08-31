package survivalplus.modid.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
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

import java.util.Optional;

import static net.minecraft.block.RespawnAnchorBlock.CHARGES;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityChanger extends PlayerEntity implements IServerPlayerChanger {

    public ServerPlayerEntityChanger(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean isCreative();

    @Shadow @Nullable public abstract BlockPos getSpawnPointPosition();

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract void resetStat(Stat<?> stat);

    @Shadow public abstract void increaseStat(Stat<?> stat, int amount);

    @Shadow private @Nullable BlockPos spawnPointPosition;

    @Shadow private RegistryKey<World> spawnPointDimension;

    @Shadow private float spawnAngle;

    @Shadow private boolean spawnForced;

    @Unique public boolean shouldNotSpawnAtAnchor = false;

    @Unique public Optional<ModRespawnPos> findModRespawnPosition(ServerWorld world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive) {
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block instanceof RespawnAnchorBlock && (spawnForced || blockState.get(RespawnAnchorBlock.CHARGES) > 0) && RespawnAnchorBlock.isNether(world)) {
                Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos);
                if (!spawnForced && !alive && optional.isPresent()) {
                    world.setBlockState(pos, blockState.with(RespawnAnchorBlock.CHARGES, blockState.get(RespawnAnchorBlock.CHARGES) - 1), Block.NOTIFY_ALL);
                }
                return optional.map(respawnPos -> ModRespawnPos.fromCurrentPos(respawnPos, pos));
            }
            if (block instanceof BedBlock && BedBlock.isBedWorking(world)) {
                return BedBlock.findWakeUpPosition(EntityType.PLAYER, world, pos, blockState.get(BedBlock.FACING), spawnAngle).map(respawnPos -> ModRespawnPos.fromCurrentPos(respawnPos, pos));
            }
            if (!spawnForced) {
                return Optional.empty();
            }
            boolean bl = block.canMobSpawnInside(blockState);
            BlockState blockState2 = world.getBlockState(pos.up());
            boolean bl2 = blockState2.getBlock().canMobSpawnInside(blockState2);
            if (bl && bl2) {
                return Optional.of(new ModRespawnPos(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.1, (double)pos.getZ() + 0.5), spawnAngle));
            }
            return Optional.empty();
        }


    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private boolean noRespawnPointPunishment(ServerPlayerEntity instance){
        BlockPos bpos = this.getSpawnPointPosition();
        boolean bl;
        if(bpos == null)
            bl = false;
        else
            bl = this.findModRespawnPosition(instance.getServerWorld(), bpos,0.0f, false, true).isPresent() || this.getWorld().getLevelProperties().getGameRules().getBoolean(ModGamerules.INVENTORY_DROP_W_NO_SPAWN);
        return !this.isSpectator() && !(this.isCreative() || bl);
    }

    @Inject(method = "onDeath", at = @At(value = "TAIL"))
    private void statReset(DamageSource damageSource, CallbackInfo ci){
        PlayerData.getPlayerState(this).baseAssaultTimer = Math.max(Math.min(PlayerData.getPlayerState(this).baseAssaultTimer, 200000) - 48000, 0);
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
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, tempSpawnPos).isPresent())
            cir.setReturnValue(tempSpawnPos);
    }

    @Inject(method = "getSpawnPointDimension", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnDimensionImplementation(CallbackInfoReturnable<RegistryKey<World>> cir){
        ServerWorld world = this.getServerWorld();
        PlayerData pdata = PlayerData.getPlayerState(this);
        BlockPos tempSpawnPos = PlayerData.getPlayerState(this).tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, tempSpawnPos).isPresent()) {
            cir.setReturnValue(pdata.tempSpawnDimension);
        }
    }

    @Inject(method = "getSpawnAngle", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnAngleImplementation(CallbackInfoReturnable<Float> cir){
        ServerWorld world = this.getServerWorld();
        PlayerData pdata = PlayerData.getPlayerState(this);
        BlockPos tempSpawnPos = PlayerData.getPlayerState(this).tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, tempSpawnPos).isPresent())
            cir.setReturnValue(pdata.tempSpawnAngle);
    }

    @Inject(method = "isSpawnForced", at = @At(value = "HEAD"), cancellable = true)
    private void tempSpawnForcedBooleanImplementation(CallbackInfoReturnable<Boolean> cir){
        ServerWorld world = this.getServerWorld();
        PlayerData pdata = PlayerData.getPlayerState(this);
        BlockPos tempSpawnPos = pdata.tempSpawnPosition;
        if(tempSpawnPos != null && isValidRespawnAnchor(tempSpawnPos, world) && RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, tempSpawnPos).isPresent())
            cir.setReturnValue(pdata.tempSpawnForced);
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
    }

    @Inject(method = "setSpawnPoint", at = @At(value = "TAIL"))
    private void mainSpawnImplementation(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci){
        PlayerData pData = PlayerData.getPlayerState(this);
        pData.mainSpawnPosition = this.spawnPointPosition;
        pData.mainSpawnDimension = this.spawnPointDimension;
        pData.mainSpawnAngle = this.spawnAngle;
        pData.mainSpawnForced = this.spawnForced;
    }

    @Inject(method = "teleportTo", at = @At(value = "TAIL"))
    private void endPortalToOverworldFix(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir){
        PlayerData pData = PlayerData.getPlayerState(this);
        pData.mainSpawnPosition = this.spawnPointPosition;
        pData.mainSpawnDimension = this.spawnPointDimension;
        pData.mainSpawnAngle = this.spawnAngle;
        pData.mainSpawnForced = this.spawnForced;
    }

    @Unique
    public boolean isValidRespawnAnchor(BlockPos pos, World world){
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.RESPAWN_ANCHOR) && state.get(CHARGES) > 0;
    }

    @Unique
    public BlockPos getMainSpawnPoint(){
        return this.spawnPointPosition;
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
