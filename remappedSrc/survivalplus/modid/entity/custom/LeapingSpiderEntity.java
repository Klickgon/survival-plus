/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import survivalplus.modid.entity.ai.DestroyBedGoal;
import survivalplus.modid.entity.ai.LeapAtTargetGoal;
import survivalplus.modid.util.ModGamerules;

public class LeapingSpiderEntity
extends SpiderEntity {

    private static final TrackedData<Byte> SPIDER_FLAGS = DataTracker.registerData(LeapingSpiderEntity.class, TrackedDataHandlerRegistry.BYTE);
    public boolean isLeaping;
    public int attackCooldown = 15;

    public LeapingSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
        this.isLeaping = false;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new LeapAtTargetGoal(this, 0.4765f));
        this.goalSelector.add(4, new AttackGoal(this));
        this.goalSelector.add(4, new DestroyBedGoal(this, 1.0, 8));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
        this.targetSelector.add(2, new TargetGoal<PlayerEntity>(this, PlayerEntity.class));
        this.targetSelector.add(3, new TargetGoal<IronGolemEntity>(this, IronGolemEntity.class));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SPIDER_FLAGS, (byte)0);
    }

    @Override
    public void tick() {
        super.tick();
        this.attackCooldown--;
        if (!this.getWorld().isClient) {
            this.setClimbingWall(this.horizontalCollision);
        }
    }

    public static DefaultAttributeContainer.Builder createSpiderAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.MAX_HEALTH, 16.0).add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.ATTACK_KNOCKBACK, 3.7f);
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
    public boolean tryAttack(ServerWorld sworld, Entity target) {
        boolean bl;
        float k = (float) this.getVelocity().length();
        DamageSource damageSource = this.getDamageSources().mobAttack(this);
        float f = EnchantmentHelper.getDamage(sworld, this.getWeaponStack(), target, damageSource, (float)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE));
        bl = this.attackCooldown <= 0 && (this.isOnGround() || this.isLeaping) && target.damage(sworld, this.getDamageSources().mobAttack(this), f * k + 1);
        if (bl) {
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
            this.attackCooldown = 15;
        }
        return bl;
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int fullDaysRequired = 25;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return world.getServer() != null && (!world.getServer().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= fullDaysRequired || spawnReason != SpawnReason.NATURAL) && canSpawnInDark(type, world, spawnReason, pos, random);
    }

    static class AttackGoal
    extends MeleeAttackGoal {
        private LeapingSpiderEntity spider;
        public AttackGoal(LeapingSpiderEntity spider) {
            super(spider, 1.0, true);
            this.spider = spider;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !this.mob.hasPassengers();
        }

    }

    static class TargetGoal<T extends LivingEntity>
    extends ActiveTargetGoal<T> {
        public TargetGoal(LeapingSpiderEntity spider, Class<T> targetEntityClass) {
            super((MobEntity)spider, targetEntityClass, true);
        }
    }

}

