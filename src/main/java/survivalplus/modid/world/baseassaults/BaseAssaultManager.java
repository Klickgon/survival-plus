/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.world.baseassaults;

import com.google.common.collect.Maps;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.ModGamerules;

import java.util.Iterator;
import java.util.Map;

public class BaseAssaultManager
extends PersistentState {
    private static final String BASEASSAULT = "base assault";
    private final ServerWorld world;
    private final Map<Integer, BaseAssault> baseAssaults = Maps.newHashMap();
    private int currentTime;
    private int nextAvailableId;

    public static Type<BaseAssaultManager> getPersistentStateType(ServerWorld world) {
        return new Type<BaseAssaultManager>(() -> new BaseAssaultManager(world), nbt -> BaseAssaultManager.fromNbt(world, nbt), DataFixTypes.SAVED_DATA_RAIDS);
    }

    public BaseAssaultManager(ServerWorld world) {
        this.world = world;
        this.markDirty();
        this.nextAvailableId = 1;
    }


    public void tick() {
        ++this.currentTime;
        Iterator<BaseAssault> iterator = this.baseAssaults.values().iterator();
        while (iterator.hasNext()) {
            BaseAssault baseAssault = iterator.next();
            if (this.world.getGameRules().getBoolean(ModGamerules.DISABLE_BASEASSAULTS)) {
                baseAssault.invalidate();
            }
            if (baseAssault.hasStopped()) {
                iterator.remove();
                this.markDirty();
                continue;
            }
            baseAssault.tick();
        }
        if (this.currentTime % 200 == 0) {
            this.markDirty();
        }

    }


    @Nullable
    public BaseAssault startBaseAssault(ServerPlayerEntity player) {
        if (player.isSpectator() || player.isCreative()) {
            return null;
        }
        if (this.world.getGameRules().getBoolean(ModGamerules.DISABLE_BASEASSAULTS) || world.getDifficulty() == Difficulty.PEACEFUL) {
            return null;
        }
        DimensionType dimensionType = player.getWorld().getDimension();
        if (!dimensionType.bedWorks()) {
            return null;
        }
        if(((IServerPlayerChanger)player).getTimeSinceLastBaseAssault() < 400) {
            return null;
        }
        BlockPos playerPos = player.getBlockPos();
        BlockPos spawnPos = player.getSpawnPointPosition();
        if(spawnPos == null || !this.world.getBlockState(spawnPos).isIn(BlockTags.BEDS)|| !playerPos.isWithinDistance(spawnPos, 64) || world.getAmbientDarkness() < 4) {
            return null;
        }
        BaseAssault baseAssault = this.getOrCreateBaseAssault(player.getServerWorld(), spawnPos, player);
        if (!baseAssault.hasStarted()) {
            if (!this.baseAssaults.containsKey(baseAssault.getId())) {
                this.baseAssaults.put(baseAssault.getId(), baseAssault);
            }
        }
        baseAssault.setCenter(spawnPos);
        baseAssault.start(player);
        this.markDirty();
        return baseAssault;
    }

    private int nextId() {
        return ++this.nextAvailableId;
    }

    private BaseAssault getOrCreateBaseAssault(ServerWorld world, BlockPos pos, ServerPlayerEntity spe) {
        BaseAssault baseAssault = this.getBaseAssaultAt(pos, 9216);
        return baseAssault != null ? baseAssault : new BaseAssault(this.nextId(), world, pos, spe);
    }

    public static BaseAssaultManager fromNbt(ServerWorld world, NbtCompound nbt) {
        BaseAssaultManager baManager = new BaseAssaultManager(world);
        baManager.currentTime = nbt.getInt("Tick");
        return baManager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Tick", this.currentTime);
        return nbt;
    }

    public static String nameFor(RegistryEntry<DimensionType> dimensionTypeEntry) {
        return BASEASSAULT;
    }

    @Nullable
    public BaseAssault getBaseAssaultAt(BlockPos pos, int searchDistance) {
        BaseAssault baseAssault = null;
        double d = searchDistance;
        for (BaseAssault baseAssault2 : this.baseAssaults.values()) {
            double e = baseAssault2.getCenter().getSquaredDistance(pos);
            if (!baseAssault2.isActive() || !(e < d)) continue;
            baseAssault = baseAssault2;
            d = e;
        }
        return baseAssault;
    }

}

