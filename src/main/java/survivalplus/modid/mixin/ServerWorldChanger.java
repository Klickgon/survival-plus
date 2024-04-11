package survivalplus.modid.mixin;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.util.IWorldChanger;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static survivalplus.modid.util.IWorldChanger.*;

@Mixin(ServerWorld.class)
public abstract class ServerWorldChanger extends World {

    @Shadow @Final private static Logger LOGGER;


    protected ServerWorldChanger(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    public long ticksleep(long timeOfDay) {
        return this.properties.getTimeOfDay() + 10000L;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    protected void resetSleepcooldown(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        IWorldChanger world = (IWorldChanger) (Object) this;
        SurvivalPlus.LOGGER.info("sleepcooldown applied");
        world.setSleepCooldown(3000);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    protected void sleepCooldownMinus(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        IWorldChanger world = (IWorldChanger) (Object) this;
        world.setSleepCooldown(world.getSleepCooldown() - 1);
    }

}
