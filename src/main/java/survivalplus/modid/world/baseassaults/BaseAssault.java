/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.world.baseassaults;

import com.google.common.collect.Sets;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.ModEntities;
import survivalplus.modid.entity.ai.BaseAssaultGoal;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.IServerPlayerChanger;
import survivalplus.modid.util.IServerWorldChanger;
import survivalplus.modid.util.ModPlayerStats;

import java.util.*;
import java.util.function.Predicate;

public class BaseAssault {

    private static final Text EVENT_TEXT = Text.translatable("event.survival-plus.baseassault");
    private static final Text VICTORY_TITLE = Text.translatable("event.survival-plus.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.survival-plus.defeat.full");
    private static final String HOSTILES_REMAINING_TRANSLATION_KEY = "event.survival-plus.baseassault.hostiles_remaining";
    private final ArrayList<HostileEntity> hostiles = new ArrayList<>();
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    public final ServerPlayerEntity attachedPlayer;
    private boolean started;
    private final int id;
    private float totalHealth;
    private boolean active;
    private final byte[] wave;
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
        this.world = world;
        this.attachedPlayer = attachedPlayer;
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
        this.world = world;
        this.attachedPlayer = (ServerPlayerEntity) world.getEntity(nbt.getUuid("attachedplayer"));
        this.id = nbt.getInt("Id");
        this.started = nbt.getBoolean("Started");
        this.active = nbt.getBoolean("Active");
        this.ticksActive = nbt.getLong("TicksActive");
        this.preBaseAssaultTicks = nbt.getInt("PreRaidTicks");
        this.totalHealth = nbt.getFloat("TotalHealth");
        this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
        this.status = Status.fromName(nbt.getString("Status"));
        this.waveSpawned = nbt.getBoolean("wavespawned");
        this.wave = getWave(attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) + 1);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", this.id);
        nbt.putBoolean("Started", this.started);
        nbt.putBoolean("Active", this.active);
        nbt.putLong("TicksActive", this.ticksActive);
        nbt.putInt("PreRaidTicks", this.preBaseAssaultTicks);
        nbt.putFloat("TotalHealth", this.totalHealth);
        nbt.putString("Status", this.status.getName());
        nbt.putInt("CX", this.center.getX());
        nbt.putInt("CY", this.center.getY());
        nbt.putInt("CZ", this.center.getZ());
        nbt.putUuid("attachedplayer", this.attachedPlayer.getUuid());
        nbt.putBoolean("wavespawned", this.waveSpawned);
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

    private byte @Nullable [] getGeneratedWave(){
        return ((IServerPlayerChanger)this.attachedPlayer).getGeneratedWave();
    }

    private void generateNextWave(){
        byte[] wave = getGeneratedWave();
        if(calcSumArray(wave) < 45){ // checks if the generated wave has less than 45 mobs in it to increment the count of a random mob
            byte randomIndex = (byte) Math.rint(Math.random() * 10); // to increment the count of a random mob for the next wave
            ++wave[randomIndex];
        }
        else { // if the wave has 45 mobs, one random mob gets replaced with a different one
            byte randomIndex1 = (byte) Math.rint(Math.random() * 10);
            --wave[randomIndex1];
            byte randomIndex2 = (byte) Math.rint(Math.random() * 10);
            ++wave[randomIndex2];
        }
        ((IServerPlayerChanger)this.attachedPlayer).setGeneratedWave(wave);
    }

    private int calcSumArray(byte[] array){
        int sum = 0;
        for(byte num : array){
            sum += num;
        }
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
            if(this.attachedPlayer.getHealth() <= 0 && !this.world.getBlockState(this.center).isIn(BlockTags.BEDS)){
                this.status = Status.LOSS;
            }
            updateCenter();
            if(this.waveSpawned && getCurrentHostilesHealth() <= 0){
                this.status = Status.VICTORY;
            }
            if (this.ticksActive >= 48000L) {
                this.invalidate();
            }
            int i = this.getHostileCount();
            if (i == 0) {
                if (this.preBaseAssaultTicks > 0) {
                    if (this.preBaseAssaultTicks == 300 || this.preBaseAssaultTicks % 20 == 0) {
                        this.updateBarToPlayers();
                    }
                    --this.preBaseAssaultTicks;
                    this.bar.setPercent(MathHelper.clamp((float)(300 - this.preBaseAssaultTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.preBaseAssaultTicks == 0 && this.hasStarted()) {
                    this.preBaseAssaultTicks = 300;
                    this.bar.setName(EVENT_TEXT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                if(this.waveSpawned) updateBar();
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
            this.attachedPlayer.resetStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.TIME_SINCE_LAST_BASEASSAULT));
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
                    if(!this.winStatIncreased) {
                        this.attachedPlayer.incrementStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON));
                        this.winStatIncreased = true;
                        if(attachedPlayer.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(ModPlayerStats.BASEASSAULTS_WON)) + 1 >= 12)
                            generateNextWave();
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
                this.spawnWave(pos1, pos2, pos3);
            } else {
                ++k;
            }
            if (k <= 5) continue;
            this.invalidate();
            break;
        }
    }

    private void updateCenter() {
        Optional<Vec3d> op = PlayerEntity.findRespawnPosition(this.world, this.center,0.0f, false, true);
        if(op.isPresent()) {
            this.center = this.attachedPlayer.getSpawnPointPosition();
            this.findPlayerInsteadOfBed = !this.world.getBlockState(this.center).isIn(BlockTags.BEDS);
        }
        else this.findPlayerInsteadOfBed = true;

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
        while(world.getBlockState(pos.down()).isReplaceable() || !world.getBlockState(pos).isReplaceable() || !world.getBlockState(pos.up()).isAir()){
            if(pos.getY() <= (y - 16)) break;
            pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        }
        return pos.getY();
    }

    public int getId() {
        return this.id;
    }

    public int getHostileCount() {
        ArrayList<HostileEntity> hostileList = this.hostiles;
        HostileEntity[] hostileArray = hostileList.toArray(new HostileEntity[hostileList.size()]);
        int i = 0;
        for(HostileEntity hostileEntity : hostileArray){
            if(hostileEntity.isAlive()) i++;
        }
        return i;
    }

    private int nextId() {
        return ++this.nextAvailableId;
    }

    private void spawnWave(BlockPos pos1, BlockPos pos2, BlockPos pos3) {
        byte[] wave = this.wave;
        spawnTypeOfHostile(wave[0], EntityType.ZOMBIE, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[1], EntityType.SPIDER, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[2], EntityType.SKELETON, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[3], EntityType.CREEPER, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[4], ModEntities.DIGGINGZOMBIE, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[5], ModEntities.LUMBERJACKZOMBIE, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[6], ModEntities.MINERZOMBIE, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[7], ModEntities.BUILDERZOMBIE, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[8], ModEntities.LEAPINGSPIDER, pos1, pos2, pos3);;
        spawnTypeOfHostile(wave[9], ModEntities.REEPER, pos1, pos2, pos3);
        spawnTypeOfHostile(wave[10], ModEntities.SCORCHEDSKELETON, pos1, pos2, pos3);
        this.totalHealth = getCurrentHostilesHealth();
        if(getHostileCount() > 0) this.waveSpawned = true;
        this.markDirty();
    }

    private static BlockPos posDiceRoll(BlockPos pos1, BlockPos pos2, BlockPos pos3){
        byte b = (byte) (Math.rint(Math.random() * 2) + 1);
        return switch (b) {
                case 1 -> pos1;
                case 2 -> pos2;
                case 3 -> pos3;
            default -> throw new IllegalStateException("Unexpected diceroll value: " + b);
        };
    }

    private void spawnTypeOfHostile(short count, EntityType hostile, BlockPos pos1, BlockPos pos2, BlockPos pos3){
        for(int i = 0; i < count; i++){
            addHostile((HostileEntity) hostile.create(this.world), posDiceRoll(pos1, pos2, pos3));
        }
    }


    public void addHostile(HostileEntity hostile, @Nullable BlockPos pos) {
        IHostileEntityChanger hostile2 = (IHostileEntityChanger) hostile;
        hostile2.setBaseAssault(this);
            if (pos != null) {
                hostile.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                hostile.initialize(this.world, this.world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);
                hostile.setOnGround(true);
                hostile2.getGoalSelector().add(5, new BaseAssaultGoal(hostile, 1.0));
                this.world.spawnEntityAndPassengers(hostile);
                this.hostiles.add(hostile);
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
        HostileEntity[] hostilesArray = this.hostiles.toArray(new HostileEntity[this.hostiles.size()]);
        for (HostileEntity hostile : hostilesArray) {
            if(!hostile.isAlive()) f += 0.0f;
            else f += hostile.getHealth();
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


