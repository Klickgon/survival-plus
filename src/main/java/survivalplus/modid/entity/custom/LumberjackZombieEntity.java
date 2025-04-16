package survivalplus.modid.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.ai.ActiveTargetGoalDestrZomb;
import survivalplus.modid.entity.ai.DestrZombDestroyBedGoal;
import survivalplus.modid.entity.ai.DestroyerZombAttackGoal;
import survivalplus.modid.entity.ai.movecontrols.DestroyerZombieMoveControl;
import survivalplus.modid.entity.ai.pathing.DestroyZombieNavigation;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.ModGamerules;
import survivalplus.modid.util.ModTags;

import java.util.function.Predicate;

public class LumberjackZombieEntity
        extends ZombieEntity {
    private static final TrackedData<Integer> ZOMBIE_TYPE = DataTracker.registerData(LumberjackZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CONVERTING_IN_WATER = DataTracker.registerData(LumberjackZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER = difficulty -> difficulty == Difficulty.EASY;
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    protected boolean canBreakDoors;
    public BlockPos targetBedPos;
    public static final TagKey<Block> BLOCKTAG = ModTags.Blocks.LUMBERJACKZOMBIE_MINABLE;
    public static final int defaultCooldown = 12;
    protected int freeingCooldown = 0;
    protected int inWaterTime;
    protected int ticksUntilWaterConversion;

    public LumberjackZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.navigation = new DestroyZombieNavigation(this, this.getWorld());
        this.moveControl = new DestroyerZombieMoveControl(this);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(4, new DestrZombDestroyBedGoal(this, 1.0, 8));
        this.goalSelector.add(5, new DestroyEggGoal(this, 1.0, 3));
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

    public static DefaultAttributeContainer.Builder createLumberZombieAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.FOLLOW_RANGE, 20.0).add(EntityAttributes.MOVEMENT_SPEED, 0.23f).add(EntityAttributes.ATTACK_DAMAGE, 2.25).add(EntityAttributes.ARMOR, 2.0).add(EntityAttributes.SPAWN_REINFORCEMENTS);
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
            MinecraftServer server = this.getServer();
            if(server != null && this.freeingCooldown <= 0 && this.getServer().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)){
                World world = this.getWorld();
                BlockPos pos = ((IHostileEntityChanger)this).getElevatedBlockPos();
                if(world.getBlockState(pos.up()).isIn(BLOCKTAG)){
                    this.swingHand(Hand.MAIN_HAND);
                    world.breakBlock(pos.up(), true);
                    this.freeingCooldown = defaultCooldown;
                }
                else if(world.getBlockState(pos).isIn(BLOCKTAG)){
                    this.swingHand(Hand.MAIN_HAND);
                    world.breakBlock(pos, true);
                    this.freeingCooldown = defaultCooldown;
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
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean bl;
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        DamageSource damageSource = this.getDamageSources().mobAttack(this);
        if (world instanceof ServerWorld) {
            f = EnchantmentHelper.getDamage(world, this.getWeaponStack(), target, damageSource, f) * 0.60f;
        }
        if (bl = target.damage(world, damageSource, f)) {
            World world2;
            float g = this.getAttackKnockbackAgainst(target, damageSource);
            if (g > 0.0f && target instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)target;
                livingEntity.takeKnockback(g * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            if ((world2 = this.getWorld()) instanceof ServerWorld) {
                ServerWorld serverWorld2 = (ServerWorld)world2;
                EnchantmentHelper.onTargetDamaged(serverWorld2, target, damageSource);
            }
            this.onAttacking(target);
            this.playAttackSound();
        }
        if (bl) {
            float l = this.getWorld().getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < l * 0.3f) {
                target.setOnFireFor(2 * (int)l);
            }
        }
        return bl;
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int fullDaysRequired = 21;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return world.getServer() != null && (!world.getServer().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= fullDaysRequired || spawnReason != SpawnReason.NATURAL) && canSpawnInDark(type, world, spawnReason, pos, random);
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
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        this.setCanBreakDoors(true);
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
        return entityData;
    }

    class DestroyEggGoal
            extends StepAndDestroyBlockGoal {
        DestroyEggGoal(PathAwareEntity mob, double speed, int maxYDifference) {
            super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
        }

        @Override
        public void tickStepping(WorldAccess world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5f, 0.9f + LumberjackZombieEntity.this.random.nextFloat() * 0.2f);
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

