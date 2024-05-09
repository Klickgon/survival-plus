package survivalplus.modid.entity.ai.movecontrols;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

public class BuilderZombieMoveControl extends MoveControl {

    private int DirtJumpcooldown = 10;

    public BuilderZombieMoveControl(MobEntity entity) {
        super(entity);
    }

    private boolean isPosWalkable(float x, float z) {
        PathNodeMaker pathNodeMaker;
        EntityNavigation entityNavigation = this.entity.getNavigation();
        return entityNavigation == null || (pathNodeMaker = entityNavigation.getNodeMaker()) == null || pathNodeMaker.getDefaultNodeType(this.entity.getWorld(), MathHelper.floor(this.entity.getX() + (double)x), this.entity.getBlockY(), MathHelper.floor(this.entity.getZ() + (double)z)) == PathNodeType.WALKABLE;
    }

    @Override
    public void tick() {

        if (this.state == State.STRAFE) {
            float n;
            float f = (float)this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            float g = (float)this.speed * f;
            float h = this.forwardMovement;
            float i = this.sidewaysMovement;
            float j = MathHelper.sqrt(h * h + i * i);
            if (j < 1.0f) {
                j = 1.0f;
            }
            j = g / j;
            float k = MathHelper.sin(this.entity.getYaw() * ((float)Math.PI / 180));
            float l = MathHelper.cos(this.entity.getYaw() * ((float)Math.PI / 180));
            float m = (h *= j) * l - (i *= j) * k;
            if (!this.isPosWalkable(m, n = i * l + h * k)) {
                this.forwardMovement = 1.0f;
                this.sidewaysMovement = 0.0f;
            }
            this.entity.setMovementSpeed(g);
            this.entity.setForwardSpeed(this.forwardMovement);
            this.entity.setSidewaysSpeed(this.sidewaysMovement);
            this.state = State.WAIT;
        } else if (this.state == State.MOVE_TO) {
            this.state = State.WAIT;
            double d = this.targetX - this.entity.getX();
            double e = this.targetZ - this.entity.getZ();
            double o = this.targetY - this.entity.getY();
            double p = d * d + o * o + e * e;
            if (p < 2.500000277905201E-7) {
                this.entity.setForwardSpeed(0.0f);
                return;
            }
            float n = (float)(MathHelper.atan2(e, d) * 57.2957763671875) - 90.0f;
            this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), n, 90.0f));
            this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            BlockPos blockPos = this.entity.getBlockPos();
            BlockState blockState = this.entity.getWorld().getBlockState(blockPos);
            VoxelShape voxelShape = blockState.getCollisionShape(this.entity.getWorld(), blockPos);
            if (jumpRequirement(o, d, e, voxelShape, blockPos, blockState)) {
                this.entity.getJumpControl().setActive();
                this.state = State.JUMPING;
            }
        } else if (this.state == State.JUMPING) {
            this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            World world = this.entity.getWorld();
            BuilderZombieEntity bzomb = (BuilderZombieEntity) this.entity;
            IHostileEntityChanger bzomb2 = (IHostileEntityChanger) this.entity;
            BlockPos bzombpos = bzomb.getBlockPos();
            if(DirtJumpcooldown <= 0 && (bzomb.getTarget() != null || bzomb.hasTargetBed || bzomb2.getBaseAssault() != null)) {
                if(isDirtJumpRequired(bzombpos.down(), world)){
                    world.setBlockState(bzombpos.down(), Blocks.DIRT.getDefaultState());
                    world.playSound(null, bzombpos.down(), SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
                    DirtJumpcooldown = 20;
                }
            }
            else DirtJumpcooldown--;
            if (this.entity.isOnGround()) {
                this.state = State.WAIT;
            }
        } else {
            this.entity.setForwardSpeed(0.0f);
        }
    }

    private boolean isDirtJumpRequired(BlockPos pos, World world){
        boolean canPlaceBlock = world.getBlockState(pos).isIn(BlockTags.REPLACEABLE);

        boolean canJumpSafely = world.getBlockState(pos.up(2)).isAir();

        boolean mustPlaceBlockToProgress = world.getBlockState(pos.east()).isIn(BlockTags.REPLACEABLE) && world.getBlockState(pos.west()).isIn(BlockTags.REPLACEABLE)
                && world.getBlockState(pos.north()).isIn(BlockTags.REPLACEABLE) && world.getBlockState(pos.south()).isIn(BlockTags.REPLACEABLE);

        return canPlaceBlock && canJumpSafely && mustPlaceBlockToProgress;
    }

    private boolean jumpRequirement(double o, double d, double e, VoxelShape voxelShape, BlockPos blockPos, BlockState blockState){
        boolean bl1 = o > (double)this.entity.getStepHeight() && d * d + e * e < (double)Math.max(1.0f, this.entity.getWidth());
        boolean bl2 = this.entity.getY() < voxelShape.getMax(Direction.Axis.Y) + (double)blockPos.getY() && !blockState.isIn(BlockTags.DOORS);
        PathNodeMaker pnm = this.entity.getNavigation().getNodeMaker();
        int x = this.entity.getBlockX();
        int y = this.entity.getBlockY();
        int z = this.entity.getBlockZ();
        return bl1 || bl2 && !blockState.isIn(BlockTags.FENCES) || (pnm.getNode(x, y + 1, z).type == PathNodeType.WALKABLE);
    }




}
