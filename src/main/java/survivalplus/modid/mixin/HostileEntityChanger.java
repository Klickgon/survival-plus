package survivalplus.modid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class HostileEntityChanger {

    @Inject(method = "canSpawnInDark", at = @At("RETURN"), cancellable = true)
    private static void SpawnDayReq(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir){
        // Blocks hostile Entity Spawns during the first day on the surface, but mobs can still spawn underground during the first day
        boolean afterOneDay;
        if (world.getLevelProperties().getTimeOfDay() <= 24000L) {
            afterOneDay = !world.isSkyVisible(pos);
        }
        else afterOneDay = true;
        cir.setReturnValue(world.getDifficulty() != Difficulty.PEACEFUL && afterOneDay && HostileEntity.isSpawnDark(world, pos, random) && HostileEntity.canMobSpawn(type, world, spawnReason, pos, random));
    }
}
