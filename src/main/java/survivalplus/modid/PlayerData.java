package survivalplus.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import survivalplus.modid.world.baseassaults.BaseAssaultWaves;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerData extends StateSaverAndLoader{

    public static final MapCodec<PlayerData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.fieldOf("baseAssaultTimer").forGetter(playerData -> playerData.baseAssaultTimer),
                            Codec.BYTE_BUFFER.fieldOf("generatedWave").forGetter(playerData -> ByteBuffer.wrap(playerData.generatedWave)),
                            ServerPlayerEntity.Respawn.CODEC.optionalFieldOf("tempRespawn").forGetter(playerData -> Optional.ofNullable(playerData.tempRespawn)),
                            ServerPlayerEntity.Respawn.CODEC.optionalFieldOf("mainRespawn").forGetter(playerData -> Optional.ofNullable(playerData.mainRespawn)),
                            Codec.BOOL.fieldOf("receivedBAWarning").forGetter(playerData -> playerData.receivedBAWarningMessage)
                    ).apply(instance, PlayerData::new));

    public PlayerData(UUID uuid) {
        this.baseAssaultTimer = 0;
        this.generatedWave = BaseAssaultWaves.BASEASSAULT_TWELVE;
        this.tempRespawn = null;
        this.mainRespawn = null;
        this.receivedBAWarningMessage = false;
    }

    public PlayerData(int baseAssaultTimer, ByteBuffer generatedWave, Optional<ServerPlayerEntity.Respawn> tempRespawn, Optional<ServerPlayerEntity.Respawn> mainRespawn, boolean receivedBAWarningMessage){
        this.baseAssaultTimer = baseAssaultTimer;
        this.generatedWave = generatedWave.array();
        this.tempRespawn = tempRespawn.orElse(null);
        this.mainRespawn = mainRespawn.orElse(null);
        this.receivedBAWarningMessage = receivedBAWarningMessage;
    }

    public int baseAssaultTimer;
    public byte[] generatedWave;
    public ServerPlayerEntity.Respawn tempRespawn;
    public ServerPlayerEntity.Respawn mainRespawn;
    public boolean receivedBAWarningMessage;



    record UUIDWithPlayerData(UUID uuid, PlayerData playerData) {
        public static final Codec<UUIDWithPlayerData> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(Uuids.CODEC.fieldOf("uuid").forGetter(UUIDWithPlayerData::uuid), PlayerData.CODEC.forGetter(UUIDWithPlayerData::playerData))
                        .apply(instance, UUIDWithPlayerData::new)
        );

        public static UUIDWithPlayerData fromMapEntry(Map.Entry<UUID, PlayerData> entry) {
            return new UUIDWithPlayerData(entry.getKey(), entry.getValue());
        }
    }
}
