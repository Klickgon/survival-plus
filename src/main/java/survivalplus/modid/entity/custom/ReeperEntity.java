package survivalplus.modid.entity.custom;

import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.ai.ActiveTargetGoalReeper;
import survivalplus.modid.entity.ai.ReeperDestroyBedGoal;
import survivalplus.modid.entity.ai.ReeperIgniteGoal;
import survivalplus.modid.util.ModGamerules;

import java.util.Collection;
import java.util.List;

public class ReeperEntity
        extends CreeperEntity
        implements SkinOverlayOwner {
    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(survivalplus.modid.entity.custom.ReeperEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CHARGED = DataTracker.registerData(survivalplus.modid.entity.custom.ReeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IGNITED = DataTracker.registerData(survivalplus.modid.entity.custom.ReeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int lastFuseTime;
    private int currentFuseTime;
    private int fuseTime = 30;
    private int explosionRadius = 3;
    public BlockPos targetBedPos;
    public boolean hadTarget = false;
    private boolean lostTarget = false;
    public boolean wasWithinDistance = false;

    public ReeperEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super((EntityType<? extends CreeperEntity>) entityType, world);
        this.navigation.getNodeMaker().setCanOpenDoors(true);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ReeperIgniteGoal(this));
        this.goalSelector.add(3, new FleeEntityGoal<OcelotEntity>(this, OcelotEntity.class, 6.0f, 1.0, 1.2));
        this.goalSelector.add(3, new FleeEntityGoal<CatEntity>(this, CatEntity.class, 6.0f, 1.0, 1.2));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(3, new ReeperDestroyBedGoal(this, 1.0, 8));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoalReeper<>((MobEntity)this, PlayerEntity.class, false));
        this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
    }

    public static DefaultAttributeContainer.Builder createReeperAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
        this.currentFuseTime += (int)(fallDistance * 1.5f);
        if (this.currentFuseTime > this.fuseTime - 5) {
            this.currentFuseTime = this.fuseTime - 5;
        }
        return bl;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.dataTracker.get(CHARGED).booleanValue()) {
            nbt.putBoolean("powered", true);
        }
        nbt.putShort("Fuse", (short)this.fuseTime);
        nbt.putByte("ExplosionRadius", (byte)this.explosionRadius);
        nbt.putBoolean("ignited", this.isIgnited());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(CHARGED, nbt.getBoolean("powered"));
        if (nbt.contains("Fuse", NbtElement.NUMBER_TYPE)) {
            this.fuseTime = nbt.getShort("Fuse");
        }
        if (nbt.contains("ExplosionRadius", NbtElement.NUMBER_TYPE)) {
            this.explosionRadius = nbt.getByte("ExplosionRadius");
        }
        if (nbt.getBoolean("ignited")) {
            this.ignite();
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FUSE_SPEED, -1);
        builder.add(CHARGED, false);
        builder.add(IGNITED, false);
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            if(!this.hadTarget && this.getTarget() != null) this.hadTarget = true;
            if(this.hadTarget && this.getTarget() == null) this.lostTarget = true;
            int i;
            this.lastFuseTime = this.currentFuseTime;
            if (this.isIgnited()) {
                this.setFuseSpeed(1);
            }
            if (this.lostTarget) {
                if(this.wasWithinDistance){
                    this.igniteWithEntityCheck();
                }
                else {
                    this.hadTarget = false;
                    this.lostTarget = false;
                }
            }
            Path path = this.getNavigation().getCurrentPath();
            if(path != null && path.getLength() > path.getCurrentNodeIndex()){
                PathNode pathNode = path.getCurrentNode();
                if(pathNode != null && isNodeTypeClosedDoor(this.getNavigation().getNodeMaker().getDefaultNodeType(this, new BlockPos(pathNode.x, pathNode.y, pathNode.z)))){
                    this.igniteWithEntityCheck();
                }
            }

            if ((i = this.getFuseSpeed()) > 0 && this.currentFuseTime == 0) {
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);
                this.emitGameEvent(GameEvent.PRIME_FUSE);
            }
            this.currentFuseTime += i;
            if (this.currentFuseTime < 0) {
                this.currentFuseTime = 0;
            }
            if (this.currentFuseTime >= this.fuseTime) {
                this.currentFuseTime = this.fuseTime;
                this.explode();
            }
        }
        super.tick();
    }

    private boolean isNodeTypeClosedDoor(PathNodeType pnt){
        return pnt == PathNodeType.DOOR_WOOD_CLOSED || pnt == PathNodeType.DOOR_IRON_CLOSED;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof GoatEntity) {
            return;
        }
        super.setTarget(target);
    }


    private void igniteWithEntityCheck(){
        Vec3d vec3d = Vec3d.ofBottomCenter(this.getBlockPos());
        List<HostileEntity> list = this.getWorld().getEntitiesByClass(HostileEntity.class, new Box(vec3d.getX() - 3.0, vec3d.getY() - 3.0, vec3d.getZ() - 3.0, vec3d.getX() + 3.0, vec3d.getY() + 3.0, vec3d.getZ() + 3.0), hostileEntity -> true);
        if (list.size() < 4) {
            this.setFuseSpeed(1);
        }
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        super.onStruckByLightning(world, lightning);
        this.dataTracker.set(CHARGED, true);
    }

    public static boolean canSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random){
        int FullDaysRequired = 43;
        int currentAmountOfFullDays = (int) (world.getLevelProperties().getTimeOfDay() / 24000L);
        return (!world.getLevelProperties().getGameRules().getBoolean(ModGamerules.MOB_SPAWN_PROGRESSION) || currentAmountOfFullDays >= FullDaysRequired) && canSpawnInDark(type, world, spawnReason, pos, random);
    }


    private void explode() {
        if (!this.getWorld().isClient) {
            float f = this.shouldRenderOverlay() ? 2.0f : 1.0f;
            this.dead = true;
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, World.ExplosionSourceType.MOB);
            this.discard();
            this.spawnEffectsCloud();
        }
    }

    private void spawnEffectsCloud() {
        Collection<StatusEffectInstance> collection = this.getStatusEffects();
        if (!collection.isEmpty()) {
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(this.getWorld(), this.getX(), this.getY(), this.getZ());
            areaEffectCloudEntity.setRadius(2.5f);
            areaEffectCloudEntity.setRadiusOnUse(-0.5f);
            areaEffectCloudEntity.setWaitTime(10);
            areaEffectCloudEntity.setDuration(areaEffectCloudEntity.getDuration() / 2);
            areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());
            for (StatusEffectInstance statusEffectInstance : collection) {
                areaEffectCloudEntity.addEffect(new StatusEffectInstance(statusEffectInstance));
            }
            this.getWorld().spawnEntity(areaEffectCloudEntity);
        }
    }

    public boolean isIgnited() {
        return this.dataTracker.get(IGNITED);
    }

    public void ignite() {
        this.dataTracker.set(IGNITED, true);
    }


}