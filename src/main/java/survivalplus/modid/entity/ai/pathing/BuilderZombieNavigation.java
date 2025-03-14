package survivalplus.modid.entity.ai.pathing;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        Path path;
        BlockPos pathPos;
        if (this.recalcCooldown <= 0 || !this.entity.getMainHandStack().isOf(Items.DIRT) || ((path = this.getCurrentPath()) != null && (pathPos = path.getCurrentNodePos()) != null && (this.world.getBlockState(pathPos).isOf(Blocks.DIRT)))) {
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
