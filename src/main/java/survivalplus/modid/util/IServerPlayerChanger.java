package survivalplus.modid.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface IServerPlayerChanger {

    BlockPos getMainSpawnPoint();

    void setShouldNotSpawnAtAnchor(boolean bl);

    boolean getShouldNotSpawnAtAnchor();

    Optional<ModRespawnPos> findModRespawnPosition(ServerWorld world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive);
}
