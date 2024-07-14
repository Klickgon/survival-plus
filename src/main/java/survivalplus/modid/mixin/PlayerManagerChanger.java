package survivalplus.modid.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import survivalplus.modid.PlayerData;

@Mixin(PlayerManager.class)
public class PlayerManagerChanger {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
    private void setSpawnPointRedirect(ServerPlayerEntity instance, RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage){
        PlayerData pdata = PlayerData.getPlayerState(instance); // Necessary so the player has a non Respawn Anchor block spawnpoint after death due to it actually not saved after death
        instance.setSpawnPoint(pdata.mainSpawnDimension, pdata.mainSpawnPosition, pdata.mainSpawnAngle, pdata.mainSpawnForced, false);
    }
}
