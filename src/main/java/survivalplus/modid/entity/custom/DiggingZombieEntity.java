package survivalplus.modid.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.ai.ActiveTargetGoalDestrZomb;
import survivalplus.modid.entity.ai.DestrZombDestroyBedGoal;
import survivalplus.modid.entity.ai.DestroyerZombAttackGoal;
import survivalplus.modid.entity.ai.movecontrols.DestroyerZombieMoveControl;
import survivalplus.modid.entity.ai.pathing.DestroyZombieNavigation;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.util.ModTags;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.function.Predicate;

public class DiggingZombieEntity
        extends ZombieEntity {
    private static final TrackedData<Integer> ZOMBIE_TYPE = DataTracker.registerData(DiggingZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CONVERTING_IN_WATER = DataTracker.registerData(DiggingZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER = difficulty -> difficulty == Difficulty.NORMAL;
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    private boolean canBreakDoors;
    public BlockPos targetBedPos;
    public static final TagKey<Block> BLOCKTAG = ModTags.Blocks.DIGGINGZOMBIE_MINABLE;
    public static final int defaultCooldown = 10;
    private int inWaterTime;
    private int ticksUntilWaterConversion;

    public DiggingZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.navigation = new DestroyZombieNavigation(this, this.getWorld());
        this.moveControl = new DestroyerZombieMoveControl(this);
    }



    @Override
    protected void initGoals() {
        this.goalSelector.add(4, new DestrZombDestroyBedGoal((HostileEntity)this, 1.0, 8));
        this.goalSelector.add(5, new DestroyEggGoal((PathAwareEntity)this, 1.0, 3));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.initCustomGoals();
    }

    protected void initCustomGoals() {
        this.goalSelector.add(2, new DestroyerZombAttackGoal(this, 1.0, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoalDestrZomb<>(this, PlayerEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0).add(EntityAttributes.GENERIC_ARMOR, 2.0).add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
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

    public void setCanBreakDoors(boolean canBreakDoors) {
        if (this.shouldBreakDoors() && NavigationConditions.hasMobNavigation(this)) {
            if (this.canBreakDoors != canBreakDoors) {
                this.canBreakDoors = canBreakDoors;
                ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
                if (canBreakDoors) {
                    this.goalSelector.add(1, this.breakDoorsGoal);
                } else {
                    this.goalSelector.remove(this.breakDoorsGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.remove(this.breakDoorsGoal);
            this.canBreakDoors = false;
        }
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    public void tick() {
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
            boolean bl;
            boolean bl2 = bl = this.burnsInDaylight() && this.isAffectedByDaylight();
            if (bl) {
                ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
                if (!itemStack.isEmpty()) {
                    if (itemStack.isDamageable()) {
                        itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                        if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                            this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                            this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                    bl = false;
                }
                if (bl) {
                    this.setOnFireFor(8);
                }
            }
        }
        super.tickMovement();
    }

    private void setTicksUntilWaterConversion(int ticksUntilWaterConversion) {
        this.ticksUntilWaterConversion = ticksUntilWaterConversion;
        this.getDataTracker().set(CONVERTING_IN_WATER, true);
    }

    @Override
    public void setBaby(boolean baby) {
    }

    @Override
    protected void convertInWater() {
    }


    @Override
    public boolean tryAttack(Entity target) {
        boolean bl = super.tryAttack(target);
        if (bl) {
            float f = this.getWorld().getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3f) {
                target.setOnFireFor(2 * (int)f);
            }
        }
        return bl;
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int FullDaysRequired = 12;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return (!world.getLevelProperties().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= FullDaysRequired) && canSpawnInDark(type, world, spawnReason, pos, random);
    }


    protected void initEquipment() {}

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
        this.setCanBreakDoors(nbt.getBoolean("CanBreakDoors"));
        this.inWaterTime = nbt.getInt("InWaterTime");
        if (nbt.contains("DrownedConversionTime", NbtElement.NUMBER_TYPE) && nbt.getInt("DrownedConversionTime") > -1) {
            this.setTicksUntilWaterConversion(nbt.getInt("DrownedConversionTime"));
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
    public boolean canGather(ItemStack stack) {
        if (stack.isOf(Items.GLOW_INK_SAC)) {
            return false;
        }
        return super.canGather(stack);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        float f = difficulty.getClampedLocalDifficulty();
        this.setCanPickUpLoot(random.nextFloat() < 0.55f * f);
        if (entityData == null) {
            entityData = new DiggingZombieEntity.ZombieData(DiggingZombieEntity.shouldBeBaby(random), false);
        }
        if (entityData instanceof DiggingZombieEntity.ZombieData) {
            DiggingZombieEntity.ZombieData zombieData = (DiggingZombieEntity.ZombieData)entityData;
            this.setCanBreakDoors(this.shouldBreakDoors() && random.nextFloat() < f * 0.1f);
            this.initEquipment();
        }

        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localDate = LocalDate.now();
            int i = localDate.get(ChronoField.DAY_OF_MONTH);
            int j = localDate.get(ChronoField.MONTH_OF_YEAR);
            if (j == 10 && i == 31 && random.nextFloat() < 0.25f) {
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0f;
            }
        }
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SHOVEL));
        this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 0.0f;
        this.applyAttributeModifiers(f);
        return entityData;
    }

    public static boolean shouldBeBaby(Random random) {
        return false;
    }


    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        ItemStack itemStack;
        CreeperEntity creeperEntity;
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        Entity entity = source.getAttacker();
        if (entity instanceof CreeperEntity && (creeperEntity = (CreeperEntity)entity).shouldDropHead() && !(itemStack = this.getSkull()).isEmpty()) {
            creeperEntity.onHeadDropped();
            this.dropStack(itemStack);
        }
    }


    class DestroyEggGoal
            extends StepAndDestroyBlockGoal {
        DestroyEggGoal(PathAwareEntity mob, double speed, int maxYDifference) {
            super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
        }

        @Override
        public void tickStepping(WorldAccess world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5f, 0.9f + DiggingZombieEntity.this.random.nextFloat() * 0.2f);
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

    public static class ZombieData
            implements EntityData {
        public final boolean baby;
        public final boolean tryChickenJockey;

        public ZombieData(boolean baby, boolean tryChickenJockey) {
            this.baby = baby;
            this.tryChickenJockey = tryChickenJockey;
        }
    }
}

