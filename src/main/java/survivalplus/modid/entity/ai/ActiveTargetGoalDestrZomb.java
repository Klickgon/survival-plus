/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;

import java.util.EnumSet;
import java.util.function.Predicate;


public class ActiveTargetGoalDestrZomb<T extends LivingEntity>
extends TrackTargetGoal {
    protected final Class<T> targetClass;

    protected int destroyBlockCooldown;

    protected final int reciprocalChance;
    @Nullable
    protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;
    public TagKey<Block> blockTag;
    protected BlockPos facingBlock;
    protected int destroyBlockCooldownCounter;
    protected TagKey<Item> reqItem;

    public ActiveTargetGoalDestrZomb(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
        this((ZombieEntity) mob, targetClass, 10, checkVisibility, false, null);
        if(mob instanceof MinerZombieEntity){
            this.blockTag = MinerZombieEntity.BLOCKTAG;
            destroyBlockCooldown = MinerZombieEntity.defaultCooldown;
            reqItem = ItemTags.PICKAXES;
        }
        if(mob instanceof LumberjackZombieEntity){
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
            destroyBlockCooldown = LumberjackZombieEntity.defaultCooldown;
            reqItem = ItemTags.AXES;
        }
        if(mob instanceof DiggingZombieEntity){
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
            destroyBlockCooldown = DiggingZombieEntity.defaultCooldown;
            reqItem = ItemTags.SHOVELS;
        }
        destroyBlockCooldownCounter = destroyBlockCooldown;
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
        if(this.mob.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.destroyBlockCooldownCounter <= 0){
            if(this.targetEntity != null && this.mob.getNavigation().getCurrentPath() != null && this.mob.getStackInHand(Hand.MAIN_HAND).isIn(reqItem)){
                World world = this.mob.getWorld();
                BlockPos currentPos = ((IHostileEntityChanger)this.mob).getElevatedBlockPos();

                int DiffY = calcDiffY(); // Positive: Target is higher, Negative: Zombie is Higher

                Direction direction = Direction.fromRotation(this.mob.getBodyYaw());

                switch (direction){
                    case SOUTH -> this.facingBlock = currentPos.up().south();
                    case WEST -> this.facingBlock = currentPos.up().west();
                    case NORTH -> this.facingBlock = currentPos.up().north();
                    case EAST -> this.facingBlock = currentPos.up().east();
                    default -> this.facingBlock = null;
                }

                if(this.facingBlock != null && checkOnSameXandZ()) {
                    if(DiffY == 0) {
                        if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                            world.breakBlock(this.facingBlock, true);
                        } else if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down()).getHardness(world, this.facingBlock.down());
                            world.breakBlock(this.facingBlock.down(), true);
                        }
                    }

                    if(DiffY < 0) {
                        if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down()).getHardness(world, this.facingBlock.down());
                            world.breakBlock(this.facingBlock.down(), true);
                        } else if (world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down(2)).getHardness(world, this.facingBlock.down(2));
                            world.breakBlock(this.facingBlock.down(2), true);
                        } else if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                            world.breakBlock(this.facingBlock, true);
                        }

                    }

                    if(DiffY > 0) {
                        if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                            world.breakBlock(this.facingBlock, true);
                        }
                        else if (world.getBlockState(this.facingBlock).isReplaceable() && world.getBlockState(this.facingBlock.up()).isIn(blockTag)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.up()).getHardness(world, this.facingBlock.up());
                            world.breakBlock(this.facingBlock.up(), true);
                        }
                        else if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blockTag) && world.getBlockState(this.mob.getBlockPos().up()).isIn(BlockTags.REPLACEABLE)) {
                            this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.mob.getBlockPos().up(2)).getHardness(world, this.mob.getBlockPos().up(2));
                            world.breakBlock(this.mob.getBlockPos().up(2), true);
                        }
                    }
                }
            }
        }
        this.destroyBlockCooldownCounter--;
    }

    protected int calcDiffY(){ // Calculates the height difference between the current and the next pathnode of the mob
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path == null || path.getCurrentNodeIndex() >= path.getLength()) return 0;
        if(path.getCurrentNodeIndex() > 0){
            int currentnodeposY = path.getCurrentNodePos().getY();
            int lastnodeposY = path.getNodePos(path.getCurrentNodeIndex() - 1).getY();

            return currentnodeposY - lastnodeposY;
        }
        else return 0;
    }

    protected boolean checkOnSameXandZ(){ // Calculates if the current PathNode is on the same X and Y as the Facing block
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path == null) return false;
        if(path.getCurrentNodeIndex() >= path.getLength()) return true;
        BlockPos pathNodePos = path.getCurrentNodePos();
        return pathNodePos.getX() == this.facingBlock.getX() && pathNodePos.getZ() == this.facingBlock.getZ();
    }

}

