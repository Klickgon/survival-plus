package survivalplus.modid.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.util.IServerPlayerChanger;

@Mixin(PlayerManager.class)
public class PlayerManagerChanger {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
    private void setSpawnPointRedirect(ServerPlayerEntity instance, RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage){
        PlayerData pdata = PlayerData.getPlayerState(instance); // Necessary so the player has a non Respawn Anchor block spawnpoint after death due to it actually not being saved after death
        instance.setSpawnPoint(pdata.mainSpawnDimension, pdata.mainSpawnPosition, pdata.mainSpawnAngle, pdata.mainSpawnForced, false);
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointPosition()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos endPortalToOverworldFixDimension(ServerPlayerEntity instance){
        IServerPlayerChanger Iplayer = ((IServerPlayerChanger)instance);
        BlockPos bpos = Iplayer.getShouldNotSpawnAtAnchor() ? Iplayer.getMainSpawnPoint() : instance.getSpawnPointPosition();
        return bpos;
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getSpawnPointDimension()Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> endPortalToOverworldFixPosition(ServerPlayerEntity instance){
        return ((IServerPlayerChanger)instance).getShouldNotSpawnAtAnchor() ? World.OVERWORLD : instance.getSpawnPointDimension();
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpawnForced()Z"))
    private boolean endPortalToOverworldFixBoolean(ServerPlayerEntity instance){
        return !((IServerPlayerChanger)instance).getShouldNotSpawnAtAnchor() && instance.isSpawnForced();
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setMainArm(Lnet/minecraft/util/Arm;)V"))
    private void anchorSkipFieldReset(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir){
        ((IServerPlayerChanger)player).setShouldNotSpawnAtAnchor(false);
    }

}
