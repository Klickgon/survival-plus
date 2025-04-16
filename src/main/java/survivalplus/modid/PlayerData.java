package survivalplus.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentStateType;
import survivalplus.modid.world.baseassaults.BaseAssaultWaves;

import java.nio.ByteBuffer;

public class PlayerData extends StateSaverAndLoader{

    public static final PersistentStateType<PlayerData> TYPE = new PersistentStateType<>(
            "playerdataSP",
            PlayerData::new,
            PlayerData.CODEC,
            DataFixTypes.PLAYER
    );

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("baseAssaultTimer").forGetter(playerData -> playerData.baseAssaultTimer),
                            Codec.BYTE_BUFFER.fieldOf("generatedWave").forGetter(playerData -> ByteBuffer.wrap(playerData.generatedWave)),
                            ServerPlayerEntity.Respawn.CODEC.fieldOf("tempRespawn").forGetter(playerData -> playerData.tempRespawn),
                            ServerPlayerEntity.Respawn.CODEC.fieldOf("mainRespawn").forGetter(playerData -> playerData.mainRespawn),
                            Codec.BOOL.fieldOf("receivedBAWarning").forGetter(playerData -> playerData.receivedBAWarningMessage)
                    )
                    .apply(instance, PlayerData::new)
    );

    PlayerData(){
        this.baseAssaultTimer = 0;
        this.generatedWave = BaseAssaultWaves.BASEASSAULT_TWELVE;
        this.tempRespawn = null;
        this.mainRespawn = null;
        this.receivedBAWarningMessage = false;
    }

    PlayerData(int baseAssaultTimer, ByteBuffer generatedWave, ServerPlayerEntity.Respawn tempRespawn, ServerPlayerEntity.Respawn mainRespawn, boolean receivedBAWarningMessage){
        this.baseAssaultTimer = baseAssaultTimer;
        this.generatedWave = generatedWave.array();
        this.tempRespawn = tempRespawn;
        this.mainRespawn = mainRespawn;
        this.receivedBAWarningMessage = receivedBAWarningMessage;
    }

    public int baseAssaultTimer;
    public byte[] generatedWave;
    public ServerPlayerEntity.Respawn tempRespawn;
    public ServerPlayerEntity.Respawn mainRespawn;
    public boolean receivedBAWarningMessage;
}
