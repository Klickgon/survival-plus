package survivalplus.modid.world.baseassaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.sounds.ModSounds;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.ModGamerules;

import java.util.Iterator;
import java.util.List;

public class BaseAssaultManager
extends PersistentState {
    private static final String BASEASSAULT = "base assault";
    private ServerWorld world = null;
    private final Int2ObjectMap<BaseAssault> baseAssaults = new Int2ObjectOpenHashMap<>();
    private int currentTime;
    private int nextAvailableId;
    public static final int BASE_ASSAULT_TIME_NEEDED = 250000;

    public static PersistentStateType<BaseAssaultManager> getPersistentStateType(RegistryEntry<DimensionType> dimensionType) {
        return STATE_TYPE;
    }

    public static final Codec<BaseAssaultManager> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BaseAssaultManager.BAWithId.CODEC
                                    .listOf()
                                    .optionalFieldOf("base_assaults", List.of())
                                    .forGetter(baManager -> baManager.baseAssaults.int2ObjectEntrySet().stream().map(survivalplus.modid.world.baseassaults.BaseAssaultManager.BAWithId::fromMapEntry).toList()),
                            Codec.INT.fieldOf("next_id").forGetter(baManager -> baManager.nextAvailableId),
                            Codec.INT.fieldOf("tick").forGetter(baManager -> baManager.currentTime)
                    )
                    .apply(instance, BaseAssaultManager::new)
    );

    public static final PersistentStateType<BaseAssaultManager> STATE_TYPE = new PersistentStateType<>("base_assaults", BaseAssaultManager::new , CODEC, null);

    public BaseAssaultManager(ServerWorld world) {
        this.world = world;
        this.markDirty();
        this.nextAvailableId = 1;
    }

    private BaseAssaultManager(List<BaseAssaultManager.BAWithId> raids, int nextAvailableId, int currentTime) {
        for (BaseAssaultManager.BAWithId baWithId : raids) {
            this.baseAssaults.put(baWithId.id, baWithId.ba);
        }

        this.nextAvailableId = nextAvailableId;
        this.currentTime = currentTime;
    }

    public BaseAssaultManager() {
        this.markDirty();
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
            baseAssault.tick(world);
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
            baseAssault.start(player, world);
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
        return baseAssault != null ? baseAssault : new BaseAssault(this.nextId(), pos, spe);
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

    record BAWithId(int id, BaseAssault ba) {
        public static final Codec<BaseAssaultManager.BAWithId> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(Codec.INT.fieldOf("id").forGetter(BaseAssaultManager.BAWithId::id), BaseAssault.CODEC.forGetter(BaseAssaultManager.BAWithId::ba))
                        .apply(instance, BaseAssaultManager.BAWithId::new)
        );

        public static BaseAssaultManager.BAWithId fromMapEntry(Int2ObjectMap.Entry<BaseAssault> entry) {
            return new BaseAssaultManager.BAWithId(entry.getIntKey(), entry.getValue());
        }
    }
}

