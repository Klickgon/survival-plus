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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

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
            BlockPos pos = new BlockPos(x, y + 1, z);
            if (world.getBlockState(pos).isIn(this.blockTag)){
                if(!world.getBlockState(pos.up()).isIn(this.blockTag) || !world.getBlockState(pos.up()).isIn(BlockTags.REPLACEABLE))
                    return getNodeWith(x, y, z, PathNodeType.BLOCKED, PathNodeType.BLOCKED.getDefaultPenalty());
                return getNodeWith(x, y, z, PathNodeType.WALKABLE, PathNodeType.WALKABLE.getDefaultPenalty());
            }
            if(world.getBlockState(pos).isIn(BlockTags.REPLACEABLE) && world.getBlockState(pos.up()).isIn(this.blockTag))
                return getNodeWith(x, y, z, PathNodeType.WALKABLE, PathNodeType.WALKABLE.getDefaultPenalty());
        }
        return super.getPathNode(x, y, z, maxYStep, prevFeetY, direction, nodeType);
    }

}
