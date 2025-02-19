package survivalplus.modid.world.baseassaults;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.sounds.ModSounds;
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
    public static final int BASE_ASSAULT_TIME_NEEDED = 250000;

    public static Type<BaseAssaultManager> getPersistentStateType(ServerWorld world) {
        return new PersistentState.Type<>(() -> new BaseAssaultManager(world), (nbt, registryLookup) -> BaseAssaultManager.fromNbt(world, nbt), null);
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
    
    public void startBaseAssault(ServerPlayerEntity player) {
        if (player.isSpectator() || player.isCreative()) {
            return;
        }
        if (this.world.getGameRules().getBoolean(ModGamerules.DISABLE_BASEASSAULTS) || world.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        DimensionType dimensionType = player.getWorld().getDimension();
        if (!dimensionType.bedWorks()) {
            return;
        }
        PlayerData playerState = PlayerData.getPlayerState(player);
        BlockPos playerPos = player.getBlockPos();
        if(playerState.baseAssaultTimer < BASE_ASSAULT_TIME_NEEDED) {
            if(playerState.baseAssaultTimer > (BASE_ASSAULT_TIME_NEEDED - 36000) && !playerState.receivedBAWarningMessage) {
                if(!world.isClient){
                    Random rand = world.random;
                    int x = rand.nextInt(6);
                    int z = rand.nextInt(6);
                    switch (rand.nextInt(3)){
                        case 0 -> x += 5;
                        case 1 -> z += 5;
                        default -> {
                            x += 5;
                            z += 5;
                        }
                    }
                    x = rand.nextBoolean() ? x : -x;
                    z = rand.nextBoolean() ? z : -z;
                    world.playSound(null, playerPos.add(x, 1, z), ModSounds.BASE_ASSAULT_WARNING, SoundCategory.HOSTILE, 1.0f, 1.0f);
                }
                player.sendMessage(Text.translatable("event.survival-plus.warning"), true);
                playerState.receivedBAWarningMessage = true;
            }
            return;
        }
        BlockPos spawnPos = ((IServerPlayerChanger) player).getMainSpawnPoint();
        if(spawnPos == null || !this.world.getBlockState(spawnPos).isIn(BlockTags.BEDS) || !playerPos.isWithinDistance(spawnPos, 64) || this.world.getAmbientDarkness() < 4) {
            return;
        }
        if(Math.abs(playerPos.getY() - spawnPos.getY()) > 16){
            return;
        }
        BaseAssault baseAssault = this.getOrCreateBaseAssault(player.getServerWorld(), spawnPos, player);
        if (!baseAssault.hasStarted()) {
            baseAssault.start(player);
            baseAssault.setCenter(spawnPos);
            if (!this.baseAssaults.containsKey(baseAssault.getId())) {
                this.baseAssaults.put(baseAssault.getId(), baseAssault);
            }
        }
        this.markDirty();
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
        baManager.nextAvailableId = nbt.getInt("NextAvailableID");
        baManager.currentTime = nbt.getInt("Tick");
        NbtList nbtList = nbt.getList("BaseAssaults", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            BaseAssault baseassault = new BaseAssault(world, nbtCompound);
            baManager.baseAssaults.put(baseassault.getId(), baseassault);
        }
        return baManager;
    }


    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("NextAvailableID", this.nextAvailableId);
        nbt.putInt("Tick", this.currentTime);
        NbtList nbtList = new NbtList();
        for (BaseAssault baseAssault : this.baseAssaults.values()) {
            NbtCompound nbtCompound = new NbtCompound();
            baseAssault.writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
        }
        nbt.put("BaseAssaults", nbtList);
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

