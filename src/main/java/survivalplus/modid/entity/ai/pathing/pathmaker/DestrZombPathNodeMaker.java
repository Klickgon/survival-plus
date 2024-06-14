package survivalplus.modid.entity.ai.pathing.pathmaker;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

public class DestrZombPathNodeMaker extends LandPathNodeMaker {

    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<>();

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

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = type;
        pathNode.penalty = Math.max(pathNode.penalty, penalty);
        return pathNode;
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
        if (this.entity.getTarget() != null || hasTargetBedPos(this.entity) || ((IHostileEntityChanger)this.entity).getBaseAssault() != null) {
            World world = this.entity.getWorld();
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).isIn(this.blockTag)){
                if(!world.getBlockState(pos.up()).isIn(this.blockTag) && !world.getBlockState(pos.up()).isReplaceable())
                    return getNodeWith(x, y, z, PathNodeType.BLOCKED, PathNodeType.BLOCKED.getDefaultPenalty());
                if(!world.getBlockState(pos.down()).isReplaceable())
                    return getNodeWith(x, y, z, PathNodeType.WALKABLE, 8.0f);
            }
            if(world.getBlockState(pos).isReplaceable() && world.getBlockState(pos.up()).isIn(this.blockTag))
                return getNodeWith(x, y, z, PathNodeType.WALKABLE, 8.0f);
        }
        return super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        if (this.entity.getTarget() != null || hasTargetBedPos(this.entity)) {
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
            PathNode pathNode6;
            PathNode pathNode5;
            PathNode pathNode4;
            PathNode pathNode3;
            PathNode pathNode2;
            double d;
            PathNode pathNode;
            int i = 0;
            int j = 0;
            PathNodeType pathNodeType1 = this.getNodeType(this.entity, node.x, node.y, node.z);
            PathNodeType pathNodeType2 = this.getNodeType(this.entity, node.x, node.y + 1, node.z);
            PathNodeType pathNodeType3 = this.getNodeType(this.entity, node.x, node.y + 2, node.z);
            if (this.entity.getPathfindingPenalty(pathNodeType2) >= 0.0f && pathNodeType1 != PathNodeType.STICKY_HONEY) {
                j = MathHelper.floor(Math.max(1.0f, this.entity.getStepHeight()));
            }
            d = this.getFeetY(new BlockPos(node.x, node.y, node.z));

            if (this.isValidAdjacentSuccessorUp(pathNode10 = this.getPathNode(node.x - 1, node.y + 1, node.z, j, d, Direction.WEST, pathNodeType1), node)) {
                successors[i++] = pathNode10;
            }
            if (this.isValidAdjacentSuccessorUp(pathNode11 = this.getPathNode(node.x + 1, node.y + 1, node.z, j, d, Direction.EAST, pathNodeType1), node)) {
                successors[i++] = pathNode11;
            }
            if (this.isValidAdjacentSuccessorUp(pathNode12 = this.getPathNode(node.x, node.y + 1, node.z - 1, j, d, Direction.NORTH, pathNodeType1), node)) {
                successors[i++] = pathNode12;
            }
            if (this.isValidAdjacentSuccessorUp(pathNode13 = this.getPathNode(node.x, node.y + 1, node.z + 1, j, d, Direction.SOUTH, pathNodeType1), node)) {
                successors[i++] = pathNode13;
            }
            if (this.isValidAdjacentSuccessor(pathNode = this.getPathNode(node.x, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType1), node)) {
                successors[i++] = pathNode;
            }
            if (this.isValidAdjacentSuccessor(pathNode2 = this.getPathNode(node.x - 1, node.y, node.z, j, d, Direction.WEST, pathNodeType1), node)) {
                successors[i++] = pathNode2;
            }
            if (this.isValidAdjacentSuccessor(pathNode3 = this.getPathNode(node.x + 1, node.y, node.z, j, d, Direction.EAST, pathNodeType1), node)) {
                successors[i++] = pathNode3;
            }
            if (this.isValidAdjacentSuccessor(pathNode4 = this.getPathNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType1), node)) {
                successors[i++] = pathNode4;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode2, pathNode4, pathNode5 = this.getPathNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType1))) {
                successors[i++] = pathNode5;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode3, pathNode4, pathNode6 = this.getPathNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType1))) {
                successors[i++] = pathNode6;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode2, pathNode, pathNode7 = this.getPathNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType1))) {
                successors[i++] = pathNode7;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode3, pathNode, pathNode8 = this.getPathNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType1))) {
                successors[i++] = pathNode8;
            }
            if (this.isValidAdjacentSuccessor(pathNode9 = this.getPathNode(node.x, node.y - 1, node.z + 1, j, d, Direction.SOUTH, pathNodeType1), node)) {
                successors[i++] = pathNode9;
            }
            if (this.isValidAdjacentSuccessor(pathNode14 = this.getPathNode(node.x - 1, node.y - 1, node.z, j, d, Direction.WEST, pathNodeType1), node)) {
                successors[i++] = pathNode14;
            }
            if (this.isValidAdjacentSuccessor(pathNode15 = this.getPathNode(node.x + 1, node.y - 1, node.z, j, d, Direction.EAST, pathNodeType1), node)) {
                successors[i++] = pathNode15;
            }
            if (this.isValidAdjacentSuccessor(pathNode16 = this.getPathNode(node.x, node.y - 1, node.z - 1, j, d, Direction.NORTH, pathNodeType1), node)) {
                successors[i++] = pathNode16;
            }
            return i;
        }
        else return super.getSuccessors(successors, node);
    }

    protected boolean isValidAdjacentSuccessorUp(@Nullable PathNode node, PathNode successor1) {
        return node != null && !node.visited && (node.penalty >= 0.0f || successor1.penalty < 0.0f) && this.getNodeType(this.entity, node.x, node.y + 1, node.z).getDefaultPenalty() >= 0.0f;
    }
}
