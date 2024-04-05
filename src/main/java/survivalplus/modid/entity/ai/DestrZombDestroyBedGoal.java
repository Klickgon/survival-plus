package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;

public class DestrZombDestroyBedGoal extends MoveToTargetPosGoal {

    private final HostileEntity DestroyMob;
    private int counter;
    private final TagKey<Block> BedGroup = BlockTags.BEDS;

    private static final int destroyBlockCooldown = 10;

    private final TagKey<Block> blocktag;
    private BlockPos facingBlock;

    private int destroyBlockCooldownCounter = 10;

    public DestrZombDestroyBedGoal(HostileEntity mob, double speed, int maxYDifference, TagKey<Block> blocktag) {
        super(mob, speed, 64, maxYDifference);
        this.DestroyMob = mob;
        this.cooldown = 0;
        this.blocktag = blocktag;
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
        this.DestroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        if(this.DestroyMob.getClass() == MinerZombieEntity.class) ((MinerZombieEntity) this.DestroyMob).targetBedPos = this.targetPos;
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

        if(this.destroyBlockCooldownCounter <= 0){
                float rotation = this.DestroyMob.getHeadYaw();
                BlockPos currentPos = this.DestroyMob.getBlockPos();
                int cposX = currentPos.getX();
                int cposY = currentPos.getY();
                int cposZ = currentPos.getZ();
                int targetposY = this.targetPos.getY();
                int DiffY = targetposY - cposY; // Positive: Target is higher, Negative: Zombie is Higher
                if (rotation > -135 && rotation <= -45)     this.facingBlock = new BlockPos(cposX + 1, cposY + 1, cposZ);
                else if(rotation > 45 && rotation <= 135)   this.facingBlock = new BlockPos(cposX - 1, cposY + 1, cposZ);
                else if(rotation > 135 || rotation <= -135) this.facingBlock = new BlockPos(cposX, cposY + 1, cposZ - 1);
                else if (rotation > -45 || rotation <= 45)  this.facingBlock = new BlockPos(cposX, cposY + 1, cposZ + 1);

            if(DiffY <= 0 && DiffY > -2) {
                if (world.getBlockState(this.facingBlock).isIn(blocktag)) {
                    world.breakBlock(this.facingBlock, false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                } else if (world.getBlockState(this.facingBlock.down()).isIn(blocktag)) {
                    world.breakBlock(this.facingBlock.down(), false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                }
            }

            if(DiffY < -2) {
                if (world.getBlockState(this.facingBlock.down()).isIn(blocktag)) {
                    world.breakBlock(this.facingBlock.down(), false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                }
                else if(world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blocktag)){
                    world.breakBlock(this.facingBlock.down(2), false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                }
                else if (world.getBlockState(this.facingBlock).isIn(blocktag)) {
                    world.breakBlock(this.facingBlock, false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                }

            }

            if(DiffY > 0) {
                if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blocktag)) {
                    world.breakBlock(this.mob.getBlockPos().up(2), false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                }
                else if (world.getBlockState(this.facingBlock).isIn(blocktag)) {
                    world.breakBlock(this.facingBlock, false);
                    this.destroyBlockCooldownCounter = destroyBlockCooldown;
                }
                else if(world.getBlockState(this.facingBlock).isReplaceable()){
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