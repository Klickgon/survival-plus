package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.entity.custom.ReeperEntity;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.world.baseassaults.BaseAssault;


public class BaseAssaultGoal extends MoveToTargetPosGoal {

    private final BaseAssault baseAssault;
    private TagKey<Block> blockTag = null;
    private BlockPos facingBlock;
    private int destroyBlockCooldown;
    private int destroyBlockCooldownCounter;


    public BaseAssaultGoal(HostileEntity mob, double speed) {
        super(mob, speed, 64, 12);
        this.baseAssault = ((IHostileEntityChanger) this.mob).getBaseAssault();
        this.cooldown = 0;
        if(mob.getClass() == MinerZombieEntity.class){
            this.blockTag = MinerZombieEntity.BLOCKTAG;
            destroyBlockCooldown = MinerZombieEntity.defaultCooldown;
        }
        if(mob.getClass() == LumberjackZombieEntity.class){
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
            destroyBlockCooldown = LumberjackZombieEntity.defaultCooldown;
        }
        if(mob.getClass() == DiggingZombieEntity.class){
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
            destroyBlockCooldown = DiggingZombieEntity.defaultCooldown;
        }
        destroyBlockCooldownCounter = destroyBlockCooldown;
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        if(this.baseAssault == null)
            return false;
        if (this.baseAssault.findPlayerInsteadOfBed && this.baseAssault.attachedPlayer.getBlockPos() != null) {
            this.cooldown = 30 + this.mob.getWorld().random.nextInt(15);
            BlockPos pos = tweakToProperPos(this.baseAssault.attachedPlayer.getBlockPos(), this.mob.getWorld());
            if(pos != null){
                this.targetPos = pos;
                return true;
            }
        }
        if (this.mob.getWorld().getBlockState(this.baseAssault.getCenter()).isIn(BlockTags.BEDS)) {
            this.cooldown = 50 + this.mob.getWorld().random.nextInt(15);
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
        if(((IHostileEntityChanger) this.mob).getBaseAssault() == null) ((IHostileEntityChanger) this.mob).getGoalSelector().remove(this);
        if (this.cooldown < 0) {
            if (this.baseAssault.findPlayerInsteadOfBed && this.baseAssault.attachedPlayer.getBlockPos() != null) {
                this.targetPos = tweakToProperPos(this.baseAssault.attachedPlayer.getBlockPos(), this.mob.getWorld());
                this.cooldown = 30 + this.mob.getWorld().random.nextInt(15);
            } else if (this.baseAssault.getCenter() != null) {
                this.targetPos = tweakToProperPos(baseAssault.getCenter(), this.mob.getWorld());
                this.cooldown = 50 + this.mob.getWorld().random.nextInt(15);
            } else stop();
        } else this.cooldown--;

        if(!this.baseAssault.findPlayerInsteadOfBed && this.mob.getBlockPos().isWithinDistance(targetPos, 1.5)) {
            BlockPos bedPos = tweakToProperBedPos(baseAssault.getCenter(), this.mob.getWorld());
            if (bedPos != null && mob.getWorld().getBlockState(bedPos).isIn(BlockTags.BEDS)) {
                if (mob instanceof CreeperEntity && !(mob instanceof ReeperEntity)) ((CreeperEntity) mob).ignite();
                else if (mob instanceof ReeperEntity) ((ReeperEntity) mob).forceExplosion = true;
                else mob.getWorld().breakBlock(bedPos, false);
                stop();
            }
        }
        if(this.blockTag != null && this.destroyBlockCooldownCounter <= 0 && this.mob.getNavigation().getCurrentPath() != null){
            World world = this.mob.getWorld();

            BlockPos currentPos = this.mob.getBlockPos();

            int DiffY = calcDiffY(); // Positive: Target is higher, Negative: Zombie is Higher

            Direction direction = Direction.fromRotation(this.mob.getBodyYaw());

            if (direction == Direction.SOUTH)       this.facingBlock = currentPos.up().south();
            else if (direction == Direction.WEST)   this.facingBlock = currentPos.up().west();
            else if (direction == Direction.NORTH)  this.facingBlock = currentPos.up().north();
            else if (direction == Direction.EAST)   this.facingBlock = currentPos.up().east();

                if(DiffY == 0 && (!world.getBlockState(this.facingBlock).isReplaceable() || !world.getBlockState(this.facingBlock.down()).isReplaceable())) {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock, true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    } else if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock.down(), true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                }

                if(DiffY < 0) {
                    if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock.down(), true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if(world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blockTag)){
                        world.breakBlock(this.facingBlock.down(2), true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock, true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }

                }

                if(DiffY > 0) {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        world.breakBlock(this.facingBlock, true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blockTag) && world.getBlockState(this.mob.getBlockPos().up()).isIn(BlockTags.REPLACEABLE)) {
                        world.breakBlock(this.mob.getBlockPos().up(2), true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if(world.getBlockState(this.facingBlock).isReplaceable() && world.getBlockState(this.facingBlock.up()).isIn(blockTag)){
                        world.breakBlock(this.facingBlock.up(), true);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                }

            }
            else this.destroyBlockCooldownCounter--;
        super.tick();
    }

    private int calcDiffY(){ // Calculates the height difference between the current and the next pathnode of the mob
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path.getCurrentNodeIndex() + 1 < path.getLength()){
            int currentnodeposY = path.getCurrentNodePos().getY();
            int nextnodeposY = path.getNodePos(path.getCurrentNodeIndex() + 1).getY();

            return nextnodeposY - currentnodeposY;
        }
        else return 0;
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