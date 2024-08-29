package survivalplus.modid.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalplus.modid.util.IServerPlayerChanger;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockChanger {

    @Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tryUsePortal(Lnet/minecraft/block/Portal;Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.BEFORE))
    private void shouldSkipRespawnAnchorFieldFlip(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci){
        if(entity instanceof ServerPlayerEntity && entity.getWorld().getRegistryKey() == World.END) ((IServerPlayerChanger)entity).setShouldNotSpawnAtAnchor(true);
    }
}
