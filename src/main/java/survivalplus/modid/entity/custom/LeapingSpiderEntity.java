/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import survivalplus.modid.entity.ai.LeapAtTargetGoal;
import survivalplus.modid.util.ModGamerules;

public class LeapingSpiderEntity
extends HostileEntity {
    /**
     * The tracked flags of spiders. Only has the {@code 1} bit for {@linkplain
     * #isClimbingWall() wall climbing}.
     */
    private static final TrackedData<Byte> SPIDER_FLAGS = DataTracker.registerData(LeapingSpiderEntity.class, TrackedDataHandlerRegistry.BYTE);

    public LeapingSpiderEntity(EntityType<? extends LeapingSpiderEntity> entityType, World world) {
        super((EntityType<? extends HostileEntity>)entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new LeapAtTargetGoal(this, 0.4765f));
        this.goalSelector.add(4, new AttackGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new TargetGoal<PlayerEntity>(this, PlayerEntity.class));
        this.targetSelector.add(3, new TargetGoal<IronGolemEntity>(this, IronGolemEntity.class));
    }

    @Override
    protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return new Vector3f(0.0f, dimensions.height * 0.85f, 0.0f);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SpiderNavigation(this, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SPIDER_FLAGS, (byte)0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            this.setClimbingWall(this.horizontalCollision);
        }
    }

    public static DefaultAttributeContainer.Builder createSpiderAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 3.7f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean isClimbing() {
        return this.isClimbingWall();
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
        if (!state.isOf(Blocks.COBWEB)) {
            super.slowMovement(state, multiplier);
        }
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.ARTHROPOD;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (effect.getEffectType() == StatusEffects.POISON) {
            return false;
        }
        return super.canHaveStatusEffect(effect);
    }

    public boolean isClimbingWall() {
        return (this.dataTracker.get(SPIDER_FLAGS) & 1) != 0;
    }

    public void setClimbingWall(boolean climbing) {
        byte b = this.dataTracker.get(SPIDER_FLAGS);
        b = climbing ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE);
        this.dataTracker.set(SPIDER_FLAGS, b);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean bl;
        int i;
        float k = (float) this.getVelocity().length();
        float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float g = (float) (k * this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK));
        if (target instanceof LivingEntity) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
            g += (float)EnchantmentHelper.getKnockback(this);
        }
        bl = target.damage(this.getDamageSources().mobAttack(this), f * k + 1);
        if (bl) {
            if (g > 0.0f && target instanceof LivingEntity) {
                ((LivingEntity)target).takeKnockback(g, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            if (target instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity) target;
                this.disablePlayerShield(playerEntity, this.getMainHandStack(), playerEntity.isUsingItem() ? playerEntity.getActiveItem() : ItemStack.EMPTY);
            }
            this.applyDamageEffects(this, target);
            this.onAttacking(target);
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
        int FullDaysRequired = 21;
        long currentAmountofFullDays = (world.getLevelProperties().getTimeOfDay() / 24000L);
        return (currentAmountofFullDays >= FullDaysRequired || !world.getLevelProperties().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION)) && canSpawnInDark(type, world, spawnReason, pos, random);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        Random random = world.getRandom();
        if (entityData == null) {
            entityData = new SpiderData();
            if (world.getDifficulty() == Difficulty.HARD && random.nextFloat() < 0.1f * difficulty.getClampedLocalDifficulty()) {
                ((SpiderData)entityData).setEffect(random);
            }
        }
        if (entityData instanceof SpiderData) {
            SpiderData spiderData = (SpiderData)entityData;
            StatusEffect statusEffect = spiderData.effect;
            if (statusEffect != null) {
                this.addStatusEffect(new StatusEffectInstance(statusEffect, -1));
            }
        }
        return entityData;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.65f;
    }

    @Override
    protected float getUnscaledRidingOffset(Entity vehicle) {
        return vehicle.getWidth() <= this.getWidth() ? -0.3125f : 0.0f;
    }

    static class AttackGoal
    extends MeleeAttackGoal {
        public AttackGoal(LeapingSpiderEntity spider) {
            super(spider, 1.0, true);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.mob.hasPassengers();
        }

        @Override
        public boolean shouldContinue() {
            float f = this.mob.getBrightnessAtEyes();
            if (f >= 0.5f && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            }
            return super.shouldContinue();
        }


    }

    static class TargetGoal<T extends LivingEntity>
    extends ActiveTargetGoal<T> {
        public TargetGoal(LeapingSpiderEntity spider, Class<T> targetEntityClass) {
            super((MobEntity)spider, targetEntityClass, true);
        }

        @Override
        public boolean canStart() {
            float f = this.mob.getBrightnessAtEyes();
            if (f >= 0.5f) {
                return false;
            }
            return super.canStart();
        }
    }

    public static class SpiderData
    implements EntityData {
        @Nullable
        public StatusEffect effect;

        public void setEffect(Random random) {
            int i = random.nextInt(5);
            if (i <= 1) {
                this.effect = StatusEffects.SPEED;
            } else if (i <= 2) {
                this.effect = StatusEffects.STRENGTH;
            } else if (i <= 3) {
                this.effect = StatusEffects.REGENERATION;
            } else if (i <= 4) {
                this.effect = StatusEffects.INVISIBILITY;
            }
        }
    }
}

