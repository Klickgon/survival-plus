package survivalplus.modid.entity.ai;


import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.mob.CreeperEntity;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.ReeperEntity;

public class ReeperIgniteGoal extends Goal {
    private final ReeperEntity reeper;
    @Nullable
    private LivingEntity target;
    private boolean hadTarget;

    public ReeperIgniteGoal(ReeperEntity reeper) {
        this.reeper = reeper;
        this.setControls(EnumSet.of(Control.MOVE));
        this.hadTarget = false;
    }

    public boolean canStart() {
        LivingEntity livingEntity = this.reeper.getTarget();

        if(livingEntity != null) this.hadTarget = true;
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
        if (this.target == null && !this.hadTarget) {
            this.reeper.setFuseSpeed(-1);
        } else if (this.reeper.squaredDistanceTo(this.target) > 49.0) {
            this.reeper.setFuseSpeed(-1);
        } else {
            this.reeper.setFuseSpeed(1);
        }
    }
}

