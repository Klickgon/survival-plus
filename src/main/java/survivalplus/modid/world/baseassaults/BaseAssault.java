/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.world.baseassaults;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.PlayerData;
import survivalplus.modid.SurvivalPlus;
import survivalplus.modid.entity.ModEntities;
import survivalplus.modid.entity.ai.BaseAssaultGoal;
import survivalplus.modid.sounds.ModSounds;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModPlayerStats;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Predicate;

public class BaseAssault {

    private static final Text EVENT_TEXT = Text.translatable("event.survival-plus.baseassault");
    private static final Text VICTORY_TITLE = Text.translatable("event.survival-plus.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.survival-plus.defeat.full");
    private static final String HOSTILES_REMAINING_TRANSLATION_KEY = "event.survival-plus.baseassault.hostiles_remaining";
    public static final MapCodec<BaseAssault> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.BOOL.fieldOf("BAStarted").forGetter(baseAssault -> baseAssault.started),
                            Codec.BOOL.fieldOf("BAActive").forGetter(baseAssault -> baseAssault.active),
                            Codec.LONG.fieldOf("BATicksActive").forGetter(baseAssault -> baseAssault.ticksActive),
                            Codec.INT.fieldOf("BAPreAssaultTicks").forGetter(baseAssault -> baseAssault.preBaseAssaultTicks),
                            Codec.FLOAT.fieldOf("BATotalHealth").forGetter(baseAssault -> baseAssault.totalHealth),
                            BlockPos.CODEC.optionalFieldOf("Center").forGetter(baseAssault -> Optional.ofNullable(baseAssault.center)),
                            BaseAssault.Status.CODEC.fieldOf("Status").forGetter(baseAssault -> baseAssault.status),
                            Codec.BOOL.fieldOf("WaveSpawned").forGetter(baseAssault -> baseAssault.waveSpawned),
                            Codec.BOOL.fieldOf("WinStatIncreased").forGetter(baseAssault -> baseAssault.winStatIncreased),
                            Codec.BYTE_BUFFER.fieldOf("Wave").forGetter(baseAssault -> ByteBuffer.wrap(baseAssault.wave)),
                            Codec.INT.fieldOf("HostileCount").forGetter(baseAssault -> baseAssault.getHostileCount()),
                            Codec.BOOL.fieldOf("startSoundPlayed").forGetter(baseAssault -> baseAssault.startSoundPlayed),
                            Uuids.SET_CODEC.fieldOf("hostileIDs").forGetter(baseAssault -> new HashSet<>(baseAssault.hostileIDs)),
                            Uuids.CODEC.fieldOf("playerID").forGetter(baseAssault -> baseAssault.playerID),
                            Codec.INT.fieldOf("BAId").forGetter(baseAssault -> baseAssault.id)
                    ).apply(instance, BaseAssault::new)
    );

    private int requiredHostileCount = 0;
    private HostileEntity[] hostiles;
    private LinkedList<UUID> hostileIDs = new LinkedList<>();
    private final boolean isFromNBT;
    private long ticksActive;
    private BlockPos center;
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
    private boolean startSoundPlayed = false;
    public boolean findPlayerInsteadOfBed;
    private static final int REQUIRED_WAVE_LENGTH = 12;
    private static final int MAX_MOB_COUNT = 4;
    private static final int MAX_WAVE_SIZE = 32;

    public BaseAssault(int id, BlockPos pos, ServerPlayerEntity attachedPlayer) {
        this.id = id;
        this.isFromNBT = false;
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

    public BaseAssault(boolean started, boolean active, long ticksActive, int preBaseAssaultTicks,
                       float totalHealth, Optional<BlockPos> center, BaseAssault.Status status, boolean waveSpawned,
                       boolean winStatIncreased, ByteBuffer wave, int requiredHostileCount,
                       boolean startSoundPlayed, Set<UUID> hostileIDs, UUID playerID, int id) {
        this.isFromNBT = true;
        this.id = id;
        this.started = started;
        this.playerID = playerID;
        this.active = active;
        this.ticksActive = ticksActive;
        this.preBaseAssaultTicks = preBaseAssaultTicks;
        this.totalHealth = totalHealth;
        this.center = center.orElseThrow();
        this.status = status;
        this.waveSpawned = waveSpawned;
        this.winStatIncreased = winStatIncreased;
        this.wave = wave.array();
        this.requiredHostileCount = requiredHostileCount;
        this.startSoundPlayed = startSoundPlayed;
        this.hostileIDs = new LinkedList<>(hostileIDs);
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

    private void generateNextWave(ServerWorld world){
        byte[] wave = getGeneratedWave();
        Random random = world.getRandom();
        if(calcWaveSize(wave) < MAX_WAVE_SIZE) { // checks if the generated wave has less than 35 mobs in it
            SurvivalPlus.LOGGER.info("{}'s generated wave size is below 35, incrementing it.", this.attachedPlayer.getName().getString());
            int randomIndex;
            if(random.nextBoolean()) {
                randomIndex = 4 + random.nextInt(REQUIRED_WAVE_LENGTH - 4); // to increment the count of one of the powerful mobs
                if(wave[randomIndex] > MAX_MOB_COUNT) randomIndex = random.nextInt(4);
            }
            else
                randomIndex = random.nextInt(4); // to increment the count of one of the standard mobs
            wave[randomIndex]++;
        }
        else { // if the wave has 35 mobs, one random mob gets replaced with a different one
            SurvivalPlus.LOGGER.info("{}'s generated wave size is 35, shuffling.", this.attachedPlayer.getName().getString());
            int randomIndex1 = random.nextInt(REQUIRED_WAVE_LENGTH);;
            wave[randomIndex1]--;
            int randomIndex2;
            do randomIndex2 = random.nextInt(REQUIRED_WAVE_LENGTH);
            while (randomIndex1 == randomIndex2 || wave[randomIndex2] > MAX_MOB_COUNT);
            wave[randomIndex2]++;
        }
        SurvivalPlus.LOGGER.info("{}'s new generated Wave: {}", this.attachedPlayer.getName().getString(), Arrays.toString(wave));
        PlayerData.getPlayerState(this.attachedPlayer).generatedWave = wave;
    }

    // Checks if one the powerful mobs count in the wave bigger than max
    private static boolean isOnePowerfulMobCountOverMax(byte[] wave, int max){
        for (int i = 4; i < wave.length; i++){
            if (wave[i] > max) return true;
        }
        return false;
    }

    private static int calcWaveSize(byte[] array){
        int sum = 0;
        for(byte num : array) sum += num;
        return sum;
    }

    public boolean hasStarted() {
        return this.started;
    }


    private Predicate<ServerPlayerEntity> isInBaseAssaultDistance(ServerWorld world) {
        return player -> {
            IServerWorldChanger sworld = (IServerWorldChanger) world;
            BlockPos bpos = player.getBlockPos();
            return player.isAlive() && sworld.getBaseAssaultAt(bpos) == this;
        };
    }

    public BlockPos getCenter(){
        return this.center;
    }

    private void updateBarToPlayers(ServerWorld world) {
        HashSet<ServerPlayerEntity> set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = world.getPlayers(this.isInBaseAssaultDistance(world));
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

    public void start(PlayerEntity player, ServerWorld world) {
        Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_WITHOUT_CUSTOM_RESPAWNPOINT);
        StatHandler handler = player.getServer().getPlayerManager().getPlayer(player.getUuid()).getStatHandler();
        handler.setStat(player, stat, Math.max(0, handler.getStat(stat) - 72000));
        if(!world.isClient && !startSoundPlayed){
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
            world.playSound(null, attachedPlayer.getBlockPos().add(x, 1, z), ModSounds.BASE_ASSAULT_START, SoundCategory.HOSTILE, 1.0f, 1.0f);
            startSoundPlayed = true;
        }
    }

    public void invalidate() {
        if(this.attachedPlayer != null)
            PlayerData.getPlayerState(this.attachedPlayer).receivedBAWarningMessage = false;
        if(this.hostiles != null){
            for(HostileEntity hostile : this.hostiles)
                if(hostile != null)
                    ((IHostileEntityChanger)hostile).setBaseAssault(null);
        }
        this.active = false;
        this.bar.clearPlayers();
        this.status = Status.STOPPED;
    }

    public void tick(ServerWorld world) {
        if(this.isFromNBT){
            if(this.attachedPlayer == null) {
                this.attachedPlayer = world.getServer().getPlayerManager().getPlayer(this.playerID);
                if(!world.getServer().getPlayerManager().getPlayerList().isEmpty() && this.attachedPlayer == null) invalidate();
                return;
            }
            if(this.wave == null){
                this.wave = getWave(attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) + 1);
                return;
            }
            if(this.hostiles == null && this.waveSpawned){
                ArrayList<HostileEntity> hostileList = new ArrayList<>();
                for (UUID uuid : this.hostileIDs) {
                    HostileEntity hostile = (HostileEntity) world.getEntity(uuid);
                    if(hostile != null) hostileList.add(hostile);
                }
                if(hostileList.size() < this.requiredHostileCount) return;
                this.hostiles = hostileList.toArray(new HostileEntity[hostileList.size()]);
                this.hostileIDs = new LinkedList<>();
                for (HostileEntity hostile : this.hostiles) {
                        IHostileEntityChanger hostile2 = (IHostileEntityChanger) hostile;
                        hostile2.setBaseAssault(this);
                        if(hostile instanceof WitchEntity) hostile2.getGoalSelector().add(3, new BaseAssaultGoal(hostile, 1.0));
                        else hostile2.getGoalSelector().add(5, new BaseAssaultGoal(hostile, 1.0));
                        this.hostileIDs.add(hostile.getUuid());
                }
            }
        }
        if (this.hasStopped()) {
            return;
        }
        if (this.status == Status.ONGOING) {
            boolean bl = this.active;
            this.active = world.isChunkLoaded(this.center);
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
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
            if (!this.attachedPlayer.isAlive() && this.attachedPlayer.getRespawnTarget(false, TeleportTarget.NO_OP).missingRespawnBlock()) {
                this.status = Status.LOSS;
            }
            updateCenter(world);
            if (this.waveSpawned && getCurrentHostilesHealth() <= 0) {
                this.status = Status.VICTORY;
            }
            if (this.ticksActive >= 48000L) {
                this.invalidate();
            }
            if (this.ticksActive >= 2000L && getHostileCount() <= 5) {
                for(HostileEntity hostile : this.hostiles){
                    if(hostile != null) hostile.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 3));
                }
            }
            int i = this.getHostileCount();
            if (i == 0) {
                if (this.preBaseAssaultTicks > 0) {
                    if (this.preBaseAssaultTicks == 300 || this.preBaseAssaultTicks % 20 == 0) {
                        this.updateBarToPlayers(world);
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
                this.updateBarToPlayers(world);
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
                this.updateBarToPlayers(world);
                this.bar.setVisible(true);
                if (this.hasWon()) {
                    this.bar.setPercent(0.0f);
                    this.bar.setName(VICTORY_TITLE);
                    if (!this.winStatIncreased) {
                        this.attachedPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON));
                        this.winStatIncreased = true;
                        if (this.attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) >= 12){
                            dropXp(world);
                            generateNextWave(world);
                        }
                    }
                } else {
                    this.bar.setName(DEFEAT_TITLE);
                }
            }
        }
        int k = 0;
        while (this.preBaseAssaultTicks == 0 && getHostileCount() == 0) {
            float f = world.random.nextFloat();
            BlockPos pos1 = getSpawnLocation(f,world);
            BlockPos pos2 = getSpawnLocation(f, world);
            BlockPos pos3 = getSpawnLocation(f, world);
            if (pos1 != null && pos2 != null && pos3 != null) {
                this.started = true;
                ArrayList<HostileEntity> hostileList = new ArrayList<>();
                this.spawnWave(pos1, pos2, pos3, hostileList, world);
            } else {
                ++k;
            }
            if (k <= 5) continue;
            this.invalidate();
            break;
        }
    }

    protected void dropXp(ServerWorld world) {
        ExperienceOrbEntity.spawn(world, this.attachedPlayer.getPos().add(0,0.5,0), calcWaveSize(this.wave) * 5);
    }

    private void updateCenter(ServerWorld world) {
        BlockPos spawnPoint = ((IServerPlayerChanger)this.attachedPlayer).getMainSpawnPoint();
        if(spawnPoint == null || !world.getBlockState(spawnPoint).isIn(BlockTags.BEDS)) this.findPlayerInsteadOfBed = true;
        else {
            this.center = spawnPoint;
            this.findPlayerInsteadOfBed = false;
        }
    }

    @Nullable
    private BlockPos getSpawnLocation(float f, ServerWorld world) {
        float i = 2.5f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int j = 0; j < 30; ++j) {
            float fl = (f + (world.random.nextFloat() * 0.40f)) * ((float)Math.PI * 2);
            int k = this.center.getX() + MathHelper.floor(MathHelper.cos(fl) * 32.0f * i) + world.random.nextInt(10);
            int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(fl) * 32.0f * i) + world.random.nextInt(10);
            int m = calculateSpawnY(k, l, world);
            mutable.set(k, m, l);
            i -= 0.03f;
            if (!world.isRegionLoaded(mutable.getX() - 10, mutable.getZ() - 10, mutable.getX() + 10, mutable.getZ() + 10) || !world.shouldTickBlockAt(mutable) || !SpawnRestriction.getLocation(EntityType.RAVAGER).isSpawnPositionOk(world, mutable, EntityType.RAVAGER) && (!world.getBlockState(mutable.down()).isOf(Blocks.SNOW) || !world.getBlockState(mutable).isAir()) || this.isInVillage(mutable, world)) continue;
            return mutable;
        }
        return null;
    }

    private boolean isInVillage(BlockPos pos, ServerWorld world){
        BlockPos villagePos = world.locateStructure(StructureTags.VILLAGE, pos, 10,false);
        if(villagePos == null) return false;
        int x = villagePos.getX() - pos.getX();
        int z = villagePos.getZ() - pos.getZ();
        return (x * x + z * z) < 3000 && world.locateStructure(StructureTags.VILLAGE, pos, 0,false) != null;
    }

    private int calculateSpawnY(int x, int z, ServerWorld world){
        int y = this.center.getY();
        BlockPos pos = new BlockPos.Mutable (x, y + 36, z);
        while(!(world.getBlockState(pos.down()).isOpaqueFullCube() && world.getBlockState(pos).isReplaceable() && world.getBlockState(pos.up()).isReplaceable())){
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

    private void spawnWave(BlockPos pos1, BlockPos pos2, BlockPos pos3, ArrayList<HostileEntity> list, ServerWorld world) {
        if(this.wave.length < REQUIRED_WAVE_LENGTH || isOnePowerfulMobCountOverMax(this.wave, MAX_MOB_COUNT) || calcWaveSize(wave) > MAX_WAVE_SIZE)
            this.wave = this.updateWave(this.wave, world);
        byte[] wave = this.wave;
        EntityType[] entityTypes = {EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SKELETON, EntityType.CREEPER, ModEntities.DIGGINGZOMBIE, ModEntities.LUMBERJACKZOMBIE,
                ModEntities.MINERZOMBIE, ModEntities.BUILDERZOMBIE, ModEntities.LEAPINGSPIDER, ModEntities.REEPER, ModEntities.SCORCHEDSKELETON, EntityType.WITCH};
        for(byte i = 0; i < REQUIRED_WAVE_LENGTH; i++) {
            for(byte j = 0; j < wave[i]; j++){
                addHostile((HostileEntity) entityTypes[i].create(world, SpawnReason.EVENT), posDiceRoll(pos1, pos2, pos3, world), list, world);
            }
        }
        this.hostiles = list.toArray(new HostileEntity[list.size()]);
        this.totalHealth = getCurrentHostilesHealth();
        if(getHostileCount() > 0) this.waveSpawned = true;
        this.markDirty(world);
    }

    private byte[] updateWave(byte[] wave, ServerWorld world){
        byte[] output = BaseAssaultWaves.BASEASSAULT_TWELVE;
        System.arraycopy(wave, 0, output, 0, wave.length); // Copy the outdated array to the new array format
        {
            int pool = 0;
            for(int i = 4; i < REQUIRED_WAVE_LENGTH; i++){ // Sets all powerful mob counts to five if over
                if (output[i] > MAX_MOB_COUNT){
                    pool += output[i] - MAX_MOB_COUNT;
                    output[i] = MAX_MOB_COUNT;
                }
            }
            int index;
            while(pool > 0){ // redistributes the removed counts to the standard mobs
                index = world.getRandom().nextInt(4);
                if(output[index] < MAX_MOB_COUNT) output[index]++;
                pool--;
            }
        }
        int overhang = calcWaveSize(output) - MAX_WAVE_SIZE;
        while(overhang-- > 0){
            output[world.getRandom().nextInt(4)]--;
        }
        SurvivalPlus.LOGGER.info("Incompatible wave detected and updated: {} with length {}", Arrays.toString(output), output.length);
        PlayerData.getPlayerState(this.attachedPlayer).generatedWave = output;
        return output;
    }

    private BlockPos posDiceRoll(BlockPos pos1, BlockPos pos2, BlockPos pos3, ServerWorld world){
        int b = world.getRandom().nextInt(3);
        return switch (b) {
                case 0 -> pos1;
                case 1 -> pos2;
                case 2 -> pos3;
            default -> throw new IllegalStateException("Unexpected diceroll value: " + b);
        };
    }

    private void spawnTypeOfHostile(byte count, EntityType hostile, BlockPos pos1, BlockPos pos2, BlockPos pos3, ArrayList<HostileEntity> list){

    }

    public void addHostile(HostileEntity hostile, @Nullable BlockPos pos, ArrayList<HostileEntity> list, ServerWorld world) {
            if (pos != null) {
                IHostileEntityChanger hostile2 = (IHostileEntityChanger) hostile;
                hostile2.setBaseAssault(this);
                hostile.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                hostile.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
                hostile.setOnGround(true);
                world.spawnEntityAndPassengers(hostile);
                if(hostile instanceof WitchEntity) hostile2.getGoalSelector().add(3, new BaseAssaultGoal(hostile, 1.0));
                else hostile2.getGoalSelector().add(5, new BaseAssaultGoal(hostile, 1.0));
                list.add(hostile);
                this.hostileIDs.add(hostile.getUuid());
                this.totalHealth += hostile.getHealth();
            }
        }

    private void markDirty(ServerWorld world) {
        IServerWorldChanger sworld = (IServerWorldChanger) world;
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

    public int getCurrentWaveSize(){
        if(wave == null) return 0;
        return calcWaveSize(wave);
    }

    public boolean isActive() {
        return this.active;
    }

    enum Status implements StringIdentifiable {
        ONGOING("ongoing"),
        VICTORY("victory"),
        LOSS("loss"),
        STOPPED("stopped");

        public static final Codec<BaseAssault.Status> CODEC = StringIdentifiable.createCodec(BaseAssault.Status::values);
        private final String id;

        private Status(final String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }
    }
}


