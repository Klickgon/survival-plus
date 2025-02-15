package survivalplus.modid.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModSounds {

    public static final SoundEvent BASE_ASSAULT_WARNING = registerSoundEvent("base_assault_warning");

    private static SoundEvent registerSoundEvent(String name){
        Identifier id = Identifier.of(SurvivalPlus.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds(){
    }

}
