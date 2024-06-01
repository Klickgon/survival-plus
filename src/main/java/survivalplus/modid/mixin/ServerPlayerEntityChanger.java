package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.world.baseassaults.BaseAssaultWaves;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityChanger extends PlayerEntity implements IServerPlayerChanger {

    public ServerPlayerEntityChanger(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique
    private byte[] generatedWave = BaseAssaultWaves.BASEASSAULT_TWELVE;

    @Unique
    private int timeSinceLastBaseAssault;

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean isCreative();

    @Shadow @Nullable public abstract BlockPos getSpawnPointPosition();

    @Shadow public abstract ServerWorld getServerWorld();

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    public boolean noRespawnPointPunishment(ServerPlayerEntity instance){
        BlockPos bpos = this.getSpawnPointPosition();
        boolean bl;
        if(bpos == null)
            bl = false;
        else
            bl = ServerPlayerEntity.findRespawnPosition(this.getServerWorld(), bpos, 0.0f, false, true).isPresent() || this.getWorld().getLevelProperties().getGameRules().getBoolean(ModGamerules.INVENTORY_DROP_W_NO_SPAWN);
        ((IServerPlayerChanger)this).resetTimeSinceLastBaseAssault();
        return !this.isSpectator() && !(this.isCreative() || bl);
    }

    @Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = At.Shift.AFTER), cancellable = true)
    public void sleepFailureInject(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir){
        if(!this.isCreative() && ((IServerWorldChanger)this.getWorld()).getBaseAssaultAt(this.getBlockPos()) != null){
            cir.setReturnValue(Either.left(SleepFailureReason.NOT_SAFE));
        }
    }

    @ModifyExpressionValue(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
    public boolean SleepCooldownChecker(boolean original){
        return !((IServerWorldChanger)this.getServerWorld()).getEnoughTimeSinceRest();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void nbtWriteInject(NbtCompound nbt, CallbackInfo ci){
        nbt.putByteArray("generatedwave", this.generatedWave);
        nbt.putInt("timesincelastbaseassault", this.timeSinceLastBaseAssault);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void nbtReadInject(NbtCompound nbt, CallbackInfo ci){
        if(nbt.contains("generatedwave"))
            this.generatedWave = nbt.getByteArray("generatedwave");
        if(nbt.contains("timesincelastbaseassault"))
            this.timeSinceLastBaseAssault = nbt.getInt("timesincelastbaseassault");
    }

    @Unique
    public byte[] getGeneratedWave() {
        return this.generatedWave;
    }

    @Unique
    public void setGeneratedWave(byte[] wave) {
        this.generatedWave = wave;
    }

    @Unique
    public void resetTimeSinceLastBaseAssault(){
        this.timeSinceLastBaseAssault = 0;
    }

    @Unique
    public void incrementTimeSinceLastBaseAssault(){
        this.timeSinceLastBaseAssault++;
    }

    @Unique
    public int getTimeSinceLastBaseAssault(){
        return this.timeSinceLastBaseAssault;
    }

    @Unique
    public void increaseTimeSinceLastBaseAssault(int time){
        this.timeSinceLastBaseAssault += time;
    }
}
