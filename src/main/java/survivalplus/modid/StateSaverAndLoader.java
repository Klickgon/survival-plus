package survivalplus.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Uuids.SET_CODEC.fieldOf("players").forGetter(stateSaverAndLoader -> stateSaverAndLoader.players.keySet())
                    )
                    .apply(instance, StateSaverAndLoader::new)
    );

    public static final PersistentStateType<StateSaverAndLoader> TYPE = new PersistentStateType<>("stateSaverAndLoader", StateSaverAndLoader::new , CODEC, null);

    public HashMap<UUID, PlayerData> players;

    public StateSaverAndLoader(){
        players = new HashMap<>();
    }

    private StateSaverAndLoader(Set<UUID> playerSet){
        for(UUID player : playerSet){
            this.players.put(player, new PlayerData());
        }
    }
    /*@Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        NbtCompound playersNbt = new NbtCompound();
        this.players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("baseAssaultTimer", playerData.baseAssaultTimer);
            playerNbt.putByteArray("generatedWave", playerData.generatedWave);
            if(playerData.tempRespawn != null){
                BlockPos pos = playerData.tempRespawn.pos();
                playerNbt.putInt("tempSpawnPositionX", pos.getX());
                playerNbt.putInt("tempSpawnPositionY", pos.getY());
                playerNbt.putInt("tempSpawnPositionZ", pos.getZ());

                Identifier.CODEC.encodeStart(NbtOps.INSTANCE, playerData.tempRespawn.dimension().getValue()).resultOrPartial(SurvivalPlus.LOGGER::error).ifPresent(encoded -> playerNbt.put("tempSpawnDimension", encoded));
                playerNbt.putFloat("tempSpawnAngle", playerData.tempRespawn.angle());
                playerNbt.putBoolean("tempSpawnForced", playerData.tempRespawn.forced());
            }

            if(playerData.mainRespawn != null){
                BlockPos pos = playerData.mainRespawn.pos();
                playerNbt.putInt("mainSpawnPositionX", pos.getX());
                playerNbt.putInt("mainSpawnPositionY", pos.getY());
                playerNbt.putInt("mainSpawnPositionZ", pos.getZ());

                Identifier.CODEC.encodeStart(NbtOps.INSTANCE, playerData.mainRespawn.dimension().getValue()).resultOrPartial(SurvivalPlus.LOGGER::error).ifPresent(encoded -> playerNbt.put("mainSpawnDimension", encoded));
                playerNbt.putFloat("mainSpawnAngle", playerData.mainRespawn.angle());
                playerNbt.putBoolean("mainSpawnForced", playerData.mainRespawn.forced());
            }
            playerNbt.putBoolean("receivedBAWarning", playerData.receivedBAWarningMessage);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }*/

    /*public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        Optional<NbtCompound> playersNbtOp = tag.getCompound("players");
        if(playersNbtOp.isPresent()){
            NbtCompound playersNbt = playersNbtOp.get();
            playersNbt.getKeys().forEach(key -> {
                UUID uuid = UUID.fromString(key);
                PlayerData playerData = new PlayerData();
                NbtCompound compound = playersNbt.getCompound(key).get();
                playerData.baseAssaultTimer = compound.getInt("baseAssaultTimer").get();
                if(compound.contains("generatedWave")) playerData.generatedWave = compound.getByteArray("generatedWave").get();

                BlockPos respawnPos = null;
                if(compound.contains("tempSpawnPositionX") && compound.contains("tempSpawnPositionY") && compound.contains("tempSpawnPositionZ")) {
                    int x = compound.getInt("tempSpawnPositionX").get();
                    int y = compound.getInt("tempSpawnPositionY").get();
                    int z = compound.getInt("tempSpawnPositionZ").get();
                    respawnPos = new BlockPos(x, y, z);
                }
                RegistryKey<World> dimension = World.CODEC.parse(NbtOps.INSTANCE, compound.get("tempSpawnDimension")).resultOrPartial(SurvivalPlus.LOGGER::error).orElse(World.OVERWORLD);
                playerData.tempRespawn = new ServerPlayerEntity.Respawn(dimension, respawnPos, compound.getFloat("tempSpawnAngle", 0.0f), compound.getBoolean("tempSpawnForced", false));

                if(compound.contains("mainSpawnPositionX") && compound.contains("mainSpawnPositionY") && compound.contains("mainSpawnPositionZ")) {
                    int x = compound.getInt("mainSpawnPositionX").get();
                    int y = compound.getInt("mainSpawnPositionY").get();
                    int z = compound.getInt("mainSpawnPositionZ").get();
                    respawnPos = new BlockPos(x, y, z);
                }

                dimension =  World.CODEC.parse(NbtOps.INSTANCE, compound.get("mainSpawnDimension")).resultOrPartial(SurvivalPlus.LOGGER::error).orElse(World.OVERWORLD);
                playerData.tempRespawn = new ServerPlayerEntity.Respawn(dimension, respawnPos, compound.getFloat("mainSpawnAngle", 0.0f), compound.getBoolean("mainSpawnForced", false));

                playerData.receivedBAWarningMessage = compound.getBoolean("receivedBAWarning").get();
                state.players.put(uuid, playerData);
            });
        }
        return state;
    }*/

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverAndLoader state = persistentStateManager.getOrCreate(TYPE);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        PersistentStateManager persistentStateManager = player.getServer().getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverAndLoader serverState = getServerState(player.getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> persistentStateManager.getOrCreate(PlayerData.TYPE));
    }
}
