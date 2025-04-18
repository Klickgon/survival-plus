package survivalplus.modid.mixin;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalplus.modid.PlayerData;
import survivalplus.modid.StateSaverAndLoader;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModPlayerStats;
import survivalplus.modid.world.baseassaults.BaseAssault;
import survivalplus.modid.world.baseassaults.BaseAssaultManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;


@Mixin(ServerWorld.class)
public abstract class ServerWorldChanger extends World implements IServerWorldChanger {

    @Unique
    private static final int SLEEP_TIME = 7000;

    @Unique
    public BaseAssaultManager baseAssaultManager;

    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow @NotNull public abstract MinecraftServer getServer();

    @Shadow public abstract ServerWorld toServerWorld();

    @Shadow @Final private ServerWorldProperties worldProperties;

    protected ServerWorldChanger(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    public long tickSleepSkip(long timeOfDay) {
        return this.properties.getTimeOfDay() + SLEEP_TIME; // Changes the sleeping skip from a set time point to 7000 ticks after sleep start
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;resetWeather()V"))
    public void changeWeatherReset(ServerWorld instance) { // Reduces the rain or thunder time by the time skip of sleeping in ticks
        int rain = this.worldProperties.getRainTime() - SLEEP_TIME;
        if(rain > 0) this.worldProperties.setRainTime(rain);
        else {
            this.worldProperties.setRaining(false);
            this.worldProperties.setRainTime(0);
        }
        int thunder = this.worldProperties.getThunderTime() - SLEEP_TIME;
        if(thunder > 0) this.worldProperties.setThunderTime(thunder);
        else {
            this.worldProperties.setThundering(false);
            this.worldProperties.setThunderTime(0);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    protected void addTimeFromSleep(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            serverPlayer.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP));// Resets the "time since sleep" stat for every player once everyone wakes up
            StateSaverAndLoader.getPlayerState(serverPlayer).baseAssaultTimer += SLEEP_TIME * 2;
            // also increases the time since last Base Assault by the time of day skipped through sleeping
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void constructorInject(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci){
        ServerWorld sworld = this.toServerWorld();
        this.baseAssaultManager = sworld.getPersistentStateManager().getOrCreate(BaseAssaultManager.getPersistentStateType(this.getDimensionEntry()));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    protected void injectTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            serverPlayer.incrementStat(ModPlayerStats.TIME_SINCE_SLEEP);
            if (serverPlayer.getRespawn() == null || serverPlayer.getRespawnTarget(false, TeleportTarget.NO_OP).missingRespawnBlock())
                serverPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT));
            this.baseAssaultManager.startBaseAssault(serverPlayer);
            BlockPos spawnPoint = ((IServerPlayerChanger) serverPlayer).getMainSpawnPoint();
            if(spawnPoint != null && this.getBlockState(spawnPoint).isIn(BlockTags.BEDS)){
                double distance = serverPlayer.getPos().squaredDistanceTo(spawnPoint.toCenterPos());
                PlayerData playerData = PlayerData.getPlayerState(serverPlayer);
                if(distance < 9216)
                    playerData.baseAssaultTimer += 2;
                else if(distance < 65536 && playerData.baseAssaultTimer < BaseAssaultManager.BASE_ASSAULT_TIME_NEEDED - 3000)
                    playerData.baseAssaultTimer++;
            }
        }
        baseAssaultManager.tick(this.toServerWorld());
    }

    @Nullable
    public BaseAssault getBaseAssaultAt(BlockPos pos) {
        return this.baseAssaultManager.getBaseAssaultAt(pos, 9216);
    }

    public BaseAssaultManager getBaseAssaultManager(){
        return this.baseAssaultManager;
    }

    public boolean notEnoughTimeSinceRest() {
        for (ServerPlayerEntity serverPlayer : this.getPlayers()) {
            if(serverPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_SLEEP)) < 4000) {
                return true;
            }
        }
        return false;
    }
}
