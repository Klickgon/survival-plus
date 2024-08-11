/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package survivalplus.modid.entity.ai.movecontrols;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import survivalplus.modid.entity.ai.pathing.pathmaker.DestrZombPathNodeMaker;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;

public class DestroyerZombieMoveControl
extends MoveControl {

    TagKey<Block> blockTag;

    public DestroyerZombieMoveControl(MobEntity entity) {
        super(entity);
        if(entity instanceof DiggingZombieEntity) this.blockTag = DiggingZombieEntity.BLOCKTAG;
        else if(entity instanceof LumberjackZombieEntity) this.blockTag = LumberjackZombieEntity.BLOCKTAG;
        else if(entity instanceof MinerZombieEntity) this.blockTag = MinerZombieEntity.BLOCKTAG;
    }

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
            BlockPos facingPos = null;
            BlockState blockState = this.entity.getWorld().getBlockState(blockPos);

            switch (Direction.fromRotation(this.entity.getBodyYaw())){
                case SOUTH -> facingPos = blockPos.up().south();
                case WEST -> facingPos = blockPos.up().west();
                case NORTH -> facingPos = blockPos.up().north();
                case EAST -> facingPos = blockPos.up().east();
            }
            World world = this.entity.getWorld();
            VoxelShape voxelShape = blockState.getCollisionShape(this.entity.getWorld(), blockPos);
            if ((facingPos == null || (!world.getBlockState(facingPos.up()).isIn(blockTag) && !world.getBlockState(facingPos).isIn(blockTag))) && (o > (double)this.entity.getStepHeight() && d * d + e * e < (double)Math.max(1.0f, this.entity.getWidth()) || !voxelShape.isEmpty() && this.entity.getY() < voxelShape.getMax(Direction.Axis.Y) + (double)blockPos.getY() && !blockState.isIn(BlockTags.DOORS) && !blockState.isIn(BlockTags.FENCES))) {
                this.entity.getJumpControl().setActive();
                this.state = State.JUMPING;
            }
        } else if (this.state == State.JUMPING) {
            this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            if (this.entity.isOnGround()) {
                this.state = State.WAIT;
            }
        } else {
            this.entity.setForwardSpeed(0.0f);
        }
    }

    private boolean isPosWalkable(float x, float z) {
        PathNodeMaker pathNodeMaker;
        EntityNavigation entityNavigation = this.entity.getNavigation();
        return entityNavigation == null || (pathNodeMaker = entityNavigation.getNodeMaker()) == null || pathNodeMaker.getDefaultNodeType(((DestrZombPathNodeMaker)entityNavigation.getNodeMaker()).getPathContext(), MathHelper.floor(this.entity.getX() + (double)x), this.entity.getBlockY(), MathHelper.floor(this.entity.getZ() + (double)z)) == PathNodeType.WALKABLE;
    }
}

