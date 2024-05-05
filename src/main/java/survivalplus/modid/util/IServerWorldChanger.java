package survivalplus.modid.util;

import net.minecraft.util.math.BlockPos;
import survivalplus.modid.world.BaseAssaults.BaseAssault;
import survivalplus.modid.world.BaseAssaults.BaseAssaultManager;

public interface IServerWorldChanger {

    BaseAssault getBaseAssaultAt(BlockPos bpos);

    BaseAssaultManager getBaseAssaultManager();
}
