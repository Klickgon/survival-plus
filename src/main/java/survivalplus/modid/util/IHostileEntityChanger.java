package survivalplus.modid.util;

import net.minecraft.entity.ai.goal.GoalSelector;
import survivalplus.modid.world.BaseAssaults.BaseAssault;

public interface IHostileEntityChanger {
    void setBaseAssault(BaseAssault ba);

    BaseAssault getBaseAssault();

    GoalSelector getGoalSelector();
}
