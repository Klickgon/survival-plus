package survivalplus.modid.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record ModRespawnPos(Vec3d pos, float yaw, float pitch) {

    public static ModRespawnPos fromCurrentPos(Vec3d respawnPos, BlockPos currentPos, float f) {
        return new ModRespawnPos(respawnPos, getYaw(respawnPos, currentPos), f);
    }

    private static float getYaw(Vec3d respawnPos, BlockPos currentPos) {
        Vec3d vec3d = Vec3d.ofBottomCenter(currentPos).subtract(respawnPos).normalize();
        return (float)MathHelper.wrapDegrees(MathHelper.atan2(vec3d.z, vec3d.x) * 180.0F / (float)Math.PI - 90.0);
    }
}
