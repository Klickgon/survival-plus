package survivalplus.modid.entity.ai;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.ReeperEntity;

import java.util.EnumSet;
import java.util.List;

public class ReeperIgniteGoal extends Goal {
    private final ReeperEntity reeper;
    @Nullable
    private LivingEntity target;



    public ReeperIgniteGoal(ReeperEntity reeper) {
        this.reeper = reeper;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    public boolean canStart() {
        LivingEntity livingEntity = this.reeper.getTarget();
        return this.reeper.getFuseSpeed() > 0 || livingEntity != null && this.reeper.squaredDistanceTo(livingEntity) < 9.0;
    }

    public void start() {
        this.reeper.getNavigation().stop();
        this.target = this.reeper.getTarget();
    }

    public void stop() {
        this.target = null;

    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        if(this.target != null) {
            if (this.reeper.squaredDistanceTo(this.target) > 49.0) {
                return;
            }
            Vec3d vec3d = Vec3d.ofBottomCenter(this.reeper.getBlockPos());
            List<HostileEntity> list = this.reeper.getWorld().getEntitiesByClass(HostileEntity.class, new Box(vec3d.getX() - 3.0, vec3d.getY() - 3.0, vec3d.getZ() - 3.0, vec3d.getX() + 3.0, vec3d.getY() + 3.0, vec3d.getZ() + 3.0), hostileEntity -> true);
            if (list.size() < 3) {
                this.reeper.ignite();
            }
        }
    }
}

