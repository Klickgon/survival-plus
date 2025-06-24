package survivalplus.modid.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import survivalplus.modid.PlayerData;
import survivalplus.modid.util.IServerPlayerChanger;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerChanger {

    @Redirect(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    public int statChanger(int value, int min, int max, @Local ServerPlayerEntity serverPlayerEntity){
        return PlayerData.getPlayerState(serverPlayerEntity).phantomTimer;
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
