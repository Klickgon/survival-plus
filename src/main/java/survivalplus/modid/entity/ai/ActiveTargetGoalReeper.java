/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package survivalplus.modid.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import survivalplus.modid.entity.custom.ReeperEntity;

public class ActiveTargetGoalReeper<T extends LivingEntity>
extends ActiveTargetGoal {

    public ActiveTargetGoalReeper(MobEntity mob, Class targetClass, boolean checkVisibility) {
        super(mob, targetClass, checkVisibility);
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(null).ignoreVisibility();
    }

    @Override
    public void tick() {
        if(this.mob.getTarget() != null && this.mob.squaredDistanceTo(this.mob.getTarget()) < 70){
            ((ReeperEntity) this.mob).wasWithinDistance = true;
        }
        super.tick();
    }
}

