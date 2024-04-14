package survivalplus.modid.entity.ai.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import survivalplus.modid.entity.ai.pathing.pathmaker.BuilderPathNodeMaker;

public class BuilderZombieNavigation extends MobNavigation {

    private int recalcCooldown = 0;

    public BuilderZombieNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new BuilderPathNodeMaker();
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }



    @Override
    public void recalculatePath() {
        if (this.recalcCooldown <= 0) {
            LivingEntity target = this.entity.getTarget();
            if (target != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo((Entity) target, (int) this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
                this.recalcCooldown = 20;
            }
        }
        else this.recalcCooldown--;

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


    @Override
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
