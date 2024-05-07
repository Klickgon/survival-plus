package survivalplus.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.IWorldChanger;
import survivalplus.modid.util.ModPlayerStats;
import survivalplus.modid.world.BaseAssaults.BaseAssault;
import survivalplus.modid.world.BaseAssaults.BaseAssaultManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;


@Mixin(ServerWorld.class)
public abstract class ServerWorldChanger extends World implements IServerWorldChanger {


    @Unique
    public BaseAssaultManager baseAssaultManager;

    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Shadow public abstract ServerWorld toServerWorld();

    protected ServerWorldChanger(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);

    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    public long ticksleep(long timeOfDay) {
        return this.properties.getTimeOfDay() + 8000L; // Changes the sleeping skip from a set time point to 8000 ticks after sleep start
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    protected void resetTimeSinceSleepStat(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            serverPlayer.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP));// Resets the "time since sleep" stat for every player once everyone wakes up
            serverPlayer.increaseStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_LAST_BASEASSAULT), 8000);
            // also increases the time since last Base Assault by the time of day skipped through sleeping
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void BAMconstructInject(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci){
        this.baseAssaultManager = this.toServerWorld().getPersistentStateManager().getOrCreate(BaseAssaultManager.getPersistentStateType(this.toServerWorld()), BaseAssaultManager.nameFor(this.toServerWorld().getDimensionEntry()));
    }


    @Inject(method = "tick", at = @At("HEAD"))
    protected void injectTickHead(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        IWorldChanger oworld = (IWorldChanger) this.getServer().getOverworld();
        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            serverPlayer.incrementStat(ModPlayerStats.TIME_SINCE_SLEEP); // increments and then checks if all players can sleep in this world
            if(serverPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP)) < 3000) {
                oworld.setEnoughTimeSinceRest(false);
                break;
            }
            oworld.setEnoughTimeSinceRest(true);
        }

        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            BlockPos bpos = serverPlayer.getSpawnPointPosition();
                if(bpos != null){
                    Optional<Vec3d> result = PlayerEntity.findRespawnPosition(serverPlayer.getServerWorld(), bpos, 0.0f, false, true);
                    if (result.isPresent())
                        serverPlayer.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
                    else serverPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
            } else serverPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
        }

        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            this.baseAssaultManager.startBaseAssault(serverPlayer);
            serverPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_LAST_BASEASSAULT));
        }
        baseAssaultManager.tick();
    }

    @Nullable
    public BaseAssault getBaseAssaultAt(BlockPos pos) {
        return this.baseAssaultManager.getBaseAssaultAt(pos, 9216);
    }

    public BaseAssaultManager getBaseAssaultManager(){
        return this.baseAssaultManager;
    }

}
