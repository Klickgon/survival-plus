package survivalplus.modid.entity.ai.pathing;

import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import survivalplus.modid.entity.ai.pathing.pathmaker.DestrZombPathNodeMaker;

public class DestroyZombieNavigation extends MobNavigation {

    public DestroyZombieNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new DestrZombPathNodeMaker(entity);
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }
}
