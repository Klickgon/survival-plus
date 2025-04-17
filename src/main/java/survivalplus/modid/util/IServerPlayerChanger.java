package survivalplus.modid.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface IServerPlayerChanger {

    ServerPlayerEntity.Respawn getMainSpawn();

    BlockPos getMainSpawnPoint();

    void setShouldNotSpawnAtAnchor(boolean bl);

    boolean getShouldNotSpawnAtAnchor();
}
