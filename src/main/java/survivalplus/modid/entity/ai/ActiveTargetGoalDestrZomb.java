/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;


public class ActiveTargetGoalDestrZomb<T extends LivingEntity>
extends TrackTargetGoal {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
    protected final Class<T> targetClass;

    private static final int destroyBlockCooldown = 10;

    protected final int reciprocalChance;
    @Nullable
    protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;
    public TagKey<Block> blocktag;
    private BlockPos facingBlock;

    private int destroyBlockCooldownCounter = 10;

    public ActiveTargetGoalDestrZomb(MobEntity mob, Class<T> targetClass, boolean checkVisibility, TagKey<Block> blocktag) {
        this((ZombieEntity) mob, targetClass, 10, checkVisibility, false, null);
        this.blocktag = blocktag;
    }

    public ActiveTargetGoalDestrZomb(ZombieEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, checkVisibility, checkCanNavigate);
        this.targetClass = targetClass;
        this.reciprocalChance = ActiveTargetGoalDestrZomb.toGoalTicks(reciprocalChance);
        this.setControls(EnumSet.of(Control.TARGET));
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate).ignoreVisibility();
    }

    @Override
    public boolean canStart() {
        if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
        }
        this.findClosestTarget();
        return this.targetEntity != null;
    }


    protected Box getSearchBox(double distance) {
        return this.mob.getBoundingBox().expand(distance, 4.0, distance);
    }

    protected void findClosestTarget() {
        this.targetEntity = this.targetClass == PlayerEntity.class || this.targetClass == ServerPlayerEntity.class ? this.mob.getWorld().getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : this.mob.getWorld().getClosestEntity(this.mob.getWorld().getEntitiesByClass(this.targetClass, this.getSearchBox(this.getFollowRange()), livingEntity -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetEntity);
        super.start();
    }


    @Override
    public void tick() {
        if(this.destroyBlockCooldownCounter <= 0){
            if(this.targetEntity != null){
                World world = this.mob.getWorld();
                float rotation = this.mob.getHeadYaw();
                BlockPos currentPos = this.mob.getBlockPos();
                int cposX = currentPos.getX();
                int cposY = currentPos.getY();
                int cposZ = currentPos.getZ();
                int targetposY = this.targetEntity.getBlockY();
                int DiffY = targetposY - cposY; // Positive: Target is higher, Negative: Zombie is Higher
                if (rotation > -135 && rotation <= -45)     this.facingBlock = new BlockPos(cposX + 1, cposY + 1, cposZ);
                else if(rotation > 45 && rotation <= 135)   this.facingBlock = new BlockPos(cposX - 1, cposY + 1, cposZ);
                else if(rotation > 135 || rotation <= -135) this.facingBlock = new BlockPos(cposX, cposY + 1, cposZ - 1);
                else if (rotation > -45 || rotation <= 45)  this.facingBlock = new BlockPos(cposX, cposY + 1, cposZ + 1);


                if(DiffY <= 0 && DiffY > -2) {
                    if (world.getBlockState(this.facingBlock).isIn(blocktag)) {
                        world.breakBlock(this.facingBlock, false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    } else if (world.getBlockState(this.facingBlock.down()).isIn(blocktag)) {
                        world.breakBlock(this.facingBlock.down(), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                }

                if(DiffY < -2) {
                    if (world.getBlockState(this.facingBlock.down()).isIn(blocktag)) {
                        world.breakBlock(this.facingBlock.down(), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if(world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blocktag)){
                        world.breakBlock(this.facingBlock.down(2), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if (world.getBlockState(this.facingBlock).isIn(blocktag)) {
                        world.breakBlock(this.facingBlock, false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }

                }

                if(DiffY > 0) {
                    if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blocktag)) {
                        world.breakBlock(this.mob.getBlockPos().up(2), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if (world.getBlockState(this.facingBlock).isIn(blocktag)) {
                            world.breakBlock(this.facingBlock, false);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    else if(world.getBlockState(this.facingBlock).isReplaceable()){
                        world.breakBlock(this.facingBlock.up(), false);
                        this.destroyBlockCooldownCounter = destroyBlockCooldown;
                    }
                    }
                }
            }
        else this.destroyBlockCooldownCounter--;
    }


    public void setTargetEntity(@Nullable LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public @Nullable LivingEntity getTargetEntity(){
        return this.targetEntity;
    }
}

