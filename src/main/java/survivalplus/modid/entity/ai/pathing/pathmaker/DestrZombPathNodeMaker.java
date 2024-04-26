package survivalplus.modid.entity.ai.pathing.pathmaker;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;

public class DestrZombPathNodeMaker extends LandPathNodeMaker {

    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<Box>();

    private TagKey<Block> blockTag;

    public DestrZombPathNodeMaker(Entity zomb){
        super();
        if(zomb.getClass() == MinerZombieEntity.class){
            this.blockTag = MinerZombieEntity.BLOCKTAG;
        }
        if(zomb.getClass() == LumberjackZombieEntity.class){
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
        }
        if(zomb.getClass() == DiggingZombieEntity.class){
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
        }
    }

    private double getStepHeight() {
        return Math.max(1.125, (double) this.entity.getStepHeight());
    }

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = type;
        pathNode.penalty = Math.max(pathNode.penalty, penalty);
        return pathNode;
    }

    private static boolean isBlocked(PathNodeType nodeType) {
        return nodeType == PathNodeType.FENCE || nodeType == PathNodeType.DOOR_WOOD_CLOSED || nodeType == PathNodeType.DOOR_IRON_CLOSED;
    }

    private boolean isBlocked(PathNode node) {
        Box box = this.entity.getBoundingBox();
        Vec3d vec3d = new Vec3d((double) node.x - this.entity.getX() + box.getLengthX() / 2.0, (double) node.y - this.entity.getY() + box.getLengthY() / 2.0, (double) node.z - this.entity.getZ() + box.getLengthZ() / 2.0);
        int i = MathHelper.ceil(vec3d.length() / box.getAverageSideLength());
        vec3d = vec3d.multiply(1.0f / (float) i);
        for (int j = 1; j <= i; ++j) {
            if (!this.checkBoxCollision(box = box.offset(vec3d))) continue;
            return false;
        }
        return false;
    }

    private PathNode getBlockedNode(int x, int y, int z) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = PathNodeType.BLOCKED;
        pathNode.penalty = -1.0f;
        return pathNode;
    }

    private boolean checkBoxCollision(Box box) {
        return this.collidedBoxes.computeIfAbsent(box, box2 -> !this.cachedWorld.isSpaceEmpty(this.entity, box));
    }

    private boolean hasTargetBedPos(MobEntity mob){
        if(mob.getClass() == MinerZombieEntity.class){
           return ((MinerZombieEntity) mob).targetBedPos != null;
        }
        if(mob.getClass() == LumberjackZombieEntity.class){
            return ((LumberjackZombieEntity) mob).targetBedPos != null;
        }
        if(mob.getClass() == DiggingZombieEntity.class){
            return ((DiggingZombieEntity) mob).targetBedPos != null;
        }
        else return false;
    }


    @Override
    @Nullable
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        if (this.entity.getTarget() != null || hasTargetBedPos(this.entity)) {
                World world = this.entity.getWorld();
                BlockPos pos = new BlockPos(x, y, z);
                if (world.getBlockState(pos).isIn(this.blockTag)){
                    if(!(world.getBlockState(pos.up()).isIn(BlockTags.REPLACEABLE) && world.getBlockState(pos.up(2)).isIn(BlockTags.REPLACEABLE))){
                        if(!world.getBlockState(pos.up()).isIn(BlockTags.REPLACEABLE) || !world.getBlockState(pos.up()).isIn(this.blockTag)){
                            return getNodeWith(x, y, z, PathNodeType.BLOCKED, PathNodeType.BLOCKED.getDefaultPenalty());
                        }
                        return getNodeWith(x, y, z, PathNodeType.WALKABLE, PathNodeType.WALKABLE.getDefaultPenalty());
                    }
                }
        }
        return super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
    }

}
