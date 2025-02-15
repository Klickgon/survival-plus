package survivalplus.modid.util;

import net.minecraft.util.math.BlockPos;
import survivalplus.modid.world.baseassaults.BaseAssault;
import survivalplus.modid.world.baseassaults.BaseAssaultManager;

public interface IServerWorldChanger {

    BaseAssault getBaseAssaultAt(BlockPos bpos);

    BaseAssaultManager getBaseAssaultManager();

    boolean notEnoughTimeSinceRest();

}
