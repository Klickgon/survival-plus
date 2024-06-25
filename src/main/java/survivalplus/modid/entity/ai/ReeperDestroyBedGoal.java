package survivalplus.modid.entity.ai;


import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import survivalplus.modid.entity.custom.ReeperEntity;

import java.util.List;

public class ReeperDestroyBedGoal extends MoveToTargetPosGoal {
    private final TagKey<Block> BedGroup = BlockTags.BEDS;
    private final ReeperEntity reeper;



    public ReeperDestroyBedGoal(ReeperEntity reeper, double speed, int maxYDifference){
        super(reeper, speed, 16, maxYDifference);
        this.reeper = reeper;
        this.cooldown = 0;
    }

    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        if (!this.reeper.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        this.cooldown = 20 + this.mob.getWorld().random.nextInt(10);
        return this.findTargetPos();
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
    public boolean shouldContinue() {
        return this.tryingTime <= 1200 && !this.isTargetPos(this.mob.getWorld(), this.targetPos);
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
        EntityNavigation nav = new MobNavigation(this.reeper, this.reeper.getWorld());
        nav.startMovingTo((double)this.targetPos.getX() + 0.5, this.targetPos.getY() + 1, (double)this.targetPos.getZ() + 0.5, this.speed);
    }

    @Override
    protected boolean findTargetPos() {
        List<ServerPlayerEntity> list = this.mob.getServer().getPlayerManager().getPlayerList();
        BlockPos temptargetpos = null;
        BlockPos mobpos = this.mob.getBlockPos();
        boolean bl = false;
        for(ServerPlayerEntity player : list){
            BlockPos spawnpos = player.getSpawnPointPosition();
            if(spawnpos != null && spawnpos.isWithinDistance(mobpos, 16) && this.mob.getWorld().getBlockState(spawnpos).isIn(BlockTags.BEDS)){
                bl = true;
                if(temptargetpos == null || spawnpos.getSquaredDistance(mobpos) < temptargetpos.getSquaredDistance(mobpos)){
                    temptargetpos = spawnpos;
                }
            }
        }
        if (bl) this.targetPos = temptargetpos;
        return bl;
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

