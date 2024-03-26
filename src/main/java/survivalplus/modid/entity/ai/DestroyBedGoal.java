package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class DestroyBedGoal extends MoveToTargetPosGoal {

    private final HostileEntity DestroyMob;
    private int counter;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;

    private static final int MAX_COOLDOWN = 20;

    public DestroyBedGoal(HostileEntity mob, double speed, int maxYDifference) {
        super(mob, speed, 512, maxYDifference);
        this.DestroyMob = mob;
    }

    @Override
    public boolean canStart() {
        if (!this.DestroyMob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        if (this.findTargetPos()) {
            this.cooldown = StepAndDestroyBlockGoal.toGoalTicks(20);
            return true;
        }
        this.cooldown = this.getInterval(this.mob);
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.DestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        this.counter = 0;
    }


    @Override
    public void tick() {
        super.tick();
        World world = this.DestroyMob.getWorld();
        BlockPos blockPos = this.DestroyMob.getBlockPos();
        BlockPos blockPos2 = this.tweakToProperPos(blockPos, world);
            if (this.hasReached() && blockPos2 != null) {
            Vec3d vec3d;
            if (this.counter > 0) {
                vec3d = this.DestroyMob.getVelocity();
                this.DestroyMob.setVelocity(vec3d.x, 0.3, vec3d.z);
            }
            if (this.counter % 2 == 0) {
                vec3d = this.DestroyMob.getVelocity();
                this.DestroyMob.setVelocity(vec3d.x, -0.3, vec3d.z);
            }
            if (this.counter > 60) {
                world.removeBlock(blockPos2, false);
                if (!world.isClient) {
                    this.onDestroyBlock(world, blockPos2);
                }
            }
            ++this.counter;
        }
    }

    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        BlockPos[] blockPoss;
        if (world.getBlockState(pos).isIn(this.BedGroup)) {
            return pos;
        }
        for (BlockPos blockPos : blockPoss = new BlockPos[]{pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down()}) {
            if (!world.getBlockState(blockPos).isIn(this.BedGroup)) continue;
            return blockPos;
        }
        return null;
    }

    public void onDestroyBlock(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
    }
    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            return chunk.getBlockState(pos).isIn(this.BedGroup) && chunk.getBlockState(pos.up()).isAir() && chunk.getBlockState(pos.up(2)).isAir();
        }
        return false;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 1.14;
    }
}