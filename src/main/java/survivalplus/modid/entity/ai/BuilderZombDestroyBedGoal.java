package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.BuilderZombieEntity;

public class BuilderZombDestroyBedGoal extends MoveToTargetPosGoal {

    private final BuilderZombieEntity DestroyMob;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;

    public BuilderZombDestroyBedGoal(BuilderZombieEntity mob, double speed, int maxYDifference) {
        super(mob, speed, 16, maxYDifference);
        this.DestroyMob = mob;
        this.cooldown = 0;
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        if (!this.DestroyMob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        this.cooldown = 400 + this.mob.getWorld().random.nextInt(100);
        return this.findTargetPos();
    }

    @Override
    public void stop() {
        super.stop();
        DestroyMob.targetBedPos = null;
        this.DestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        DestroyMob.targetBedPos = this.targetPos;
    }


    @Override
    public void tick() {
        super.tick();
        World world = this.DestroyMob.getWorld();
        BlockPos blockPos = this.DestroyMob.getBlockPos();
        BlockPos blockPos2 = this.tweakToProperPos(blockPos, world);
        if (blockPos2 != null && blockPos2.isWithinDistance(blockPos, 3)) {
            world.breakBlock(blockPos2, true);
        }
    }


    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        BlockPos[] blockPoss;
        if (world.getBlockState(pos).isIn(this.BedGroup)) {
            return pos;
        }
        for (BlockPos blockPos : blockPoss = new BlockPos[]{pos.west(), pos.east(), pos.north(), pos.south(), pos.up()}) {
            if (!world.getBlockState(blockPos).isIn(this.BedGroup)) continue;
            return blockPos;
        }
        return null;
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            return chunk.getBlockState(pos.down()).isIn(this.BedGroup);
        }
        return false;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 1.14;
    }
}