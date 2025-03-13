package survivalplus.modid.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import survivalplus.modid.entity.custom.DiggingZombieEntity;
import survivalplus.modid.entity.custom.LumberjackZombieEntity;
import survivalplus.modid.entity.custom.MinerZombieEntity;
import survivalplus.modid.util.IHostileEntityChanger;
import survivalplus.modid.util.IServerPlayerChanger;

import java.util.List;

public class DestrZombDestroyBedGoal extends MoveToTargetPosGoal {

    protected final HostileEntity destroyMob;
    protected static final TagKey<Block> bedGroup = BlockTags.BEDS;
    protected int destroyBlockCooldown;
    protected TagKey<Block> blockTag;
    @Nullable
    protected BlockPos facingBlock;
    protected int destroyBlockCooldownCounter;
    protected TagKey<Item> reqItem;

    public DestrZombDestroyBedGoal(HostileEntity mob, double speed, int maxYDifference) {
        super(mob, speed, 32, maxYDifference);
        this.destroyMob = mob;
        this.cooldown = 0;
        if(mob instanceof MinerZombieEntity){
            this.blockTag = MinerZombieEntity.BLOCKTAG;
            destroyBlockCooldown = MinerZombieEntity.defaultCooldown;
            reqItem = ItemTags.PICKAXES;
        }
        else if(mob instanceof LumberjackZombieEntity){
            this.blockTag = LumberjackZombieEntity.BLOCKTAG;
            destroyBlockCooldown = LumberjackZombieEntity.defaultCooldown;
            reqItem = ItemTags.AXES;
        }
        else if(mob instanceof DiggingZombieEntity){
            this.blockTag = DiggingZombieEntity.BLOCKTAG;
            destroyBlockCooldown = DiggingZombieEntity.defaultCooldown;
            reqItem = ItemTags.SHOVELS;
        }
        destroyBlockCooldownCounter = destroyBlockCooldown;
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        MinecraftServer server = this.destroyMob.getServer();
        if (server == null || !server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            return false;
        }
        World world = this.destroyMob.getWorld();
        BlockPos pos = this.destroyMob.getBlockPos();
        if(world.getBlockState(pos).isIn(blockTag) || world.getBlockState(pos.up()).isIn(blockTag)) return false;
        this.cooldown = 20 + this.mob.getWorld().random.nextInt(10);
        return this.findTargetPos();
    }

    @Override
    public void stop() {
        super.stop();
        if(destroyMob instanceof MinerZombieEntity) ((MinerZombieEntity) this.destroyMob).targetBedPos = null;
        else if(destroyMob instanceof LumberjackZombieEntity) ((LumberjackZombieEntity) this.destroyMob).targetBedPos = null;
        else if(destroyMob instanceof DiggingZombieEntity) ((DiggingZombieEntity) this.destroyMob).targetBedPos = null;
        this.destroyMob.fallDistance = 1.0f;
    }

    @Override
    public void start() {
        super.start();
        if(destroyMob instanceof MinerZombieEntity) ((MinerZombieEntity) this.destroyMob).targetBedPos = this.targetPos;
        else if(destroyMob instanceof LumberjackZombieEntity) ((LumberjackZombieEntity) this.destroyMob).targetBedPos = this.targetPos;
        else if(destroyMob instanceof DiggingZombieEntity) ((DiggingZombieEntity) this.destroyMob).targetBedPos = this.targetPos;
    }

    @Override
    public boolean shouldContinue() {
        return this.tryingTime <= 1200 && !this.isTargetPos(this.mob.getWorld(), this.targetPos);
    }


