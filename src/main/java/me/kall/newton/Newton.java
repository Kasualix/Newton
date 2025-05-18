package me.kall.newton;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.UUID;

@Mod("newton")
public class Newton {
    public static final Set<UUID> APPLES = new ObjectOpenHashSet<>();
    public static final Logger LOGGER = LogManager.getLogger("Newton");

    public Newton() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
    }

    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.IntValue POSSIBILITY, MAX_COUNT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Newton");
        MAX_COUNT = builder.comment("Max apples that this mod will generate. Once this is reached, all generated apples in the world will be cleared and the generation and counting will be reset").defineInRange("MaxCount", 100, 0, Integer.MAX_VALUE);
        POSSIBILITY = builder.comment("Basic possibility for an apple to appear. Calculation: 1/TheNumberHere").defineInRange("PossibilityNumber", 1500, 0, Integer.MAX_VALUE);
        builder.pop();
        CONFIG = builder.build();
    }
}
