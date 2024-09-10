package survivalplus.modid;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        NbtCompound playersNbt = new NbtCompound();
        this.players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("baseAssaultTimer", playerData.baseAssaultTimer);
            playerNbt.putByteArray("generatedWave", playerData.generatedWave);
            if(playerData.tempSpawnPosition != null){
                playerNbt.putInt("tempSpawnPositionX", playerData.tempSpawnPosition.getX());
                playerNbt.putInt("tempSpawnPositionY", playerData.tempSpawnPosition.getY());
                playerNbt.putInt("tempSpawnPositionZ", playerData.tempSpawnPosition.getZ());
            }
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, playerData.tempSpawnDimension.getValue()).resultOrPartial(SurvivalPlus.LOGGER::error).ifPresent(encoded -> playerNbt.put("tempSpawnDimension", encoded));
            playerNbt.putFloat("tempSpawnAngle", playerData.tempSpawnAngle);
            playerNbt.putBoolean("tempSpawnForced", playerData.tempSpawnForced);

            if(playerData.mainSpawnPosition != null){
                playerNbt.putInt("mainSpawnPositionX", playerData.mainSpawnPosition.getX());
                playerNbt.putInt("mainSpawnPositionY", playerData.mainSpawnPosition.getY());
                playerNbt.putInt("mainSpawnPositionZ", playerData.mainSpawnPosition.getZ());
            }
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, playerData.mainSpawnDimension.getValue()).resultOrPartial(SurvivalPlus.LOGGER::error).ifPresent(encoded -> playerNbt.put("mainSpawnDimension", encoded));
            playerNbt.putFloat("mainSpawnAngle", playerData.mainSpawnAngle);
            playerNbt.putBoolean("mainSpawnForced", playerData.mainSpawnForced);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public HashMap<UUID, PlayerData> players = new HashMap<>();

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            UUID uuid = UUID.fromString(key);
            PlayerData playerData = new PlayerData();
            NbtCompound compound = playersNbt.getCompound(key);
            playerData.baseAssaultTimer = compound.getInt("baseAssaultTimer");
            if(compound.contains("generatedWave")) playerData.generatedWave = compound.getByteArray("generatedWave");

            if(compound.contains("tempSpawnPositionX") && compound.contains("tempSpawnPositionY") && compound.contains("tempSpawnPositionZ")) {
                int x = compound.getInt("tempSpawnPositionX");
                int y = compound.getInt("tempSpawnPositionY");
                int z = compound.getInt("tempSpawnPositionZ");
                playerData.tempSpawnPosition = new BlockPos(x, y, z);
            }

            playerData.tempSpawnAngle = compound.getFloat("tempSpawnAngle");
            playerData.tempSpawnForced = compound.getBoolean("tempSpawnForced");

            World.CODEC.parse(NbtOps.INSTANCE, compound.get("tempSpawnDimension")).resultOrPartial(SurvivalPlus.LOGGER::error).orElse(World.OVERWORLD);

            if(compound.contains("mainSpawnPositionX") && compound.contains("mainSpawnPositionY") && compound.contains("mainSpawnPositionZ")) {
                int x = compound.getInt("mainSpawnPositionX");
                int y = compound.getInt("mainSpawnPositionY");
                int z = compound.getInt("mainSpawnPositionZ");
                playerData.mainSpawnPosition = new BlockPos(x, y, z);
            }

            playerData.mainSpawnAngle = compound.getFloat("mainSpawnAngle");
            playerData.mainSpawnForced = compound.getBoolean("mainSpawnForced");

            World.CODEC.parse(NbtOps.INSTANCE, compound.get("mainSpawnDimension")).resultOrPartial(SurvivalPlus.LOGGER::error).orElse(World.OVERWORLD);

            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static final Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, SurvivalPlus.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());
        PlayerData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
        return playerState;
    }
}