    @Override
    public void tick() {
        super.tick();
        World world = this.destroyMob.getWorld();
        BlockPos blockPos = this.destroyMob.getBlockPos();
        BlockPos blockPos2 = this.tweakToProperPos(blockPos, world);
        if (blockPos2 != null && blockPos2.isWithinDistance(blockPos, 3)) {
            this.destroyMob.swingHand(Hand.MAIN_HAND);
            world.breakBlock(blockPos2, true);
            this.stop();
        }

        if(this.destroyBlockCooldownCounter <= 0 && this.mob.getNavigation().getCurrentPath() != null && this.mob.getStackInHand(Hand.MAIN_HAND).isIn(reqItem)){
            BlockPos currentPos = ((IHostileEntityChanger)this.mob).getElevatedBlockPos();

            int DiffY = calcDiffY(); // Positive: Target is higher, Negative: Zombie is Higher

            Direction direction = Direction.fromHorizontalDegrees(this.mob.getBodyYaw());

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
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock, true);
                    } else if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down()).getHardness(world, this.facingBlock.down());
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock.down(), true);
                    }
                }

                else if(DiffY < 0) {
                    if (world.getBlockState(this.facingBlock.down()).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down()).getHardness(world, this.facingBlock.down());
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock.down(), true);
                    } else if (world.getBlockState(this.facingBlock.down()).isReplaceable() && world.getBlockState(this.facingBlock.down(2)).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.down(2)).getHardness(world, this.facingBlock.down(2));
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock.down(2), true);
                    } else if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock, true);
                    }

                }

                else {
                    if (world.getBlockState(this.facingBlock).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock).getHardness(world, this.facingBlock);
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock, true);
                    }
                    else if (world.getBlockState(this.facingBlock).isReplaceable() && world.getBlockState(this.facingBlock.up()).isIn(blockTag)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.facingBlock.up()).getHardness(world, this.facingBlock.up());
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.facingBlock.up(), true);
                    }
                    else if (world.getBlockState(this.mob.getBlockPos().up(2)).isIn(blockTag) && world.getBlockState(this.mob.getBlockPos().up()).isIn(BlockTags.REPLACEABLE)) {
                        this.destroyBlockCooldownCounter = destroyBlockCooldown + (int) world.getBlockState(this.mob.getBlockPos().up(2)).getHardness(world, this.mob.getBlockPos().up(2));
                        mob.swingHand(Hand.MAIN_HAND);
                        world.breakBlock(this.mob.getBlockPos().up(2), true);
                    }
                }
            }
        }
        this.destroyBlockCooldownCounter--;
    }

    private int calcDiffY(){ // Calculates the height difference between the current and the last used pathnode of the mob
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path == null || path.getCurrentNodeIndex() >= path.getLength()) return 0;
        if(path.getCurrentNodeIndex() > 0){
            int currentnodeposY = path.getCurrentNodePos().getY();
            int lastnodeposY = path.getNodePos(path.getCurrentNodeIndex() - 1).getY();

            return currentnodeposY - lastnodeposY;
        }
        else return 0;
    }

    private boolean checkOnSameXandZ(){ // Calculates if the current PathNode is on the same X and Y as the Facing block
        Path path = this.mob.getNavigation().getCurrentPath();
        if(path == null) return false;
        if(path.getCurrentNodeIndex() > path.getLength() - 1) return true;
        BlockPos pathNodePos = path.getCurrentNodePos();
        return pathNodePos.getX() == this.facingBlock.getX() && pathNodePos.getZ() == this.facingBlock.getZ();
    }

    @Nullable
    private BlockPos tweakToProperPos(BlockPos pos, BlockView world) {
        if (world.getBlockState(pos).isIn(this.bedGroup)) {
            return pos;
        }
        BlockPos[] blockPoss = new BlockPos[]{pos.west(), pos.east(), pos.north(), pos.south(), pos.up()};
        for (BlockPos blockPos : blockPoss) {
            if (!world.getBlockState(blockPos).isIn(this.bedGroup)) continue;
            return blockPos;
        }
        return null;
    }

    @Override
    protected boolean findTargetPos() {
        List<ServerPlayerEntity> list = this.mob.getServer().getPlayerManager().getPlayerList();
        BlockPos temptargetpos = null;
        BlockPos mobpos = this.mob.getBlockPos();
        boolean bl = false;
        for(ServerPlayerEntity player : list){
            if(player.isCreative() || player.isSpectator()) continue;
            BlockPos spawnpos = ((IServerPlayerChanger)player).getMainSpawnPoint();
            if(spawnpos != null && spawnpos.isWithinDistance(mobpos, 32) && spawnpos.isWithinDistance(player.getBlockPos(), 48) && this.mob.getWorld().getBlockState(spawnpos).isIn(BlockTags.BEDS)){
                bl = true;
                if(temptargetpos == null || spawnpos.getSquaredDistance(mobpos) < temptargetpos.getSquaredDistance(mobpos)){
                    temptargetpos = spawnpos;
                }
            }
        }
        if (bl) this.targetPos = temptargetpos;
        return bl;
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk != null) {
            return chunk.getBlockState(pos.down()).isIn(this.bedGroup);
        }
        return false;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 1.14;
    }
}