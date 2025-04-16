package survivalplus.modid.entity.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.ai.BuilderZombDestroyBedGoal;
import survivalplus.modid.entity.ai.movecontrols.BuilderZombieMoveControl;
import survivalplus.modid.entity.ai.pathing.BuilderZombieNavigation;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.ModGamerules;

import java.util.function.Predicate;

public class BuilderZombieEntity
        extends ZombieEntity {
    private static final TrackedData<Integer> ZOMBIE_TYPE = DataTracker.registerData(BuilderZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CONVERTING_IN_WATER = DataTracker.registerData(BuilderZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER = difficulty -> difficulty == Difficulty.HARD;
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    protected boolean canBreakDoors;
    protected int inWaterTime;
    protected int ticksUntilWaterConversion;
    protected int DirtPlaceCooldown = 0;

    public boolean hasTargetBed = false;

    public BlockPos targetBedPos;

    public BuilderZombieEntity(EntityType<? extends net.minecraft.entity.mob.ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.navigation = new BuilderZombieNavigation(this, this.getWorld());
        this.moveControl = new BuilderZombieMoveControl(this);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(4, new BuilderZombDestroyBedGoal(this, 1.0, 8));
        this.goalSelector.add(5, new DestroyEggGoal(this, 1.0, 3));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.initCustomGoals();
    }

    protected void initCustomGoals() {
        this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.FOLLOW_RANGE, 20.0).add(EntityAttributes.MOVEMENT_SPEED, 0.23f).add(EntityAttributes.ATTACK_DAMAGE, 3.0).add(EntityAttributes.ARMOR, 2.0).add(EntityAttributes.SPAWN_REINFORCEMENTS);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ZOMBIE_TYPE, 0);
        builder.add(CONVERTING_IN_WATER, false);
    }

    public boolean isConvertingInWater() {
        return this.getDataTracker().get(CONVERTING_IN_WATER);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public void tick() {
        this.hasTargetBed = this.targetBedPos != null;
        if (!this.getWorld().isClient && this.isAlive() && !this.isAiDisabled()) {
            if (this.isConvertingInWater()) {
                --this.ticksUntilWaterConversion;
                if (this.ticksUntilWaterConversion < 0) {
                    this.convertInWater();
                }
            } else if (this.canConvertInWater()) {
                if (this.isSubmergedIn(FluidTags.WATER)) {
                    ++this.inWaterTime;
                    if (this.inWaterTime >= 600) {
                        this.setTicksUntilWaterConversion(300);
                    }
                } else {
                    this.inWaterTime = -1;
                }
            }
        }
        super.tick();
    }

    @Override
    public void tickMovement() {
        if (this.isAlive()) {
            LivingEntity target = getTarget();
            IHostileEntityChanger bzomb = (IHostileEntityChanger) this;
            MinecraftServer server = this.getServer();
            if (server != null && server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.getMainHandStack().isOf(Items.DIRT) && DirtPlaceCooldown <= 0 && (target != null || this.hasTargetBed || bzomb.getBaseAssault() != null)) {
                World world = this.getWorld();
                if (calcDiffY() >= 0 && this.getNavigation().getCurrentPath() != null) {
                    BlockPos BlockUnder = this.getBlockPos().down();
                    if (canPlaceDirt(world, BlockUnder, BlockUnder.down())) {
                        this.placeDirtBlock(BlockUnder);
                    }
                }
            }
            DirtPlaceCooldown--;
        }
        super.tickMovement();
    }

    public int calcDiffY(){ // Calculates the height difference between the current and the next pathnode of the mob
        Path path = this.getNavigation().getCurrentPath();
        if(path == null || path.getCurrentNodeIndex() >= path.getLength()) return -1;
        if(path.getCurrentNodeIndex() > 0){
            int currentnodeposY = path.getCurrentNodePos().getY();
            int lastnodeposY = path.getNodePos(path.getCurrentNodeIndex() - 1).getY();

            return currentnodeposY - lastnodeposY;
        }
        else return -1;
    }

    private boolean canPlaceDirt (World world, BlockPos BlockUnder, BlockPos BlockUnder2){
        if(world.getBlockState(BlockUnder).isAir()){
            return world.getBlockState(BlockUnder2).isAir();
        }
        return world.getBlockState(BlockUnder).isReplaceable() && !world.getBlockState(BlockUnder).isAir();
    }

    public void placeDirtBlock(BlockPos bpos){
        World world = this.getWorld();
        this.swingHand(Hand.MAIN_HAND);
        world.setBlockState(bpos, Blocks.DIRT.getDefaultState());
        world.playSound(null, bpos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
        if(this.getMainHandStack().isOf(Items.DIRT)) this.getMainHandStack().decrement(1);
        DirtPlaceCooldown = 2;
    }

    private void setTicksUntilWaterConversion(int ticksUntilWaterConversion) {
        this.ticksUntilWaterConversion = ticksUntilWaterConversion;
        this.getDataTracker().set(CONVERTING_IN_WATER, true);
    }

    @Override
    public void setBaby(boolean baby) {
    }

    protected void convertInWater() {
    }

    protected boolean burnsInDaylight() {
        return false;
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int fullDaysRequired = 32;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return world.getServer() != null && (!world.getServer().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= fullDaysRequired || spawnReason != SpawnReason.NATURAL) && canSpawnInDark(type, world, spawnReason, pos, random);
    }

    protected void initEquipment() {
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("CanBreakDoors", this.canBreakDoors());
        nbt.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
        nbt.putInt("DrownedConversionTime", this.isConvertingInWater() ? this.ticksUntilWaterConversion : -1);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setCanBreakDoors(nbt.getBoolean("CanBreakDoors").get());
        this.inWaterTime = nbt.getInt("InWaterTime").get();
        if (nbt.contains("DrownedConversionTime") && nbt.getInt("DrownedConversionTime").get() > -1) {
            this.setTicksUntilWaterConversion(nbt.getInt("DrownedConversionTime").get());
        }
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        if (stack.isOf(Items.EGG) && this.isBaby() && this.hasVehicle()) {
            return false;
        }
        return super.canPickupItem(stack);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        entityData = super.initialize(world, difficulty, spawnReason, new ZombieData(false, false));
        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET, 1));
            this.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0F);
        }
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Blocks.DIRT, world.getRandom().nextBetween(8, 32)));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return entityData;
    }

    class DestroyEggGoal
            extends StepAndDestroyBlockGoal {
        DestroyEggGoal(PathAwareEntity mob, double speed, int maxYDifference) {
            super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
        }

        @Override
        public void tickStepping(WorldAccess world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5f, 0.9f + survivalplus.modid.entity.custom.BuilderZombieEntity.this.random.nextFloat() * 0.2f);
        }

        @Override
        public void onDestroyBlock(World world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
        }

        @Override
        public double getDesiredDistanceToTarget() {
            return 1.14;
        }
    }

}

