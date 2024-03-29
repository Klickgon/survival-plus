package survivalplus.modid.entity.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class BuilderZombieNavigation extends MobNavigation {

    public BuilderZombieNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    protected double adjustTargetY(Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        return LandPathNodeMaker.getFeetY(this.world, blockPos);
    }

    @Override
    public void recalculatePath() {
        if (this.world.getTime() - this.lastRecalculateTime > 20L) {
            LivingEntity target = this.entity.getTarget();
            if (target != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo((Entity) target, (int) this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
                this.lastRecalculateTime = this.world.getTime();
                this.inRecalculationCooldown = false;
            }
        } else {
            this.inRecalculationCooldown = true;
        }
    }

    @Override
    public Path findPathTo(BlockPos target, int distance) {
        BlockPos blockPos;
        WorldChunk worldChunk = this.world.getChunkManager().getWorldChunk(ChunkSectionPos.getSectionCoord(target.getX()), ChunkSectionPos.getSectionCoord(target.getZ()));
        if (worldChunk == null) {
            return null;
        }
        if (worldChunk.getBlockState(target).isSolid()) {
            blockPos = target.up();
            while (blockPos.getY() < this.world.getTopY() && worldChunk.getBlockState(blockPos).isSolid()) {
                blockPos = blockPos.up();
            }
            return super.findPathTo(blockPos, distance);
        }
        return super.findPathTo(target, distance);
    }

    @Override
    public boolean isValidPosition(BlockPos pos) {
        return true;
    }

    protected boolean canWalkOnPath(PathNodeType pathType) {
        if (pathType == PathNodeType.WATER) {
            return false;
        }
        if (pathType == PathNodeType.LAVA) {
            return false;
        }
        return true;
    }

}
