package survivalplus.modid.util;

public interface IServerPlayerChanger {
    byte[] getGeneratedWave();

    void setGeneratedWave(byte[] wave);

    void resetTimeSinceLastBaseAssault();

    void incrementTimeSinceLastBaseAssault();

    int getTimeSinceLastBaseAssault();

    void increaseTimeSinceLastBaseAssault(int time);
}
