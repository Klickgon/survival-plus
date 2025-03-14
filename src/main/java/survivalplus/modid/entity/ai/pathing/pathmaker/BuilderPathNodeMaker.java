package survivalplus.modid.entity.ai.pathing.pathmaker;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.BuilderZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

public class BuilderPathNodeMaker extends LandPathNodeMaker {

    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<Box>();

    protected int verticalPathNodeCounter = 0;
    protected static final int VERTICAL_NODE_PENALTY = 16;

    protected PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = type;
        pathNode.penalty = Math.max(pathNode.penalty, penalty);
        return pathNode;
    }

    protected boolean hasTargetBedPos(BuilderZombieEntity mob){
            return mob.targetBedPos != null;
    }

    @Nullable
    protected PathNode getPathNodeForVertical(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        MinecraftServer server = this.entity.getServer();
        if (server != null && server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.entity.getStackInHand(Hand.MAIN_HAND).isOf(Items.DIRT) && (this.entity.getTarget() != null || hasTargetBedPos((BuilderZombieEntity) this.entity) || ((IHostileEntityChanger)this.entity).getBaseAssault() != null)) {
            BlockPos pos = new BlockPos(x, y, z);
            World world = this.entity.getWorld();
            if (world.getBlockState(pos).isReplaceable() && world.getBlockState(pos.up()).isReplaceable()) {
                if (world.getBlockState(pos.down()).isAir())
                    return this.getNodeWith(x, y, z, PathNodeType.BREACH, PathNodeType.BREACH.getDefaultPenalty());
            }
        }
        return null;
    }

    @Nullable
    protected PathNode getPathNodeForHorizontal(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        MinecraftServer server = this.entity.getServer();
        if (server != null && server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.entity.getMainHandStack().isOf(Items.DIRT) && (this.entity.getTarget() != null || hasTargetBedPos((BuilderZombieEntity) this.entity) || ((IHostileEntityChanger)this.entity).getBaseAssault() != null)) {
            BlockPos pos = new BlockPos(x, y, z);
            World world = this.entity.getWorld();
            if (world.getBlockState(pos).isReplaceable() && world.getBlockState(pos.up()).isReplaceable() ) {
                if (world.getBlockState(pos.down()).isAir())
                    return this.getNodeWith(x, y, z, PathNodeType.BREACH, PathNodeType.BREACH.getDefaultPenalty());
            }
        }
        return super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        MinecraftServer server = this.entity.getServer();
        if (server != null && server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.entity.getMainHandStack().isOf(Items.DIRT) && (this.entity.getTarget() != null || hasTargetBedPos((BuilderZombieEntity) this.entity) || ((IHostileEntityChanger)this.entity).getBaseAssault() != null)) {
            boolean usedVerticalNodeAsSuccessor = false;
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
            PathNode pathNode;
            double d;
            int i = 0;
            int j = 0;
            PathNodeType pathNodeType1 = this.getNodeType(this.context, node.x, node.y, node.z, this.entity);
            PathNodeType pathNodeType2 = this.getNodeType(this.context, node.x, node.y + 1, node.z, this.entity);
            if (this.entity.getPathfindingPenalty(pathNodeType2) >= 0.0f && pathNodeType1 != PathNodeType.STICKY_HONEY) {
                j = MathHelper.floor(Math.max(1.0f, this.entity.getStepHeight()));
            }
            d = this.getFeetY(new BlockPos(node.x, node.y, node.z));
            if (this.isValidAdjacentSuccessor(pathNode10 = this.getPathNodeForVertical(node.x - 1, node.y + 1, node.z, j, d, Direction.WEST, pathNodeType1), node) && canChooseUpwardNode(node.x - 1, node.y + 1, node.z) && this.getNodeType(this.context, node.x, node.y + 2, node.z, this.entity).getDefaultPenalty() >= 0.0f) {
                usedVerticalNodeAsSuccessor = true;
                successors[i++] = pathNode10;
            }
            if (this.isValidAdjacentSuccessor(pathNode11 = this.getPathNodeForVertical(node.x + 1, node.y + 1, node.z, j, d, Direction.EAST, pathNodeType1), node) && canChooseUpwardNode(node.x + 1, node.y + 1, node.z)  && this.getNodeType(this.context, node.x, node.y + 2, node.z, this.entity).getDefaultPenalty() >= 0.0f) {
                usedVerticalNodeAsSuccessor = true;
                successors[i++] = pathNode11;
            }
            if (this.isValidAdjacentSuccessor(pathNode12 = this.getPathNodeForVertical(node.x, node.y + 1, node.z - 1, j, d, Direction.NORTH, pathNodeType1), node) && canChooseUpwardNode(node.x, node.y + 1, node.z - 1)  && this.getNodeType(this.context, node.x, node.y + 2, node.z, this.entity).getDefaultPenalty() >= 0.0f) {
                usedVerticalNodeAsSuccessor = true;
                successors[i++] = pathNode12;
            }
            if (this.isValidAdjacentSuccessor(pathNode13 = this.getPathNodeForVertical(node.x, node.y + 1, node.z + 1, j, d, Direction.SOUTH, pathNodeType1), node) && canChooseUpwardNode(node.x, node.y + 1, node.z + 1)  && this.getNodeType(this.context, node.x, node.y + 2, node.z, this.entity).getDefaultPenalty() >= 0.0f) {
                usedVerticalNodeAsSuccessor = true;
                successors[i++] = pathNode13;
            }
            if (this.isValidAdjacentSuccessor(pathNode9 = this.getPathNodeForVertical(node.x, node.y - 1, node.z + 1, j, d, Direction.SOUTH, pathNodeType1), node) && this.getNodeType(this.context, node.x, node.y + 1, node.z + 1, this.entity).getDefaultPenalty() >= 0.0f) {
                successors[i++] = pathNode9;
            }
            if (this.isValidAdjacentSuccessor(pathNode14 = this.getPathNodeForVertical(node.x - 1, node.y - 1, node.z, j, d, Direction.WEST, pathNodeType1), node) && this.getNodeType(this.context, node.x - 1, node.y + 1, node.z, this.entity).getDefaultPenalty() >= 0.0f) {
                successors[i++] = pathNode14;
            }
            if (this.isValidAdjacentSuccessor(pathNode15 = this.getPathNodeForVertical(node.x + 1, node.y - 1, node.z, j, d, Direction.EAST, pathNodeType1), node) && this.getNodeType(this.context, node.x + 1, node.y + 1, node.z, this.entity).getDefaultPenalty() >= 0.0f) {
                successors[i++] = pathNode15;
            }
            if (this.isValidAdjacentSuccessor(pathNode16 = this.getPathNodeForVertical(node.x, node.y - 1, node.z - 1, j, d, Direction.NORTH, pathNodeType1), node) && this.getNodeType(this.context, node.x, node.y + 1, node.z - 1, this.entity).getDefaultPenalty() >= 0.0f) {
                successors[i++] = pathNode16;
            }
            if (this.isValidAdjacentSuccessor(pathNode = this.getPathNodeForHorizontal(node.x, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType1), node)) {
                successors[i++] = pathNode;
            }
            if (this.isValidAdjacentSuccessor(pathNode2 = this.getPathNodeForHorizontal(node.x - 1, node.y, node.z, j, d, Direction.WEST, pathNodeType1), node)) {
                successors[i++] = pathNode2;
            }
            if (this.isValidAdjacentSuccessor(pathNode3 = this.getPathNodeForHorizontal(node.x + 1, node.y, node.z, j, d, Direction.EAST, pathNodeType1), node)) {
                successors[i++] = pathNode3;
            }
            if (this.isValidAdjacentSuccessor(pathNode4 = this.getPathNodeForHorizontal(node.x, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType1), node)) {
                successors[i++] = pathNode4;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode2, pathNode4, pathNode5 = this.getPathNodeForHorizontal(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType1))) {
                successors[i++] = pathNode5;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode3, pathNode4, pathNode6 = this.getPathNodeForHorizontal(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType1))) {
                successors[i++] = pathNode6;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode2, pathNode, pathNode7 = this.getPathNodeForHorizontal(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType1))) {
                successors[i++] = pathNode7;
            }
            if (this.isValidDiagonalSuccessor(node, pathNode3, pathNode, pathNode8 = this.getPathNodeForHorizontal(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType1))) {
                successors[i++] = pathNode8;
            }
            if(usedVerticalNodeAsSuccessor) this.verticalPathNodeCounter = VERTICAL_NODE_PENALTY;
            this.verticalPathNodeCounter--;
            return i;
        }
        return super.getSuccessors(successors, node);
    }

    protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode, @Nullable PathNode zDiagNode) {
        if (zDiagNode == null || xDiagNode == null || zNode == null) {
            return false;
        }
        if (zDiagNode.visited) {
            return false;
        }
        if (xDiagNode.y > xNode.y || zNode.y > xNode.y) {
            return false;
        }
        if (zNode.type == PathNodeType.WALKABLE_DOOR || xDiagNode.type == PathNodeType.WALKABLE_DOOR || zDiagNode.type == PathNodeType.WALKABLE_DOOR) {
            return false;
        }
        BlockPos xDiagPos = new BlockPos(xDiagNode.x, xDiagNode.y, xDiagNode.z);
        BlockPos zDiagPos = new BlockPos(zDiagNode.x, zDiagNode.y, zDiagNode.z);
        World world = this.entity.getWorld();
        if(world.getBlockState(xDiagPos.down()).isReplaceable() || world.getBlockState(zDiagPos.down()).isReplaceable()) return false;
        boolean bl = xDiagNode.type == PathNodeType.FENCE && zNode.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5;
        return zDiagNode.penalty >= 0.0f && (xDiagNode.y < xNode.y || xDiagNode.penalty >= 0.0f || bl) && (zNode.y < xNode.y || zNode.penalty >= 0.0f || bl);
    }


    protected boolean canChooseUpwardNode(int x, int y, int z){
        World world = this.entity.getWorld();
        BlockPos pos = new BlockPos(x, y, z);
        if(!world.getBlockState(pos.down()).isAir()) return true;
        return this.verticalPathNodeCounter <= 0;
    }

    public PathContext getPathContext(){
        return this.context;
    }

}
