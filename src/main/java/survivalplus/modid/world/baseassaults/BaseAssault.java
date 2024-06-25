/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.world.baseassaults;

import com.google.common.collect.Sets;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.ModEntities;
import survivalplus.modid.entity.ai.BaseAssaultGoal;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModPlayerStats;

import java.util.*;
import java.util.function.Predicate;

public class BaseAssault {

    private static final Text EVENT_TEXT = Text.translatable("event.survival-plus.baseassault");
    private static final Text VICTORY_TITLE = Text.translatable("event.survival-plus.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.survival-plus.defeat.full");
    private static final String HOSTILES_REMAINING_TRANSLATION_KEY = "event.survival-plus.baseassault.hostiles_remaining";
    private int requiredHostileCount;
    private HostileEntity[] hostiles;
    private LinkedList<UUID> hostileIDs = new LinkedList<>();
    private final boolean isFromNBT;
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    public ServerPlayerEntity attachedPlayer;
    public final UUID playerID;
    private boolean started;
    private final int id;
    private float totalHealth;
    private boolean active;
    private byte[] wave;
    private final ServerBossBar bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.GREEN, BossBar.Style.NOTCHED_10);
    private int preBaseAssaultTicks;
    private Status status;
    private int finishCooldown;
    private int nextAvailableId;
    private boolean waveSpawned;
    private boolean winStatIncreased;
    public boolean findPlayerInsteadOfBed;

    public BaseAssault(int id, ServerWorld world, BlockPos pos, ServerPlayerEntity attachedPlayer) {
        this.id = id;
        this.isFromNBT = false;
        this.world = world;
        this.attachedPlayer = attachedPlayer;
        this.playerID = attachedPlayer.getUuid();
        this.active = true;
        this.preBaseAssaultTicks = 300;
        this.bar.setPercent(0.0f);
        this.center = pos;
        this.status = Status.ONGOING;
        this.waveSpawned = false;
        this.findPlayerInsteadOfBed = false;
        this.wave = getWave(attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) + 1);
    }

    public BaseAssault(ServerWorld world, NbtCompound nbt) {
        this.isFromNBT = true;
        this.world = world;
        this.playerID = nbt.getUuid("playerID");
        this.id = nbt.getInt("BAId");
        this.started = nbt.getBoolean("BAStarted");
        this.active = nbt.getBoolean("BAActive");
        this.ticksActive = nbt.getLong("BATicksActive");
        this.preBaseAssaultTicks = nbt.getInt("BAPreAssaultTicks");
        this.totalHealth = nbt.getFloat("BATotalHealth");
        this.center = new BlockPos(nbt.getInt("baCX"), nbt.getInt("baCY"), nbt.getInt("baCZ"));
        this.status = Status.fromName(nbt.getString("baStatus"));
        this.waveSpawned = nbt.getBoolean("WaveSpawned");
        this.winStatIncreased = nbt.getBoolean("WinStatIncreased");
        this.wave = nbt.getByteArray("Wave");
        this.requiredHostileCount = nbt.getInt("HostileCount");
        this.findPlayerInsteadOfBed = nbt.getBoolean("findPlayer");
        if (nbt.contains("hostileIDs", NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList("hostileIDs", NbtElement.INT_ARRAY_TYPE);
            for (NbtElement nbtElement : nbtList) {
                this.hostileIDs.add(NbtHelper.toUuid(nbtElement));
            }
        }
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("BAId", this.id);
        nbt.putBoolean("BAStarted", this.started);
        nbt.putBoolean("BAActive", this.active);
        nbt.putLong("BATicksActive", this.ticksActive);
        nbt.putInt("PreAssaultTicks", this.preBaseAssaultTicks);
        nbt.putFloat("BATotalHealth", this.totalHealth);
        nbt.putString("BAStatus", this.status.getName());
        nbt.putInt("baCX", this.center.getX());
        nbt.putInt("baCY", this.center.getY());
        nbt.putInt("baCZ", this.center.getZ());
        nbt.putByteArray("Wave", this.wave);
        nbt.putUuid("playerID", this.playerID);
        nbt.putBoolean("WaveSpawned", this.waveSpawned);
        nbt.putBoolean("WinStatIncreased", this.winStatIncreased);
        nbt.putBoolean("findPlayer", this.findPlayerInsteadOfBed);
        nbt.putInt("HostileCount", getHostileCount());
        NbtList nbtList = new NbtList();
        if(this.hostiles != null) {
            for (HostileEntity hostile : this.hostiles) {
                if (hostile != null) nbtList.add(NbtHelper.fromUuid(hostile.getUuid()));
            }
        }
        nbt.put("hostileIDs", nbtList);
        return nbt;
    }


    public boolean isFinished() {
        return this.hasWon() || this.hasLost();
    }

    public boolean hasStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean hasWon() {
        return this.status == Status.VICTORY;
    }

    public boolean hasLost() {
        return this.status == Status.LOSS;
    }


    public byte[] getWave(int wavenumber) {
        return switch (wavenumber) {
            case 1 -> BaseAssaultWaves.BASEASSAULT_ONE;
            case 2 -> BaseAssaultWaves.BASEASSAULT_TWO;
            case 3 -> BaseAssaultWaves.BASEASSAULT_THREE;
            case 4 -> BaseAssaultWaves.BASEASSAULT_FOUR;
            case 5 -> BaseAssaultWaves.BASEASSAULT_FIVE;
            case 6 -> BaseAssaultWaves.BASEASSAULT_SIX;
            case 7 -> BaseAssaultWaves.BASEASSAULT_SEVEN;
            case 8 -> BaseAssaultWaves.BASEASSAULT_EIGHT;
            case 9 -> BaseAssaultWaves.BASEASSAULT_NINE;
            case 10 -> BaseAssaultWaves.BASEASSAULT_TEN;
            case 11 -> BaseAssaultWaves.BASEASSAULT_ELEVEN;
            case 12 -> BaseAssaultWaves.BASEASSAULT_TWELVE;
            default -> getGeneratedWave();
        };
    }

    private byte [] getGeneratedWave(){
        return PlayerData.getPlayerState(this.attachedPlayer).generatedWave;
    }

    private void generateNextWave(){
        byte[] wave = getGeneratedWave();
        Random random = this.world.getRandom();
        if(calcWaveSize(wave) < 45){ // checks if the generated wave has less than 45 mobs in it
            SurvivalPlus.LOGGER.info("{}'s generated wave size is below 45, incrementing it.", this.attachedPlayer.getName().getString());
            int randomIndex = random.nextInt(11); // to increment the count of a random mob for the next wave
            wave[randomIndex]++;
        }
        else { // if the wave has 45 mobs, one random mob gets replaced with a different one
            SurvivalPlus.LOGGER.info("{}'s generated wave size is 45, shuffling.", this.attachedPlayer.getName().getString());
            int randomIndex1 = random.nextInt(11);
            wave[randomIndex1]--;
            int randomIndex2;
            do randomIndex2 = random.nextInt(11);
            while (randomIndex1 == randomIndex2);
            wave[randomIndex2]++;
        }
        SurvivalPlus.LOGGER.info("{}'s new generated Wave: {}", this.attachedPlayer.getName().getString(), Arrays.toString(wave));
        PlayerData.getPlayerState(this.attachedPlayer).generatedWave = wave;
    }

    private static int calcWaveSize(byte[] array){
        int sum = 0;
        for(byte num : array) sum += num;
        return sum;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean hasStarted() {
        return this.started;
    }


    private Predicate<ServerPlayerEntity> isInBaseAssaultDistance() {
        return player -> {
            IServerWorldChanger sworld = (IServerWorldChanger) this.world;
            BlockPos bpos = player.getBlockPos();
            return player.isAlive() && sworld.getBaseAssaultAt(bpos) == this;
        };
    }

    public BlockPos getCenter(){
        return this.center;
    }

    private void updateBarToPlayers() {
        HashSet<ServerPlayerEntity> set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = this.world.getPlayers(this.isInBaseAssaultDistance());
        for (ServerPlayerEntity serverPlayerEntity : list) {
            if (set.contains(serverPlayerEntity)) continue;
            this.bar.addPlayer(serverPlayerEntity);
        }
        for (ServerPlayerEntity serverPlayerEntity : set) {
            if (list.contains(serverPlayerEntity)) continue;
            this.bar.removePlayer(serverPlayerEntity);
        }
    }

    public void setCenter(BlockPos bpos){
        this.center = bpos;
    }

    public void start(PlayerEntity player) {

    }

    public void invalidate() {
        this.active = false;
        this.bar.clearPlayers();
        this.status = Status.STOPPED;
    }

    public void tick() {
        if(this.isFromNBT){
            if(this.attachedPlayer == null) {
                this.attachedPlayer = this.world.getServer().getPlayerManager().getPlayer(this.playerID);
                if(!this.world.getServer().getPlayerManager().getPlayerList().isEmpty() && this.attachedPlayer == null) invalidate();
                return;
            }
            if(this.wave == null){
                this.wave = getWave(attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) + 1);
                return;
            }
            if(this.hostiles == null && this.waveSpawned){
                ArrayList<HostileEntity> hostileList = new ArrayList<>();
                for (UUID uuid : this.hostileIDs) {
                    HostileEntity hostile = (HostileEntity) this.world.getEntity(uuid);
                    if(hostile != null) hostileList.add(hostile);
                }
                if(hostileList.size() != this.requiredHostileCount) return;
                this.hostiles = hostileList.toArray(new HostileEntity[hostileList.size()]);
                for (HostileEntity hostile : this.hostiles) {
                        IHostileEntityChanger hostile2 = (IHostileEntityChanger) hostile;
                        hostile2.setBaseAssault(this);
                        if (hostile instanceof AbstractSkeletonEntity) hostile2.getGoalSelector().add(4, new BaseAssaultGoal(hostile, 1.0));
                        else hostile2.getGoalSelector().add(5, new BaseAssaultGoal(hostile, 1.0));
                }
            }
        }
        if (this.hasStopped()) {
            return;
        }
        if (this.status == Status.ONGOING) {
            boolean bl = this.active;
            this.active = this.world.isChunkLoaded(this.center);
            if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                this.invalidate();
                return;
            }
            if (bl != this.active) {
                this.bar.setVisible(this.active);
            }
            if (!this.active) {
                return;
            }
            ++this.ticksActive;
            if (!this.attachedPlayer.isAlive() && !this.world.getBlockState(this.center).isIn(BlockTags.BEDS)) {
                this.status = Status.LOSS;
            }
            updateCenter();
            if (this.waveSpawned && getCurrentHostilesHealth() <= 0) {
                this.status = Status.VICTORY;
            }
            if (this.ticksActive >= 48000L) {
                this.invalidate();
            }
            if (this.ticksActive >= 2000L && getHostileCount() <= 3) {
                for(HostileEntity hostile : this.hostiles){
                    if(hostile != null) hostile.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 3));
                }
            }
            int i = this.getHostileCount();
            if (i == 0) {
                if (this.preBaseAssaultTicks > 0) {
                    if (this.preBaseAssaultTicks == 300 || this.preBaseAssaultTicks % 20 == 0) {
                        this.updateBarToPlayers();
                    }
                    --this.preBaseAssaultTicks;
                    this.bar.setPercent(MathHelper.clamp((float) (300 - this.preBaseAssaultTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.preBaseAssaultTicks == 0 && this.hasStarted()) {
                    this.preBaseAssaultTicks = 300;
                    this.bar.setName(EVENT_TEXT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                if (this.waveSpawned) updateBar();
                this.updateBarToPlayers();
                if (i > 0) {
                    if (i <= 5) {
                        this.bar.setName(EVENT_TEXT.copy().append(" - ").append(Text.translatable(HOSTILES_REMAINING_TRANSLATION_KEY, i)));
                    } else {
                        this.bar.setName(EVENT_TEXT);
                    }
                } else {
                    this.bar.setName(EVENT_TEXT);
                }
            }
        } else if (this.isFinished()) {
            PlayerData.getPlayerState(this.attachedPlayer).baseAssaultTimer = 0;
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
                this.invalidate();
                return;
            }
            if (this.finishCooldown % 20 == 0) {
                this.updateBarToPlayers();
                this.bar.setVisible(true);
                if (this.hasWon()) {
                    this.bar.setPercent(0.0f);
                    this.bar.setName(VICTORY_TITLE);
                    if (!this.winStatIncreased) {
                        this.attachedPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON));
                        this.winStatIncreased = true;
                        if (this.attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) >= 12){
                            dropXp();
                            generateNextWave();
                        }
                    }
                } else {
                    this.bar.setName(DEFEAT_TITLE);
                }
            }
        }
        int k = 0;
        while (this.preBaseAssaultTicks == 0 && getHostileCount() == 0) {
            float f = this.world.random.nextFloat();
            BlockPos pos1 = getSpawnLocation(f);
            BlockPos pos2 = getSpawnLocation(f);
            BlockPos pos3 = getSpawnLocation(f);
            if (pos1 != null && pos2 != null && pos3 != null) {
                this.started = true;
                ArrayList<HostileEntity> hostileList = new ArrayList<>();
                this.spawnWave(pos1, pos2, pos3, hostileList);
            } else {
                ++k;
            }
            if (k <= 5) continue;
            this.invalidate();
            break;
        }
    }

    protected void dropXp() {
            ExperienceOrbEntity.spawn(this.world, this.attachedPlayer.getPos().add(0,0.5,0), calcWaveSize(this.wave) * 5);
    }

    private void updateCenter() {
        BlockPos spawnPoint = this.attachedPlayer.getSpawnPointPosition();
        if(spawnPoint == null || !world.getBlockState(spawnPoint).isIn(BlockTags.BEDS)) this.findPlayerInsteadOfBed = true;
        else {
            this.center = spawnPoint;
            this.findPlayerInsteadOfBed = false;
        }
    }

    @Nullable
    private BlockPos getSpawnLocation(float f) {
        float i = 2.0f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int j = 0; j < 15; ++j) {
            float fl = (f + (this.world.random.nextFloat() * 0.40f)) * ((float)Math.PI * 2);
            int k = this.center.getX() + MathHelper.floor(MathHelper.cos(fl) * 32.0f * i) + this.world.random.nextInt(10);
            int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(fl) * 32.0f * i) + this.world.random.nextInt(10);
            int m = calculateSpawnY(k, l);
            mutable.set(k, m, l);
            i -= 0.07f;
            if (!this.world.isRegionLoaded(mutable.getX() - 10, mutable.getZ() - 10, mutable.getX() + 10, mutable.getZ() + 10) || !this.world.shouldTickEntity(mutable) || !SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, this.world, mutable, EntityType.SPIDER) && (!this.world.getBlockState((BlockPos)mutable.down()).isOf(Blocks.SNOW) || !this.world.getBlockState(mutable).isAir())) continue;
            return mutable;
        }
        return null;
    }

    private int calculateSpawnY(int x, int z){
        World world = this.world;
        int y = this.center.getY();
        BlockPos pos = new BlockPos (x, y + 36, z);
        while(!(world.getBlockState(pos.down()).isOpaqueFullCube(world, pos.down()) && world.getBlockState(pos).isReplaceable() && world.getBlockState(pos.up()).isReplaceable())){
            if(pos.getY() <= (y - 16)) break;
            pos = pos.down();
        }
        return pos.getY();
    }

    public int getId() {
        return this.id;
    }

    public int getHostileCount() {
        if(this.hostiles != null) {
            int i = 0;
            for (HostileEntity hostile : this.hostiles) {
                if (hostile != null && hostile.isAlive()) i++;
            }
            return i;
        }
        else return 0;
    }

    private int nextId() {
        return ++this.nextAvailableId;
    }

    private void spawnWave(BlockPos pos1, BlockPos pos2, BlockPos pos3, ArrayList<HostileEntity> list) {
        byte[] wave = this.wave;
        EntityType[] entityTypes = {EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SKELETON, EntityType.CREEPER, ModEntities.DIGGINGZOMBIE, ModEntities.LUMBERJACKZOMBIE,
                ModEntities.MINERZOMBIE, ModEntities.BUILDERZOMBIE, ModEntities.LEAPINGSPIDER, ModEntities.REEPER, ModEntities.SCORCHEDSKELETON};
        for(byte i = 0; i < 11; i++) {
            spawnTypeOfHostile(wave[i], entityTypes[i], pos1, pos2, pos3, list);
        }
        this.hostiles = list.toArray(new HostileEntity[list.size()]);
        this.totalHealth = getCurrentHostilesHealth();
        if(getHostileCount() > 0) this.waveSpawned = true;
        this.markDirty();
    }

    private BlockPos posDiceRoll(BlockPos pos1, BlockPos pos2, BlockPos pos3){
        int b = this.world.getRandom().nextInt(3);
        return switch (b) {
                case 0 -> pos1;
                case 1 -> pos2;
                case 2 -> pos3;
            default -> throw new IllegalStateException("Unexpected diceroll value: " + b);
        };
    }

    private void spawnTypeOfHostile(byte count, EntityType hostile, BlockPos pos1, BlockPos pos2, BlockPos pos3, ArrayList<HostileEntity> list){
        for(int i = 0; i < count; i++){
            addHostile((HostileEntity) hostile.create(this.world), posDiceRoll(pos1, pos2, pos3), list);
        }
    }

    public void addHostile(HostileEntity hostile, @Nullable BlockPos pos, ArrayList<HostileEntity> list) {
            if (pos != null) {
                IHostileEntityChanger hostile2 = (IHostileEntityChanger) hostile;
                hostile2.setBaseAssault(this);
                hostile.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                hostile.initialize(this.world, this.world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);
                hostile.setOnGround(true);
                this.world.spawnEntityAndPassengers(hostile);
                hostile2.getGoalSelector().add(5, new BaseAssaultGoal(hostile, 1.0));
                list.add(hostile);
                this.totalHealth += hostile.getHealth();
            }
        }

    private void markDirty() {
        IServerWorldChanger sworld = (IServerWorldChanger) this.world;
        sworld.getBaseAssaultManager().markDirty();
    }

    public void updateBar() {
        this.bar.setPercent(MathHelper.clamp(this.getCurrentHostilesHealth() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getCurrentHostilesHealth() {
        float f = 0.0f;
        for (HostileEntity hostile : this.hostiles) {
            if(hostile != null && hostile.isAlive()) f += hostile.getHealth();
        }
        return f;
    }

    public boolean isActive() {
        return this.active;
    }

    enum Status {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Status[] VALUES;

        static Status fromName(String name) {
            for (Status status : VALUES) {
                if (!name.equalsIgnoreCase(status.name())) continue;
                return status;
            }
            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        static {
            VALUES = Status.values();
        }
    }
}


