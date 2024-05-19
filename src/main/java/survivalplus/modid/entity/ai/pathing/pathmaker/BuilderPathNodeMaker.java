package survivalplus.modid.entity.ai.pathing.pathmaker;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

public class BuilderPathNodeMaker extends LandPathNodeMaker {

    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<Box>();


    private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = type;
        pathNode.penalty = Math.max(pathNode.penalty, penalty);
        return pathNode;
    }

    private boolean hasTargetBedPos(BuilderZombieEntity mob){
            return mob.targetBedPos != null;
    }

    @Override
    @Nullable
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        if (this.entity.getTarget() != null || hasTargetBedPos((BuilderZombieEntity) this.entity) || ((IHostileEntityChanger)this.entity).getBaseAssault() != null) {
            BlockPos pos = new BlockPos(x, y, z);
            if (this.entity.getWorld().getBlockState(pos).isIn(BlockTags.REPLACEABLE) && !this.entity.getWorld().getBlockState(pos.down()).isIn(BlockTags.REPLACEABLE)) {
                return this.getNodeWith(x, y, z, PathNodeType.WALKABLE, 8.0f);
            }
        }
        return super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
    }



    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        if (this.entity.getTarget() != null || hasTargetBedPos((BuilderZombieEntity) this.entity)) {
            PathNode pathNode26;
            PathNode pathNode25;
            PathNode pathNode24;
            PathNode pathNode23;
            PathNode pathNode22;
            PathNode pathNode21;
            PathNode pathNode20;
            PathNode pathNode19;
            PathNode pathNode18;
            PathNode pathNode17;
            PathNode pathNode16;
            PathNode pathNode15;
            PathNode pathNode14;
            PathNode pathNode13;
            PathNode pathNode12;
            PathNode pathNode11;
            PathNode pathNode10;
            PathNode pathNode9;
            PathNode pathNode8;
            PathNode pathNode7;
            PathNode pathNode4;
            PathNode pathNode3;
            PathNode pathNode2;
            int i = 0;
            PathNode pathNode = this.getPassableNode(node.x, node.y, node.z + 1);
            if (this.unvisited(pathNode)) {
                successors[i++] = pathNode;
            }
            if (this.unvisited(pathNode2 = this.getPassableNode(node.x - 1, node.y, node.z))) {
                successors[i++] = pathNode2;
            }
            if (this.unvisited(pathNode3 = this.getPassableNode(node.x + 1, node.y, node.z))) {
                successors[i++] = pathNode3;
            }
            if (this.unvisited(pathNode4 = this.getPassableNode(node.x, node.y, node.z - 1))) {
                successors[i++] = pathNode4;
            }
            if (this.unvisited(pathNode7 = this.getPassableNode(node.x, node.y + 1, node.z + 1)) && this.isPassable(pathNode)) {
                successors[i++] = pathNode7;
            }
            if (this.unvisited(pathNode8 = this.getPassableNode(node.x - 1, node.y + 1, node.z)) && this.isPassable(pathNode2)) {
                successors[i++] = pathNode8;
            }
            if (this.unvisited(pathNode9 = this.getPassableNode(node.x + 1, node.y + 1, node.z)) && this.isPassable(pathNode3)) {
                successors[i++] = pathNode9;
            }
            if (this.unvisited(pathNode10 = this.getPassableNode(node.x, node.y + 1, node.z - 1)) && this.isPassable(pathNode4)) {
                successors[i++] = pathNode10;
            }
            if (this.unvisited(pathNode11 = this.getPassableNode(node.x, node.y - 1, node.z + 1)) && this.isPassable(pathNode)) {
                successors[i++] = pathNode11;
            }
            if (this.unvisited(pathNode12 = this.getPassableNode(node.x - 1, node.y - 1, node.z)) && this.isPassable(pathNode2)) {
                successors[i++] = pathNode12;
            }
            if (this.unvisited(pathNode13 = this.getPassableNode(node.x + 1, node.y - 1, node.z)) && this.isPassable(pathNode3)) {
                successors[i++] = pathNode13;
            }
            if (this.unvisited(pathNode14 = this.getPassableNode(node.x, node.y - 1, node.z - 1)) && this.isPassable(pathNode4)) {
                successors[i++] = pathNode14;
            }
            if (this.unvisited(pathNode15 = this.getPassableNode(node.x + 1, node.y, node.z - 1)) && this.isPassable(pathNode4) && this.isPassable(pathNode3)) {
                successors[i++] = pathNode15;
            }
            if (this.unvisited(pathNode16 = this.getPassableNode(node.x + 1, node.y, node.z + 1)) && this.isPassable(pathNode) && this.isPassable(pathNode3)) {
                successors[i++] = pathNode16;
            }
            if (this.unvisited(pathNode17 = this.getPassableNode(node.x - 1, node.y, node.z - 1)) && this.isPassable(pathNode4) && this.isPassable(pathNode2)) {
                successors[i++] = pathNode17;
            }
            if (this.unvisited(pathNode18 = this.getPassableNode(node.x - 1, node.y, node.z + 1)) && this.isPassable(pathNode) && this.isPassable(pathNode2)) {
                successors[i++] = pathNode18;
            }
            if (this.unvisited(pathNode19 = this.getPassableNode(node.x + 1, node.y + 1, node.z - 1)) && this.isPassable(pathNode15) && this.isPassable(pathNode4) && this.isPassable(pathNode3) && this.isPassable(pathNode10) && this.isPassable(pathNode9)) {
                successors[i++] = pathNode19;
            }
            if (this.unvisited(pathNode20 = this.getPassableNode(node.x + 1, node.y + 1, node.z + 1)) && this.isPassable(pathNode16) && this.isPassable(pathNode) && this.isPassable(pathNode3) && this.isPassable(pathNode7) && this.isPassable(pathNode9)) {
                successors[i++] = pathNode20;
            }
            if (this.unvisited(pathNode21 = this.getPassableNode(node.x - 1, node.y + 1, node.z - 1)) && this.isPassable(pathNode17) && this.isPassable(pathNode4) && this.isPassable(pathNode2) && this.isPassable(pathNode10) && this.isPassable(pathNode8)) {
                successors[i++] = pathNode21;
            }
            if (this.unvisited(pathNode22 = this.getPassableNode(node.x - 1, node.y + 1, node.z + 1)) && this.isPassable(pathNode18) && this.isPassable(pathNode) && this.isPassable(pathNode2) && this.isPassable(pathNode7) && this.isPassable(pathNode8)) {
                successors[i++] = pathNode22;
            }
            if (this.unvisited(pathNode23 = this.getPassableNode(node.x + 1, node.y - 1, node.z - 1)) && this.isPassable(pathNode15) && this.isPassable(pathNode4) && this.isPassable(pathNode3) && this.isPassable(pathNode14) && this.isPassable(pathNode13)) {
                successors[i++] = pathNode23;
            }
            if (this.unvisited(pathNode24 = this.getPassableNode(node.x + 1, node.y - 1, node.z + 1)) && this.isPassable(pathNode16) && this.isPassable(pathNode) && this.isPassable(pathNode3) && this.isPassable(pathNode11) && this.isPassable(pathNode13)) {
                successors[i++] = pathNode24;
            }
            if (this.unvisited(pathNode25 = this.getPassableNode(node.x - 1, node.y - 1, node.z - 1)) && this.isPassable(pathNode17) && this.isPassable(pathNode4) && this.isPassable(pathNode2) && this.isPassable(pathNode14) && this.isPassable(pathNode12)) {
                successors[i++] = pathNode25;
            }
            if (this.unvisited(pathNode26 = this.getPassableNode(node.x - 1, node.y - 1, node.z + 1)) && this.isPassable(pathNode18) && this.isPassable(pathNode) && this.isPassable(pathNode2) && this.isPassable(pathNode11) && this.isPassable(pathNode12)) {
                successors[i++] = pathNode26;
            }
            return i;
        }
        else return super.getSuccessors(successors, node);
    }

    private boolean unvisited(@Nullable PathNode node) {
        return node != null && !node.visited;
    }

    private boolean isPassable(@Nullable PathNode node) {
        return node != null && node.penalty >= 0.0f;
    }

    @Nullable
    protected PathNode getPassableNode(int x, int y, int z) {
        PathNode pathNode = null;
        PathNodeType pathNodeType = this.getNodeType(this.entity, x, y, z);
        float f = this.entity.getPathfindingPenalty(pathNodeType);
        if (f >= 0.0f) {
            pathNode = this.getNode(x, y, z);
            pathNode.type = pathNodeType;
            pathNode.penalty = Math.max(pathNode.penalty, f);
        }
        return pathNode;
    }

}
