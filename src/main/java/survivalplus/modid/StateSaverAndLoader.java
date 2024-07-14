package survivalplus.modid;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
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
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("baseAssaultTimer", playerData.baseAssaultTimer);
            playerNbt.putByteArray("generatedWave", playerData.generatedWave);
            if(playerData.tempSpawnPosition != null){
                playerNbt.putInt("tempSpawnPositionX", playerData.tempSpawnPosition.getX());
                playerNbt.putInt("tempSpawnPositionY", playerData.tempSpawnPosition.getY());
                playerNbt.putInt("tempSpawnPositionZ", playerData.tempSpawnPosition.getZ());
            }
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, playerData.tempSpawnDimension.getValue()).resultOrPartial(SurvivalPlus.LOGGER::error).ifPresent(encoded -> nbt.put("tempSpawnDimension", (NbtElement)encoded));
            playerNbt.putFloat("tempSpawnAngle", playerData.tempSpawnAngle);
            playerNbt.putBoolean("tempSpawnForced", playerData.tempSpawnForced);

            if(playerData.mainSpawnPosition != null){
                playerNbt.putInt("mainSpawnPositionX", playerData.mainSpawnPosition.getX());
                playerNbt.putInt("mainSpawnPositionY", playerData.mainSpawnPosition.getY());
                playerNbt.putInt("mainSpawnPositionZ", playerData.mainSpawnPosition.getZ());
            }
            Identifier.CODEC.encodeStart(NbtOps.INSTANCE, playerData.mainSpawnDimension.getValue()).resultOrPartial(SurvivalPlus.LOGGER::error).ifPresent(encoded -> nbt.put("mainSpawnDimension", (NbtElement)encoded));
            playerNbt.putFloat("mainSpawnAngle", playerData.mainSpawnAngle);
            playerNbt.putBoolean("mainSpawnForced", playerData.mainSpawnForced);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public HashMap<UUID, PlayerData> players = new HashMap<>();

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            UUID uuid = UUID.fromString(key);
            PlayerData playerData = new PlayerData();
            if(playersNbt.contains("baseAssaultTimer")) playerData.baseAssaultTimer = playersNbt.getCompound(key).getInt("baseAssaultTimer");
            if(playersNbt.contains("generatedWave")) playerData.generatedWave = playersNbt.getCompound(key).getByteArray("generatedWave");
            if(playersNbt.contains("tempSpawnPositionX") && playersNbt.contains("tempSpawnPositionY") && playersNbt.contains("tempSpawnPositionZ")) {
                int x = playersNbt.getCompound(key).getInt("tempSpawnPositionX");
                int y = playersNbt.getCompound(key).getInt("tempSpawnPositionY");
                int z = playersNbt.getCompound(key).getInt("tempSpawnPositionZ");
                playerData.tempSpawnPosition = new BlockPos(x, y, z);
            }
            if(playersNbt.contains("tempSpawnAngle")) playerData.tempSpawnAngle = playersNbt.getCompound(key).getFloat("tempSpawnAngle");
            if(playersNbt.contains("tempSpawnForced")) playerData.tempSpawnForced = playersNbt.getCompound(key).getBoolean("tempSpawnForced");
            if(playersNbt.contains("tempSpawnDimension"))
                World.CODEC.parse(NbtOps.INSTANCE, playersNbt.get("tempSpawnDimension")).resultOrPartial(SurvivalPlus.LOGGER::error).orElse(World.OVERWORLD);

            if(playersNbt.contains("mainSpawnPositionX") && playersNbt.contains("mainSpawnPositionY") && playersNbt.contains("mainSpawnPositionZ")) {
                int x = playersNbt.getCompound(key).getInt("mainSpawnPositionX");
                int y = playersNbt.getCompound(key).getInt("mainSpawnPositionY");
                int z = playersNbt.getCompound(key).getInt("mainSpawnPositionZ");
                playerData.mainSpawnPosition = new BlockPos(x, y, z);
            }
            if(playersNbt.contains("mainSpawnAngle")) playerData.mainSpawnAngle = playersNbt.getCompound(key).getFloat("mainSpawnAngle");
            if(playersNbt.contains("mainSpawnForced")) playerData.mainSpawnForced = playersNbt.getCompound(key).getBoolean("mainSpawnForced");
            if(playersNbt.contains("mainSpawnDimension"))
                World.CODEC.parse(NbtOps.INSTANCE, playersNbt.get("mainSpawnDimension")).resultOrPartial(SurvivalPlus.LOGGER::error).orElse(World.OVERWORLD);

            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static Type<StateSaverAndLoader> type = new Type<>(
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
