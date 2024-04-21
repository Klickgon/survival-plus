package survivalplus.modid.entity.ai.pathing;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import survivalplus.modid.entity.ai.pathing.pathmaker.DestrZombPathNodeMaker;

public class DestroyZombieNavigation extends MobNavigation {

    private int recalcCooldown = 0;
    public TagKey<Block> blocktag;

    public DestroyZombieNavigation(MobEntity mobEntity, World world, TagKey<Block> blocktag) {
        super(mobEntity, world);
        this.blocktag = blocktag;
    }

    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new DestrZombPathNodeMaker(this.blocktag);
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }


    @Override
    public void recalculatePath() {
        if (this.recalcCooldown <= 0) {
            LivingEntity target = this.entity.getTarget();
            if (target != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo(target, (int) this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
                this.recalcCooldown = 20;
            }
        }
        else this.recalcCooldown--;

    }

    @Override
    public boolean isValidPosition(BlockPos pos) {
        boolean WithinBlocktag1 = this.entity.getWorld().getBlockState(this.entity.getBlockPos()).isIn(this.blocktag);
        boolean WithinBlocktag2 = this.entity.getWorld().getBlockState(this.entity.getBlockPos().up()).isIn(this.blocktag);
        return this.entity.isOnGround() || this.entity.isInFluid() || this.entity.hasVehicle() || (WithinBlocktag1 && WithinBlocktag2);
    }

}
