package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.world.BaseAssaults.BaseAssault;


public class BaseAssaultGoal extends MoveToTargetPosGoal {

    private final BaseAssault baseAssault;
    private TagKey<Block> blockTag = null;
    private BlockPos facingBlock;
    private int destroyBlockCooldownCounter;
    private static final int destroyBlockCooldown = 20;

    public BaseAssaultGoal(HostileEntity mob, double speed) {
        super(mob, speed, 64, 12);
        this.baseAssault = ((IHostileEntityChanger) this.mob).getBaseAssault();
        this.cooldown = 100;
        if(mob.getClass() == MinerZombieEntity.class){
            this.blockTag = MinerZombieEntity.BLOCKTAG;
        }
        if(mob.getClass() == LumberjackZombieEntity.class){
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
        }
        if(mob.getClass() == DiggingZombieEntity.class){
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
        }
    }

    @Override
    public boolean canStart() {
        if (this.baseAssault.findPlayerInsteadOfBed && this.baseAssault.attachedPlayer.getBlockPos() != null) {
            this.targetPos = tweakToProperPos(this.baseAssault.attachedPlayer.getBlockPos(), this.mob.getWorld());
            return true;
        }
        if (this.mob.getWorld().getBlockState(this.baseAssault.getCenter()).isIn(BlockTags.BEDS)) {
            BlockPos pos = tweakToProperPos(baseAssault.getCenter(), this.mob.getWorld());
            if(pos != null){
                this.targetPos = pos;
                return true;
            }
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
    }


    @Override
    public void tick() {
            if(!this.mob.getBlockPos().isWithinDistance(targetPos, 3)) {
                if (this.baseAssault.findPlayerInsteadOfBed && this.baseAssault.attachedPlayer.getBlockPos() != null) {
                    this.targetPos = tweakToProperPos(this.baseAssault.attachedPlayer.getBlockPos(), this.mob.getWorld());
                }
                if (this.baseAssault.getCenter() != null) {
                    this.targetPos = tweakToProperPos(baseAssault.getCenter(), this.mob.getWorld());
                }
            }
            if(this.mob.getBlockPos().isWithinDistance(targetPos, 1.5)) {
                BlockPos bedPos = tweakToProperBedPos(baseAssault.getCenter(), this.mob.getWorld());
                if (bedPos != null && mob.getWorld().getBlockState(bedPos).isIn(BlockTags.BEDS))
                    mob.getWorld().breakBlock(bedPos, false);
            }

            if(this.blockTag != null && this.destroyBlockCooldownCounter <= 0){
                World world = this.mob.getWorld();
                float rawrotation = Math.abs(this.mob.getBodyYaw());
                float rotation = (float) (rawrotation - 360 * (Math.floor(rawrotation / 360)));

                BlockPos currentPos = this.mob.getBlockPos();
                int cposY = currentPos.getY();

                int targetposY = this.targetPos.getY();
                int DiffY = targetposY - cposY; // Positive: Target is higher, Negative: Zombie is Higher

                if (rotation > 315 || rotation <= 45)   this.facingBlock = currentPos.up().south();
                else if (rotation <= 135)               this.facingBlock = currentPos.up().west();
                else if (rotation <= 225)               this.facingBlock = currentPos.up().north();
                else if (rotation <= 315)               this.facingBlock = currentPos.up().east();

                if(DiffY == 0) {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock, false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    } else if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock.down(), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                }

                if(DiffY < 0) {
                    if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock.down(), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if(world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blockTag)){
                        world.breakBlock(this.facingBlock.down(2), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock, false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }

                }

                if(DiffY > 0) {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock, false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blockTag) && world.getBlockState(this.mob.getBlockPos().up()).isIn(BlockTags.REPLACEABLE)) {
                        world.breakBlock(this.mob.getBlockPos().up(2), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if(world.getBlockState(this.facingBlock).isReplaceable() && world.getBlockState(this.facingBlock.up()).isIn(blockTag)){
                        world.breakBlock(this.facingBlock.up(), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                }

            }
            else this.destroyBlockCooldownCounter--;
        super.tick();
    }

    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        BlockPos[] blockPoss = new BlockPos[]{pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down()};
        for (BlockPos blockPos : blockPoss) {
            if (!world.getBlockState(blockPos).isIn(BlockTags.REPLACEABLE)) continue;
            return blockPos;
        }
        return null;
    }


    @Nullable
    private BlockPos tweakToProperBedPos(BlockPos pos, BlockView world) {
        BlockPos[] blockPoss = new BlockPos[]{pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down()};
        for (BlockPos blockPos : blockPoss ) {
            if (!world.getBlockState(blockPos).isIn(BlockTags.BEDS)) continue;
            return blockPos;
        }
        return null;
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            BlockPos bedPos = tweakToProperBedPos(baseAssault.getCenter(), world);
            if(bedPos != null) {
                return chunk.getBlockState(pos.up()).isAir() && chunk.getBlockState(pos.up(2)).isAir();
            }
        }
        return false;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 1.14;
    }
}