package survivalplus.modid.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record ModRespawnPos(Vec3d pos, float yaw) {

    public static ModRespawnPos fromCurrentPos(Vec3d respawnPos, BlockPos currentPos) {
        return new ModRespawnPos(respawnPos, ModRespawnPos.getYaw(respawnPos, currentPos));
    }

    private static float getYaw(Vec3d respawnPos, BlockPos currentPos) {
        Vec3d vec3d = Vec3d.ofBottomCenter(currentPos).subtract(respawnPos).normalize();
        return (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875 - 90.0);
    }
}
