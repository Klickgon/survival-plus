package survivalplus.modid.entity.ai.pathing;

import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class ReeperNavigation extends MobNavigation {

    public ReeperNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    public boolean isValidPosition(BlockPos pos) {
        return true;
    }

}
