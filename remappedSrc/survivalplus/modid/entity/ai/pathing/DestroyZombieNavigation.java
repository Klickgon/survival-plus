package survivalplus.modid.entity.ai.pathing;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import survivalplus.modid.entity.ai.pathing.pathmaker.DestrZombPathNodeMaker;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

public class DestroyZombieNavigation extends MobNavigation {

    protected int recalcCooldown = 0;
    protected TagKey<Item> reqItem;
    protected TagKey<Block> blockTag;


    public DestroyZombieNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
        if(mobEntity instanceof MinerZombieEntity){
            this.reqItem = ItemTags.PICKAXES;
            this.blockTag = MinerZombieEntity.BLOCKTAG;
        }
        else if(mobEntity instanceof LumberjackZombieEntity){
            this.reqItem = ItemTags.AXES;
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
        }
        else if(mobEntity instanceof DiggingZombieEntity){
            this.reqItem = ItemTags.SHOVELS;
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
        }
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new DestrZombPathNodeMaker(entity);
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    @Override
    public void recalculatePath() {
        Path path;
        BlockPos pathPos;
        if (this.recalcCooldown <= 0 || !this.entity.getMainHandStack().isIn(this.reqItem) || ((path = this.getCurrentPath()) != null && (pathPos = path.getCurrentNodePos()) != null && (this.world.getBlockState(pathPos).isIn(this.blockTag) && pathPos.getY() == ((IHostileEntityChanger)entity).getElevatedBlockPos().getY() - 1))) {
            LivingEntity target = this.entity.getTarget();
            if (target != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo(target, (int) this.entity.getAttributeValue(EntityAttributes.FOLLOW_RANGE));
                this.recalcCooldown = 10;
            }
        }
        else this.recalcCooldown--;
    }
}
