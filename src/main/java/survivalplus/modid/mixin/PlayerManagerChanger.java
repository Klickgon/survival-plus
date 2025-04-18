package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.util.IServerPlayerChanger;

@Mixin(PlayerManager.class)
public class PlayerManagerChanger {

    @Shadow @Final private MinecraftServer server;

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPointFrom(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void setSpawnPointRedirect(ServerPlayerEntity instance, ServerPlayerEntity player){
        PlayerData pdata = PlayerData.getPlayerState(instance); // Necessary so the player has a non Respawn Anchor block spawnpoint after death due to it actually not being saved after death
        instance.setSpawnPoint(pdata.mainRespawn, false);
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"))
    private TeleportTarget endPortalToOverworldFixDimension(ServerPlayerEntity instance, boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition){
        return ((IServerPlayerChanger)instance).getShouldNotSpawnAtAnchor() ? new TeleportTarget(instance.getServerWorld(), instance, TeleportTarget.NO_OP) : instance.getRespawnTarget(alive, TeleportTarget.NO_OP);
    }

    @ModifyExpressionValue(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean differentDimensionSpawnSoundFix(boolean original, ServerPlayerEntity serverPlayerEntity, @Local ServerPlayerEntity.Respawn respawn){
        return original && serverPlayerEntity.getWorld().getRegistryKey() == respawn.dimension();
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setMainArm(Lnet/minecraft/util/Arm;)V"))
    private void anchorSkipFieldReset(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir){
        ((IServerPlayerChanger)player).setShouldNotSpawnAtAnchor(false);
    }

}
