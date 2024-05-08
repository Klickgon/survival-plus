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
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;

import java.util.EnumSet;
import java.util.function.Predicate;


public class ActiveTargetGoalDestrZomb<T extends LivingEntity>
extends TrackTargetGoal {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
    protected final Class<T> targetClass;

    private static final int destroyBlockCooldown = 20;

    protected final int reciprocalChance;
    @Nullable
    protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;
    public TagKey<Block> blockTag;
    private BlockPos facingBlock;

    private int destroyBlockCooldownCounter = 20;

    public ActiveTargetGoalDestrZomb(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
        this((ZombieEntity) mob, targetClass, 10, checkVisibility, false, null);
        if(mob.getClass() == MinerZombieEntity.class){
            this.blockTag = MinerZombieEntity.BLOCKTAG;
        }
        if(mob.getClass() == LumberjackZombieEntity.class){
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
        }
        if(mob.getClass() == DiggingZombieEntity.class){
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
        }
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

                float rawrotation = Math.abs(this.mob.getBodyYaw());
                float rotation = (float) (rawrotation - 360 * (Math.floor(rawrotation / 360)));

                BlockPos currentPos = this.mob.getBlockPos();
                int cposY = currentPos.getY();

                int targetposY = this.targetEntity.getBlockY();
                int DiffY = targetposY - cposY; // Positive: Target is higher, Negative: Zombie is Higher

                if (rotation > 315 || rotation <= 45)   this.facingBlock = currentPos.up().south();
                else if (rotation <= 135)               this.facingBlock = currentPos.up().west();
                else if (rotation <= 225)               this.facingBlock = currentPos.up().north();
                else if (rotation <= 315)               this.facingBlock = currentPos.up().east();

                    if(DiffY > 0 && DiffY <= 2) {
                        if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                            world.breakBlock(this.facingBlock, true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        } else if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                            world.breakBlock(this.facingBlock.down(), true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        }
                    }

                    if(DiffY < 0) {
                        if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                            world.breakBlock(this.facingBlock.down(), true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        }
                        else if(world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blockTag)){
                            world.breakBlock(this.facingBlock.down(2), true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        }
                        else if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                            world.breakBlock(this.facingBlock, true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        }

                    }

                    if(DiffY > 2) {
                        if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                            world.breakBlock(this.facingBlock, true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        }
                        else if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blockTag) && world.getBlockState(this.mob.getBlockPos().up()).isIn(BlockTags.REPLACEABLE)) {
                            world.breakBlock(this.mob.getBlockPos().up(2), true);
                            this.destroyBlockCooldownCounter = destroyBlockCooldown;
                        }
                        else if(world.getBlockState(this.facingBlock).isReplaceable() && world.getBlockState(this.facingBlock.up()).isIn(blockTag)){
                            world.breakBlock(this.facingBlock.up(), true);
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

