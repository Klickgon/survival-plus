package survivalplus.modid.entity.custom;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.entity.ai.AdvancedBowAttackGoal;
import survivalplus.modid.entity.ai.DestroyBedGoal;
import survivalplus.modid.util.ModGamerules;

public class ScorchedSkeletonEntity
extends SkeletonEntity {
    private static final TrackedData<Boolean> CONVERTING = DataTracker.registerData(ScorchedSkeletonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final String STRAY_CONVERSION_TIME_KEY = "StrayConversionTime";
    private int conversionTime;
    private int smokeParticleCooldown;

    private final AdvancedBowAttackGoal<ScorchedSkeletonEntity> bowAttackGoal = new AdvancedBowAttackGoal<>(this, 1.0, 20, 15.0f);

    public ScorchedSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
        smokeParticleCooldown = this.getRandom().nextInt(11) + 10;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CONVERTING, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new AvoidSunlightGoal(this));
        this.goalSelector.add(2, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(2, new FleeEntityGoal(this, WolfEntity.class, 6.0F, 1.0, 1.2));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
        this.goalSelector.add(5, new DestroyBedGoal(this, 1.0, 8));
    }

    public void tick(){
        super.tick();
        if (this.getWorld().isClient) {
            if(smokeParticleCooldown <= 0) {
                getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.99, this.getZ(), 0, 0, 0);
                smokeParticleCooldown = this.getRandom().nextInt(11) + 10;
            }
            else smokeParticleCooldown--;
        }
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        this.updateEnchantments(world.getRandom(), difficulty);
        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0f;
        }
        this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 0.0f;
        return entityData;
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int fullDaysRequired = 51;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return world.getServer() != null && (!world.getServer().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= fullDaysRequired || spawnReason != SpawnReason.NATURAL) && canSpawnInDark(type, world, spawnReason, pos, random);
    }

    protected void updateEnchantments(Random random, LocalDifficulty localDifficulty) {
        float f = localDifficulty.getClampedLocalDifficulty();
        this.enchantMainHandItem(random, f);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantEquipment((ServerWorldAccess)this.getWorld(), random, equipmentSlot, localDifficulty);
        }
    }

    protected void enchantMainHandItem(Random random, float power) {
        if (this.getMainHandStack().isOf(Items.BOW)) {
            ItemStack bow = this.getMainHandStack();
            bow.addEnchantment(this.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.FLAME_TWO), 1);
            this.equipStack(EquipmentSlot.MAINHAND, bow);
        }
    }

    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    @Override
    public boolean isConverting() {
        return this.getDataTracker().get(CONVERTING);
    }

    @Override
    public void setConverting(boolean converting) {
    }

    @Override
    public boolean isShaking() {
        return this.isConverting();
    }


    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(STRAY_CONVERSION_TIME_KEY, this.isConverting() ? this.conversionTime : -1);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(STRAY_CONVERSION_TIME_KEY, NbtElement.NUMBER_TYPE) && nbt.getInt(STRAY_CONVERSION_TIME_KEY) > -1) {
            this.setConversionTime(nbt.getInt(STRAY_CONVERSION_TIME_KEY));
        }
    }

    @Override
    public void updateAttackType() {
        if (this.getWorld() == null || this.getWorld().isClient) {
            return;
        }
        this.goalSelector.remove(this.bowAttackGoal);
        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        if (itemStack.isOf(Items.BOW)) {
            int i = 20;
            if (this.getWorld().getDifficulty() != Difficulty.HARD) {
                i = 40;
            }
            this.bowAttackGoal.setAttackInterval(i);
            this.goalSelector.add(3, this.bowAttackGoal);
        }
    }
}

