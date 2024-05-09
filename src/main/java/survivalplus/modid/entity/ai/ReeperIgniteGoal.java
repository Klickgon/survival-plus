package survivalplus.modid.entity.ai;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.ReeperEntity;

import java.util.EnumSet;

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
            this.reeper.ignite();
        }
    }
}

