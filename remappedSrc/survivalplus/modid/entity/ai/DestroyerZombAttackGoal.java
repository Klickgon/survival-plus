package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;

public class DestroyerZombAttackGoal extends ZombieAttackGoal {

    protected int ticks;
    protected final ZombieEntity zombie;
    protected final double speed;
    protected final boolean pauseWhenMobIdle;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected int updateCountdownTicks;
    protected int cooldown;
    protected byte lookCounter;
    protected byte lookCounterCooldown;
    protected Random random = this.mob.getRandom();
    protected TagKey<Block> blockTag;


    public DestroyerZombAttackGoal(ZombieEntity zombie, double speed, boolean pauseWhenMobIdle) {
        super(zombie, speed, pauseWhenMobIdle);
        this.zombie = zombie;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        if(mob instanceof MinerZombieEntity) this.blockTag = MinerZombieEntity.BLOCKTAG;
        else if(mob instanceof LumberjackZombieEntity) this.blockTag = LumberjackZombieEntity.BLOCKTAG;
        else if(mob instanceof DiggingZombieEntity) this.blockTag = DiggingZombieEntity.BLOCKTAG;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return;
        }
        if(this.lookCounter-- <= 0 && this.lookCounterCooldown-- <= 0 && this.random.nextInt(70) < 3) this.lookCounter = (byte) random.nextBetween(10, 30);
        if((this.lookCounter > 0 || this.mob.getPos().squaredDistanceTo(livingEntity.getPos()) < 16) && !breakableBlockWithinPath()){
            this.mob.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);
            this.lookCounterCooldown = (byte) (20 + random.nextInt(30));
        }
        this.updateCountdownTicks = Math.max(this.updateCountdownTicks - 1, 0);
        if ((this.pauseWhenMobIdle || this.mob.getVisibilityCache().canSee(livingEntity)) && this.updateCountdownTicks <= 0 && (this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0 || livingEntity.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05f)) {
            this.targetX = livingEntity.getX();
            this.targetY = livingEntity.getY();
            this.targetZ = livingEntity.getZ();
            this.updateCountdownTicks = 4 + this.mob.getRandom().nextInt(7);
            double d = this.mob.squaredDistanceTo(livingEntity);
            if (d > 1024.0) {
                this.updateCountdownTicks += 10;
            } else if (d > 256.0) {
                this.updateCountdownTicks += 5;
            }
            if (!this.mob.getNavigation().startMovingTo(livingEntity, this.speed)) {
                this.updateCountdownTicks += 15;
            }
            this.updateCountdownTicks = this.getTickCount(this.updateCountdownTicks);
        }
        this.cooldown = Math.max(this.cooldown - 1, 0);
        this.attack(livingEntity);
        ++this.ticks;
        this.zombie.setAttacking(this.ticks >= 5 && this.getCooldown() < this.getMaxCooldown() / 2);
    }

    private boolean breakableBlockWithinPath() {
        Path path = this.zombie.getNavigation().getCurrentPath();
        if(path == null || path.getCurrentNodeIndex() >= path.getLength()) return false;
        BlockPos pathBlockPos = path.getCurrentNodePos();
        World world = this.zombie.getWorld();
        return world.getBlockState(pathBlockPos).isIn(blockTag) || world.getBlockState(pathBlockPos.up()).isIn(blockTag);
    }
}
