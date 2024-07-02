package survivalplus.modid.entity.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.enchantments.ModEnchantments;
import survivalplus.modid.entity.ai.DestroyBedGoal;
import survivalplus.modid.util.ModGamerules;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class ScorchedSkeletonEntity
extends SkeletonEntity {
    private static final TrackedData<Boolean> CONVERTING = DataTracker.registerData(ScorchedSkeletonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final String STRAY_CONVERSION_TIME_KEY = "StrayConversionTime";
    private int conversionTime;
    private int smokeParticleCooldown;

    public ScorchedSkeletonEntity(EntityType<? extends ScorchedSkeletonEntity> entityType, World world) {
        super((EntityType<? extends SkeletonEntity>)entityType, world);
        smokeParticleCooldown = this.getRandom().nextInt(11) + 10;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(4, new DestroyBedGoal(this, 1.0, 8));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(CONVERTING, false);
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
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        Random random = world.getRandom();
        this.initEquipment(random, difficulty);
        this.updateEnchantments(random, difficulty);
        this.updateAttackType();
        this.isFireImmune();
        this.setCanPickUpLoot(random.nextFloat() < 0.55f * difficulty.getClampedLocalDifficulty());
        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localDate = LocalDate.now();
            int i = localDate.get(ChronoField.DAY_OF_MONTH);
            int j = localDate.get(ChronoField.MONTH_OF_YEAR);
            if (j == 10 && i == 31 && random.nextFloat() < 0.25f) {
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0f;
            }
            else this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));

            this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0f;
        }
        this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 0.0f;
        return entityData;
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int FullDaysRequired = 51;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return (!world.getLevelProperties().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= FullDaysRequired) && canSpawnInDark(type, world, spawnReason, pos, random);
    }

    @Override
    protected void updateEnchantments(Random random, LocalDifficulty localDifficulty) {
        float f = localDifficulty.getClampedLocalDifficulty();
        this.enchantMainHandItem(random, f);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.ARMOR) continue;
            this.enchantEquipment(random, f, equipmentSlot);
        }
    }

    @Override
    protected void enchantMainHandItem(Random random, float power) {
        if (this.getMainHandStack().isOf(Items.BOW)) {
            ItemStack bow = this.getMainHandStack();
            bow.addEnchantment(ModEnchantments.FLAME_TWO, 2);
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

    private void setConversionTime(int time) {
        this.conversionTime = time;
        this.setConverting(true);
    }


    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
    }
}

