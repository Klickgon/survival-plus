package survivalplus.modid.util;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.util.math.BlockPos;
import survivalplus.modid.world.baseassaults.BaseAssault;

public interface IHostileEntityChanger {
    void setBaseAssault(BaseAssault ba);

    BaseAssault getBaseAssault();

    GoalSelector getGoalSelector();

    BlockPos getCustomBlockPos();
}
