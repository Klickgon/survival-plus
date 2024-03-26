/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.BuilderZombieEntity;

import java.util.EnumSet;
import java.util.function.Predicate;

import static net.minecraft.command.argument.BlockPosArgumentType.getBlockPos;


public class ActiveTargetGoalBuilderZomb<T extends LivingEntity>
extends TrackTargetGoal {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
    protected final Class<T> targetClass;

    protected final int reciprocalChance;
    @Nullable
    protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;

    private int DirtJumpCooldown = 20;

    public ActiveTargetGoalBuilderZomb(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
        this((BuilderZombieEntity) mob, targetClass, 10, checkVisibility, false, null);
    }

    public ActiveTargetGoalBuilderZomb(MobEntity mob, Class<T> targetClass, boolean checkVisibility, Predicate<LivingEntity> targetPredicate) {
        this((BuilderZombieEntity) mob, targetClass, 10, checkVisibility, false, targetPredicate);
    }

    public ActiveTargetGoalBuilderZomb(MobEntity mob, Class<T> targetClass, boolean checkVisibility, boolean checkCanNavigate) {
        this((BuilderZombieEntity) mob, targetClass, 10, checkVisibility, checkCanNavigate, null);
    }

    public ActiveTargetGoalBuilderZomb(BuilderZombieEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, checkVisibility, checkCanNavigate);
        this.targetClass = targetClass;
        this.reciprocalChance = ActiveTargetGoalBuilderZomb.toGoalTicks(reciprocalChance);
        this.setControls(EnumSet.of(Control.TARGET));
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
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
        if(targetEntity != null){
            int mobPosY = mob.getBlockPos().getY();
            int targetPosY = targetEntity.getBlockPos().getY();
            int mobTargetDiff = mobPosY - targetPosY;

            if(mobTargetDiff < 0 && mobTargetDiff > -8){

                World world = mob.getWorld();

                    if(DirtJumpCooldown <= 0 && world.getBlockState(mob.getBlockPos()).isIn(BlockTags.REPLACEABLE)){
                        if(mob.getWorld().getBlockState(mob.getBlockPos().up(2)).isOf(Blocks.AIR)){
                        this.mob.getJumpControl().setActive();
                        BlockPos BlockUnder = mob.getBlockPos();
                        mob.getWorld().setBlockState(BlockUnder, Blocks.DIRT.getDefaultState());
                        world.playSound(null, BlockUnder, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
                        DirtJumpCooldown = 10;
                        }
                    }
                    else DirtJumpCooldown--;
                }

            }
        }


    public void setTargetEntity(@Nullable LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public @Nullable LivingEntity getTargetEntity(){
        return this.targetEntity;
    }
}

