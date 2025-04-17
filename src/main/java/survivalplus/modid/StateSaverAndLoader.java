package survivalplus.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            PlayerData.UUIDWithPlayerData.CODEC
                                    .listOf()
                                    .optionalFieldOf("players_data", List.of())
                                    .forGetter(stateSaverAndLoader -> stateSaverAndLoader.players.entrySet().stream().map(PlayerData.UUIDWithPlayerData::fromMapEntry).toList())
                    )
                    .apply(instance, StateSaverAndLoader::new)
    );

    public static final PersistentStateType<StateSaverAndLoader> TYPE = new PersistentStateType<>("stateSaverAndLoader", StateSaverAndLoader::new , CODEC, null);

    public HashMap<UUID, PlayerData> players;

    public StateSaverAndLoader(){
        this.players = new HashMap<>();
    }

    private StateSaverAndLoader(List<PlayerData.UUIDWithPlayerData> playerDataList){
        this.players = new HashMap<>();
        for(PlayerData.UUIDWithPlayerData player : playerDataList){
            this.players.put(player.uuid(), player.playerData());
        }
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverAndLoader state = persistentStateManager.getOrCreate(TYPE);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), PlayerData::new);
    }


}
