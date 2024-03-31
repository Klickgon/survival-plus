package survivalplus.modid.entity.ai.pathing;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DestroyZombieNavigation extends MobNavigation {

    private int recalcCooldown = 0;
    private final TagKey<Block> blocktag;

    public DestroyZombieNavigation(MobEntity mobEntity, World world, TagKey<Block> blocktag) {
        super(mobEntity, world);
        this.blocktag = blocktag;
    }


    @Override
    public void recalculatePath() {
        if (this.recalcCooldown <= 0) {
            LivingEntity target = this.entity.getTarget();
            if (target != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo(target, (int) this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
                this.recalcCooldown = 40;
            }
        }
        else this.recalcCooldown--;

    }

    @Override
    public boolean isValidPosition(BlockPos pos) {
        boolean bl1 = this.entity.getWorld().getBlockState(this.entity.getBlockPos()).isIn(this.blocktag);
        boolean bl2 = this.entity.getWorld().getBlockState(this.entity.getBlockPos().up()).isIn(this.blocktag);
        return this.entity.isOnGround() || this.entity.isInFluid() || this.entity.hasVehicle() || (bl1 && bl2);
    }

}
