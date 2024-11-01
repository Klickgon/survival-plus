package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
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
import survivalplus.modid.util.IServerPlayerChanger;

import java.util.List;

public class DestroyBedGoal extends MoveToTargetPosGoal {

    private final HostileEntity DestroyMob;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;


    public DestroyBedGoal(HostileEntity mob, double speed, int maxYDifference) {
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
        MinecraftServer server = this.DestroyMob.getServer();
        if (server == null || !server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        this.cooldown = 20 + this.mob.getWorld().random.nextInt(10);
        return this.findTargetPos();
    }

    @Override
    public void stop() {
        super.stop();
        this.DestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
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
        if (blockPos2 != null && blockPos2.isWithinDistance(blockPos, 2)) {
            world.breakBlock(blockPos2, true);
            this.stop();
        }
    }

    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        if (world.getBlockState(pos).isIn(this.BedGroup)) {
            return pos;
        }
        BlockPos[] blockPoss = new BlockPos[]{pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down()};
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
            return chunk.getBlockState(pos).isIn(this.BedGroup) && chunk.getBlockState(pos.up()).isSolidBlock(chunk, pos.up()) && chunk.getBlockState(pos.up(2)).isSolidBlock(chunk, pos.up(2));
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
            BlockPos spawnpos = ((IServerPlayerChanger)player).getMainSpawnPoint();
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