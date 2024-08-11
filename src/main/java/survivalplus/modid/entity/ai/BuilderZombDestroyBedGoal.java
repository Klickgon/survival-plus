package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
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

import java.util.List;

public class BuilderZombDestroyBedGoal extends MoveToTargetPosGoal {

    private final BuilderZombieEntity DestroyMob;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;

    public BuilderZombDestroyBedGoal(BuilderZombieEntity mob, double speed, int maxYDifference) {
        super(mob, speed, 32, maxYDifference);
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
        this.cooldown = 20 + this.mob.getWorld().random.nextInt(10);
        return this.findTargetPos();
    }

    @Override
    public void stop() {
        super.stop();
        this.DestroyMob.targetBedPos = null;
        this.DestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        this.DestroyMob.targetBedPos = this.targetPos;
    }

    @Override
    public boolean shouldContinue() {
        return this.tryingTime <= 1200 && !this.isTargetPos(this.mob.getWorld(), this.targetPos);
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.DestroyMob.getWorld();
        BlockPos blockPos = this.DestroyMob.getBlockPos();
        BlockPos blockPos2 = this.tweakToProperPos(blockPos, world);
        if (blockPos2 != null && blockPos2.isWithinDistance(blockPos, 3)) {
            world.breakBlock(blockPos2, true);
            this.stop();
        }
    }


    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        if (world.getBlockState(pos).isIn(this.BedGroup)) {
            return pos;
        }
        BlockPos[] blockPoss = new BlockPos[]{pos.west(), pos.east(), pos.north(), pos.south(), pos.up()};
        for (BlockPos blockPos : blockPoss) {
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
    protected boolean findTargetPos() {
        List<ServerPlayerEntity> list = this.mob.getServer().getPlayerManager().getPlayerList();
        BlockPos temptargetpos = null;
        BlockPos mobpos = this.mob.getBlockPos();
        boolean bl = false;
        for(ServerPlayerEntity player : list){
            if(player.isCreative() || player.isSpectator()) continue;
            BlockPos spawnpos = player.getSpawnPointPosition();
            if(spawnpos != null && spawnpos.isWithinDistance(mobpos, 32) && this.mob.getWorld().getBlockState(spawnpos).isIn(BlockTags.BEDS)){
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
    public double getDesiredDistanceToTarget() {
        return 1.14;
    }
}