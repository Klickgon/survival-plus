package survivalplus.modid.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.ai.ActiveTargetGoalDestrZomb;
import survivalplus.modid.entity.ai.DestrZombDestroyBedGoal;
import survivalplus.modid.entity.ai.pathing.DestroyZombieNavigation;
import survivalplus.modid.util.ModGamerules;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.UUID;
import java.util.function.Predicate;

public class LumberjackZombieEntity
        extends ZombieEntity {
    private static final UUID BABY_SPEED_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final EntityAttributeModifier BABY_SPEED_BONUS = new EntityAttributeModifier(BABY_SPEED_ID, "Baby speed boost", 0.5, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    private static final TrackedData<Boolean> BABY = DataTracker.registerData(LumberjackZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ZOMBIE_TYPE = DataTracker.registerData(LumberjackZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CONVERTING_IN_WATER = DataTracker.registerData(LumberjackZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER = difficulty -> difficulty == Difficulty.EASY;
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    private boolean canBreakDoors;
    public BlockPos targetBedPos;
    public static final TagKey<Block> BLOCKTAG = BlockTags.AXE_MINEABLE;
    private int inWaterTime;
    private int ticksUntilWaterConversion;

    public LumberjackZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.navigation = new DestroyZombieNavigation((ZombieEntity) this, this.getWorld());
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
        this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoalDestrZomb<PlayerEntity>((MobEntity)this, PlayerEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<MerchantEntity>((MobEntity)this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<IronGolemEntity>((MobEntity)this, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createLumberZombieAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23f).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.25).add(EntityAttributes.GENERIC_ARMOR, 2.0).add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(BABY, false);
        this.getDataTracker().startTracking(ZOMBIE_TYPE, 0);
        this.getDataTracker().startTracking(CONVERTING_IN_WATER, false);
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
    public boolean isBaby() {
        return this.getDataTracker().get(BABY);
    }

    @Override
    public int getXpToDrop() {
        if (this.isBaby()) {
            this.experiencePoints = (int)((double)this.experiencePoints * 2.5);
        }
        return super.getXpToDrop();
    }

    @Override
    public void setBaby(boolean baby) {
        this.getDataTracker().set(BABY, baby);
        if (this.getWorld() != null && !this.getWorld().isClient) {
            EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            entityAttributeInstance.removeModifier(BABY_SPEED_BONUS.getId());
            if (baby) {
                entityAttributeInstance.addTemporaryModifier(BABY_SPEED_BONUS);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (BABY.equals(data)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(data);
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



    protected void convertInWater() {
    }


    public void convertTo(EntityType<? extends ZombieEntity> entityType) {
        LumberjackZombieEntity zombieEntity = (LumberjackZombieEntity) this.convertTo(entityType, true);
        if (zombieEntity != null) {
            zombieEntity.applyAttributeModifiers(zombieEntity.getWorld().getLocalDifficulty(zombieEntity.getBlockPos()).getClampedLocalDifficulty());
            zombieEntity.setCanBreakDoors(zombieEntity.shouldBreakDoors() && this.canBreakDoors());
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!super.damage(source, amount)) {
            return false;
        }
        if (!(this.getWorld() instanceof ServerWorld)) {
            return false;
        }
        ServerWorld serverWorld = (ServerWorld)this.getWorld();
        LivingEntity livingEntity = this.getTarget();
        if (livingEntity == null && source.getAttacker() instanceof LivingEntity) {
            livingEntity = (LivingEntity)source.getAttacker();
        }
        if (livingEntity != null && this.getWorld().getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttributeValue(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS) && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ());
            ZombieEntity zombieEntity = new ZombieEntity(this.getWorld());
            for (int l = 0; l < 50; ++l) {
                int m = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                int n = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                int o = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                BlockPos blockPos = new BlockPos(m, n, o);
                EntityType<?> entityType = zombieEntity.getType();
                SpawnRestriction.Location location = SpawnRestriction.getLocation(entityType);
                if (!SpawnHelper.canSpawn(location, this.getWorld(), blockPos, entityType) || !SpawnRestriction.canSpawn(entityType, serverWorld, SpawnReason.REINFORCEMENT, blockPos, this.getWorld().random)) continue;
                zombieEntity.setPosition(m, n, o);
                if (this.getWorld().isPlayerInRange(m, n, o, 7.0) || !this.getWorld().doesNotIntersectEntities(zombieEntity) || !this.getWorld().isSpaceEmpty(zombieEntity) || this.getWorld().containsFluid(zombieEntity.getBoundingBox())) continue;
                zombieEntity.setTarget(livingEntity);
                zombieEntity.initialize(serverWorld, this.getWorld().getLocalDifficulty(zombieEntity.getBlockPos()), SpawnReason.REINFORCEMENT, null, null);
                serverWorld.spawnEntityAndPassengers(zombieEntity);
                this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Zombie reinforcement caller charge", -0.05f, EntityAttributeModifier.Operation.ADDITION));
                zombieEntity.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Zombie reinforcement callee charge", -0.05f, EntityAttributeModifier.Operation.ADDITION));
                break;
            }
        }
        return true;
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl;
        int i;
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float g = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
            g += (float)EnchantmentHelper.getKnockback(this);
        }
        if ((i = EnchantmentHelper.getFireAspect(this)) > 0) {
            target.setOnFireFor(i * 4);
        }
        bl = target.damage(this.getDamageSources().mobAttack(this), f * 0.60f);
        if (bl) {
            if (g > 0.0f && target instanceof LivingEntity) {
                ((LivingEntity)target).takeKnockback(g * 0.5f, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            if (target instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity)target;
                this.disablePlayerShield(playerEntity, this.getMainHandStack(), playerEntity.isUsingItem() ? playerEntity.getActiveItem() : ItemStack.EMPTY);
            }
            this.applyDamageEffects(this, target);
            this.onAttacking(target);
        }
        if (bl) {
            float d = this.getWorld().getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < d * 0.3f) {
                target.setOnFireFor(2 * (int)d);
            }
        }
        return bl;
    }

    private void disablePlayerShield(PlayerEntity player, ItemStack mobStack, ItemStack playerStack) {
        if (!mobStack.isEmpty() && !playerStack.isEmpty() && mobStack.getItem() instanceof AxeItem && playerStack.isOf(Items.SHIELD)) {
            float f = 0.25f + (float)EnchantmentHelper.getEfficiency(this) * 0.05f;
            if (this.random.nextFloat() < f) {
                player.getItemCooldownManager().set(Items.SHIELD, 100);
                this.getWorld().sendEntityStatus(player, EntityStatuses.BREAK_SHIELD);
            }
        }
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int FullDaysRequired = 15;
        long currentAmountOfFullDays = (world.getLevelProperties().getTimeOfDay() / 24000L);
        return (currentAmountOfFullDays >= FullDaysRequired || !world.getLevelProperties().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION)) && canSpawnInDark(type, world, spawnReason, pos, random);
    }


    protected void initEquipment() {}

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsBaby", this.isBaby());
        nbt.putBoolean("CanBreakDoors", this.canBreakDoors());
        nbt.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
        nbt.putInt("DrownedConversionTime", this.isConvertingInWater() ? this.ticksUntilWaterConversion : -1);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setBaby(nbt.getBoolean("IsBaby"));
        this.setCanBreakDoors(nbt.getBoolean("CanBreakDoors"));
        this.inWaterTime = nbt.getInt("InWaterTime");
        if (nbt.contains("DrownedConversionTime", NbtElement.NUMBER_TYPE) && nbt.getInt("DrownedConversionTime") > -1) {
            this.setTicksUntilWaterConversion(nbt.getInt("DrownedConversionTime"));
        }
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        boolean bl = super.onKilledOther(world, other);
        if ((world.getDifficulty() == Difficulty.NORMAL || world.getDifficulty() == Difficulty.HARD) && other instanceof VillagerEntity) {
            VillagerEntity villagerEntity = (VillagerEntity)other;
            if (world.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return bl;
            }
            ZombieVillagerEntity zombieVillagerEntity = villagerEntity.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            if (zombieVillagerEntity != null) {
                zombieVillagerEntity.initialize(world, world.getLocalDifficulty(zombieVillagerEntity.getBlockPos()), SpawnReason.CONVERSION, new ZombieEntity.ZombieData(false, true), null);
                zombieVillagerEntity.setVillagerData(villagerEntity.getVillagerData());
                zombieVillagerEntity.setGossipData(villagerEntity.getGossip().serialize(NbtOps.INSTANCE));
                zombieVillagerEntity.setOfferData(villagerEntity.getOffers().toNbt());
                zombieVillagerEntity.setXp(villagerEntity.getExperience());
                if (!this.isSilent()) {
                    world.syncWorldEvent(null, WorldEvents.ZOMBIE_INFECTS_VILLAGER, this.getBlockPos(), 0);
                }
                bl = false;
            }
        }
        return bl;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return this.isBaby() ? 0.93f : 1.74f;
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
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        Random random = world.getRandom();
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        float f = difficulty.getClampedLocalDifficulty();
        this.setCanPickUpLoot(random.nextFloat() < 0.55f * f);
        if (entityData == null) {
            entityData = new LumberjackZombieEntity.ZombieData(LumberjackZombieEntity.shouldBeBaby(random), true);
        }
        if (entityData instanceof LumberjackZombieEntity.ZombieData) {
            LumberjackZombieEntity.ZombieData zombieData = (LumberjackZombieEntity.ZombieData)entityData;
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
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 0.0f;
        this.applyAttributeModifiers(f);
        return entityData;
    }

    public static boolean shouldBeBaby(Random random) {
        return false;
    }

    protected void applyAttributeModifiers(float chanceMultiplier) {
        this.initAttributes();
        this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).addPersistentModifier(new EntityAttributeModifier("Random spawn bonus", this.random.nextDouble() * (double)0.05f, EntityAttributeModifier.Operation.ADDITION));
        double d = this.random.nextDouble() * 1.5 * (double)chanceMultiplier;
        if (d > 1.0) {
            this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(new EntityAttributeModifier("Random zombie-spawn bonus", d, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (this.random.nextFloat() < chanceMultiplier * 0.05f) {
            this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25 + 0.5, EntityAttributeModifier.Operation.ADDITION));
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(new EntityAttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0 + 1.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            this.setCanBreakDoors(this.shouldBreakDoors());
        }
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

