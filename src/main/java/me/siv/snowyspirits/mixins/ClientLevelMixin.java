package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @WrapOperation(
            method = "getSkyColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getSkyColorRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR
            ) {
                return 1.0f;
            }
        }
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getSkyColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getSkyColorThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER
            ) {
                return 1.0f;
            }
        }
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getCloudColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getCloudColorRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR
            ) {
                return 1.0f;
            }
        }
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getCloudColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getCloudColorThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER
            ) {
                return 1.0f;
            }
        }
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getSkyDarken",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getSkyDarkenRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR
            ) {
                return 1.0f;
            }
        }
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getSkyDarken",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getSkyDarkenThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER
            ) {
                return 1.0f;
            }
        }
        return original.call(instance, v);
    }
}
