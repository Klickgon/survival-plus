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
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;

public class DestrZombDestroyBedGoal extends MoveToTargetPosGoal {

    private final HostileEntity DestroyMob;
    private int counter;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;
    private static final int destroyBlockCooldown = 20;
    private TagKey<Block> blockTag;
    @Nullable
    private BlockPos facingBlock;
    private int destroyBlockCooldownCounter = 20;

    public DestrZombDestroyBedGoal(HostileEntity mob, double speed, int maxYDifference) {
        super(mob, speed, 16, maxYDifference);
        this.DestroyMob = mob;
        this.cooldown = 0;
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
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }

        if (!this.DestroyMob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
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
        if(this.DestroyMob.getClass() == MinerZombieEntity.class) ((MinerZombieEntity) this.DestroyMob).targetBedPos = null;
        if(this.DestroyMob.getClass() == LumberjackZombieEntity.class) ((LumberjackZombieEntity) this.DestroyMob).targetBedPos = null;
        if(this.DestroyMob.getClass() == DiggingZombieEntity.class) ((DiggingZombieEntity) this.DestroyMob).targetBedPos = null;
        this.DestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        if(this.DestroyMob.getClass() == MinerZombieEntity.class) ((MinerZombieEntity) this.DestroyMob).targetBedPos = this.targetPos;
        if(this.DestroyMob.getClass() == LumberjackZombieEntity.class) ((LumberjackZombieEntity) this.DestroyMob).targetBedPos = this.targetPos;
        if(this.DestroyMob.getClass() == DiggingZombieEntity.class) ((DiggingZombieEntity) this.DestroyMob).targetBedPos = this.targetPos;
        this.counter = 0;
    }


    @Override
    public void tick() {
        super.tick();
        World world = this.DestroyMob.getWorld();
        BlockPos blockPos = this.DestroyMob.getBlockPos();
        BlockPos blockPos2 = this.tweakToProperPos(blockPos, world);
        if (blockPos2 != null && blockPos2.isWithinDistance(blockPos, 3)) {
            world.removeBlock(blockPos2, false);
            this.onDestroyBlock(world, blockPos2);
        }

        if(this.destroyBlockCooldownCounter <= 0){
            float rawrotation = Math.abs(this.mob.getBodyYaw());
            float rotation = (float) (rawrotation - 360 * (Math.floor(rawrotation / 360)));

            BlockPos currentPos = this.DestroyMob.getBlockPos();
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

    public void onDestroyBlock(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
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