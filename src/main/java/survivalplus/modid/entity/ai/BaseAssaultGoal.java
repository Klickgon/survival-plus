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
import net.minecraft.world.GameRules;
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
        this.cooldown = mob.getRandom().nextInt(40);
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
        BlockPos center = this.baseAssault.getCenter();
        if (this.mob.getWorld().getBlockState(center).isIn(BlockTags.BEDS)) {
            this.cooldown = 50 + this.mob.getWorld().random.nextInt(15);
            this.targetPos = center;
            return true;

        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.fallDistance = 1.0f;
    }

    @Override
    public boolean shouldContinue() {
        return this.tryingTime <= 1200;
    }

    @Override
    public void start() {
        super.start();
    }


    @Override
    public void tick() {
        if(this.baseAssault == null || this.baseAssault.isFinished()){
            stop();
            ((IHostileEntityChanger) this.mob).getGoalSelector().remove(this);
            return;
        }
        if (this.cooldown < 0) {
            if (this.baseAssault.findPlayerInsteadOfBed && this.baseAssault.attachedPlayer.getBlockPos() != null) {
                this.targetPos = tweakToProperPos(this.baseAssault.attachedPlayer.getBlockPos(), this.mob.getWorld());
                this.cooldown = 30 + this.mob.getWorld().random.nextInt(15);
            } else {
                if (this.baseAssault.getCenter() != null) {
                    this.targetPos = this.baseAssault.getCenter();
                    this.cooldown = 50 + this.mob.getWorld().random.nextInt(15);
                }
            }
        }
        this.cooldown--;

        if(!this.baseAssault.findPlayerInsteadOfBed) {
            BlockPos bedPos = baseAssault.getCenter();
            if(this.mob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.mob.getBlockPos().isWithinDistance(bedPos, 1.5)){
                if (mob instanceof ReeperEntity) ((ReeperEntity) mob).hadTarget = true;
                else if (mob instanceof CreeperEntity) ((CreeperEntity) mob).ignite();
                else mob.getWorld().breakBlock(bedPos, true);
            }
        }
        if(this.mob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.blockTag != null && this.destroyBlockCooldownCounter <= 0 && this.mob.getNavigation().getCurrentPath() != null){
            World world = this.mob.getWorld();

            BlockPos currentPos = ((IHostileEntityChanger)this.mob).getElevatedBlockPos();

            int DiffY = calcDiffY(); // Positive: Target is higher, Negative: Zombie is Higher

            Direction direction = Direction.fromRotation(this.mob.getBodyYaw());

            switch (direction){
                case SOUTH -> this.facingBlock = currentPos.up().south();
                case WEST -> this.facingBlock = currentPos.up().west();
                case NORTH -> this.facingBlock = currentPos.up().north();
                case EAST -> this.facingBlock = currentPos.up().east();
                default -> this.facingBlock = null;
            }

            if(this.facingBlock != null && checkOnSameXandZ()) {
                if(DiffY == 0 && (!world.getBlockState(this.facingBlock).isReplaceable() || !world.getBlockState(this.facingBlock.down()).isReplaceable())) {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                        world.breakBlock(this.facingBlock, true);
                    } else if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down()).getHardness(world, this.facingBlock.down());
                        world.breakBlock(this.facingBlock.down(), true);
                    }
                }

                if(DiffY < 0) {
                    if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down()).getHardness(world, this.facingBlock.down());
                        world.breakBlock(this.facingBlock.down(), true);
                    } else if (world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down(2)).getHardness(world, this.facingBlock.down(2));
                        world.breakBlock(this.facingBlock.down(2), true);
                    } else if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                        world.breakBlock(this.facingBlock, true);
                    }

                }

                if(DiffY > 0) {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                        world.breakBlock(this.facingBlock, true);
                    } else if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blockTag) && world.getBlockState(this.mob.getBlockPos().up()).isIn(BlockTags.REPLACEABLE)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.mob.getBlockPos().up(2)).getHardness(world, this.mob.getBlockPos().up(2));
                        world.breakBlock(this.mob.getBlockPos().up(2), true);
                    } else if (world.getBlockState(this.facingBlock).isReplaceable() && world.getBlockState(this.facingBlock.up()).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.up()).getHardness(world, this.facingBlock.up());
                        world.breakBlock(this.facingBlock.up(), true);
                    }
                }
            }
        }
        this.destroyBlockCooldownCounter--;
        super.tick();
    }

    private int calcDiffY(){ // Calculates the height difference between the current and the next pathnode of the mob
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path == null || path.getCurrentNodeIndex() >= path.getLength()) return 0;
        if(path.getCurrentNodeIndex() > 0){
            int currentnodeposY = path.getCurrentNodePos().getY();
            int lastnodeposY = path.getNodePos(path.getCurrentNodeIndex() - 1).getY();

            return currentnodeposY - lastnodeposY;
        }
        else return 0;
    }

    private boolean checkOnSameXandZ(){ // Calculates if the current PathNode is on the same X and Y as the Facing block
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path == null) return false;
        if(path.getCurrentNodeIndex() > path.getLength() - 1) return true;
        BlockPos pathNodePos = path.getCurrentNodePos();
        return pathNodePos.getX() == this.facingBlock.getX() && pathNodePos.getZ() == this.facingBlock.getZ();
    }

    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        BlockPos[] blockPoss = new BlockPos[]{pos, pos.down(), pos.west(), pos.east(), pos.north(), pos.south(), pos.down().down(), pos.up()};
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