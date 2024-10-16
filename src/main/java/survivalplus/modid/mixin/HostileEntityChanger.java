package survivalplus.modid.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.world.baseassaults.BaseAssault;

@Mixin(HostileEntity.class)
public abstract class HostileEntityChanger extends PathAwareEntity implements IHostileEntityChanger {

    @Unique
    public BaseAssault baseAssault;

    protected HostileEntityChanger(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "canSpawnInDark", at = @At("HEAD"), cancellable = true)
    private static void SpawnDayReq(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir){
        if(spawnReason == SpawnReason.NATURAL) {
            // Cuts hostile entity spawnrate in half during the first day on the surface, but mobs can still spawn normally under full opaque blocks during the first day
            boolean firstNightRestriction = true;
            if (world.getLevelProperties().getTimeOfDay() <= 24000L)
                firstNightRestriction = firstNightSpawnRestriction(pos, world.toServerWorld());

            cir.setReturnValue(world.getDifficulty() != Difficulty.PEACEFUL && (firstNightRestriction || !world.getLevelProperties().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION)) && HostileEntity.isSpawnDark(world, pos, random) && HostileEntity.canMobSpawn(type, world, spawnReason, pos, random));
        }
    }

    @Unique
    private static boolean firstNightSpawnRestriction(BlockPos pos, World world) {
        BlockPos.Mutable blockPos = new BlockPos.Mutable(pos.getX(), pos.getY() + 32, pos.getZ());
        while (blockPos.getY() > pos.getY()) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isOpaqueFullCube(world, blockPos))
                return true;
            blockPos.move(Direction.DOWN);
        }
        return world.random.nextBoolean();
    }

    @Override
    public void setBaseAssault(@Nullable BaseAssault ba) {
        this.baseAssault = ba;
    }

    public BaseAssault getBaseAssault(){
        return this.baseAssault;
    }

    public GoalSelector getGoalSelector(){
        return this.goalSelector;
    }

    public BlockPos getElevatedBlockPos(){
        return new BlockPos(this.getBlockX(), (int) Math.rint(this.getY() + 0.1), this.getBlockZ());
    }
}
