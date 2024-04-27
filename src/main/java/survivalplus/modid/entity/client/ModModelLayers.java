package survivalplus.modid.entity.client;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import survivalplus.modid.SurvivalPlus;

public class ModModelLayers {
    public static final EntityModelLayer REEPER =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"reeper"),"main");
    public static final EntityModelLayer BUILDERZOMBIE =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"builderzombie"),"main");

    public static final EntityModelLayer MINERZOMBIE =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"minerzombie"),"main");

    public static final EntityModelLayer LUMBERJACKZOMBIE =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"lumberjackzombie"),"main");

    public static final EntityModelLayer DIGGINGZOMBIE =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"diggingzombie"),"main");

    public static final EntityModelLayer SCORCHEDSKELETON =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"scorchedskeleton"),"main");

    public static final EntityModelLayer LEAPINGSPIDER =
            new EntityModelLayer(new Identifier(SurvivalPlus.MOD_ID,"leapingspider"),"main");
}
