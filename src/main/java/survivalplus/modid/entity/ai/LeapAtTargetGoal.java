/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class LeapAtTargetGoal
extends Goal {
    private final MobEntity mob;
    private LivingEntity target;
    private final float velocity;
    private boolean canLeapAttack = false;

    public LeapAtTargetGoal(MobEntity mob, float velocity) {
        this.mob = mob;
        this.velocity = velocity;
        this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.mob.hasControllingPassenger()) {
            return false;
        }
        this.target = this.mob.getTarget();
        if (this.target == null) {
            return false;
        }
        double d = this.mob.squaredDistanceTo(this.target);
        if (d < 2.0 ||d > 28.0) {
            return false;
        }
        if (!this.mob.isOnGround()) {
            return false;
        }
        return this.mob.getRandom().nextInt(LeapAtTargetGoal.toGoalTicks(40)) == 0;
    }

    @Override
    public void tick(){
        this.attack(this.target);
        this.canLeapAttack = !this.mob.isOnGround();
    }

    protected void attack(LivingEntity target) {
        if (this.canAttack(target)) {
            this.mob.swingHand(Hand.MAIN_HAND);
            boolean bl = this.mob.tryAttack(target);
            this.canLeapAttack = !bl;
        }
    }
    protected boolean canAttack(LivingEntity target) {
        return this.canLeapAttack && this.mob.isInAttackRange(target) && this.mob.getVisibilityCache().canSee(target);
    }


    @Override
    public boolean shouldContinue() {
        return !this.mob.isOnGround();
    }

    @Override
    public void start() {
        Vec3d vec3d = this.mob.getVelocity();
        Vec3d vec3d2 = new Vec3d(this.target.getX() - this.mob.getX(), 0.0, this.target.getZ() - this.mob.getZ());
        if (vec3d2.lengthSquared() > 1.0E-7) {
            vec3d2 = vec3d2.normalize().multiply(1.1).add(vec3d.multiply(0.2));
        }
        this.mob.setVelocity(vec3d2.x, this.velocity, vec3d2.z);
    }
}

