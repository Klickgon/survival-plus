package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
    private int counter;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;

    private int DirtJumpCooldown = 10;
    private final int maxYDifference;
    private final int range;

    public BuilderZombDestroyBedGoal(BuilderZombieEntity mob, double speed, int maxYDifference) {
        super((HostileEntity)mob, speed, 16, maxYDifference);
        this.range = 32;
        this.maxYDifference = maxYDifference;
        this.DestroyMob = mob;
        this.cooldown = 80;
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        this.cooldown = 80;
        if (!this.DestroyMob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        if (this.findTargetPos()) {
            return true;
        }
        return false;
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
        this.counter = 0;
    }


    @Override
    public void tick() {
        super.tick();
        World world = this.DestroyMob.getWorld();
        BlockPos blockPos = this.DestroyMob.getBlockPos();

        BlockPos blockPos2 = this.tweakToProperPos(blockPos, world);
        if (blockPos2 != null && blockPos2.isWithinDistance(blockPos, 2)) {
            world.removeBlock(blockPos2, false);
            this.onDestroyBlock(world, blockPos2);
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


    @Override
    protected boolean findTargetPos() {
        int i = this.range;
        int j = this.maxYDifference;
        BlockPos blockPos = this.mob.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int k = this.lowestY;
        while (k <= j) {
            for (int l = 0; l < i; ++l) {
                int m = 0;
                while (m <= l) {
                    int n;
                    int n2 = n = m < l && m > -l ? l : 0;
                    while (n <= l) {
                        mutable.set(blockPos, m, k - 1, n);
                        if (this.isTargetPos(this.mob.getWorld(), mutable)) {
                            this.targetPos = mutable;
                            return true;
                        }
                        n = n > 0 ? -n : 1 - n;
                    }
                    m = m > 0 ? -m : 1 - m;
                }
            }
            k = k > 0 ? -k : 1 - k;
        }
        return false;
    }
}