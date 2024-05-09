package survivalplus.modid.entity.ai;


import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import survivalplus.modid.entity.ai.pathing.ReeperNavigation;
import survivalplus.modid.entity.custom.ReeperEntity;

public class ReeperDestroyBedGoal extends MoveToTargetPosGoal {
    private final TagKey<Block> BedGroup = BlockTags.BEDS;
    private final ReeperEntity reeper;



    public ReeperDestroyBedGoal(ReeperEntity reeper, double speed, int maxYDifference){
        super(reeper, speed, 16, maxYDifference);
        this.reeper = reeper;
        this.cooldown = 0;
    }

    public boolean canStart() {
        if (this.cooldown > 0){
            --this.cooldown;
            return false;
        }
        if (!this.reeper.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        if (this.findTargetPos()) {
            this.cooldown = 200;
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.reeper.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        this.reeper.targetBedPos = this.targetPos;
    }


    @Override
    public double getDesiredDistanceToTarget() {
        return 2.0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (targetPos.isWithinDistance(this.reeper.getBlockPos(), 3.0)) this.reeper.ignite();
    }

    @Override
    protected void startMovingToTarget() {
        EntityNavigation nav = new ReeperNavigation(this.reeper, this.reeper.getWorld());
        nav.startMovingTo((double)this.targetPos.getX() + 0.5, this.targetPos.getY() + 1, (double)this.targetPos.getZ() + 0.5, this.speed);
    }


    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            return chunk.getBlockState(pos).isIn(this.BedGroup);
        }
        return false;
    }

}

