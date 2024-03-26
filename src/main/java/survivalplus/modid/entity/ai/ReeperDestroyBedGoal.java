package survivalplus.modid.entity.ai;


import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.ReeperEntity;

import java.util.EnumSet;

public class ReeperDestroyBedGoal extends MoveToTargetPosGoal {
    private final TagKey<Block> BedGroup = BlockTags.BEDS;
    private final ReeperEntity reeper;



    public ReeperDestroyBedGoal(ReeperEntity reeper, double speed) {
        super(reeper, speed, 512);
        this.reeper = reeper;
    }

    public boolean canStart() {
        if (!this.reeper.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        if (this.findTargetPos()) {
            return true;
        }
        this.cooldown = this.getInterval(this.reeper);
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.reeper.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
    }


    @Override
    public double getDesiredDistanceToTarget() {
        return 2.0f;
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.reeper.getWorld();
        BlockPos blockPos = this.reeper.getBlockPos();
        if (blockPos != null && !targetPos.isWithinDistance(this.reeper.getBlockPos(), 3.0)) {
                this.reeper.setFuseSpeed(-1);
        }
        else this.reeper.setFuseSpeed(1);
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            return chunk.getBlockState(pos).isIn(this.BedGroup) && chunk.getBlockState(pos.up()).isAir() && chunk.getBlockState(pos.up(2)).isAir();
        }
        return false;
    }

}

