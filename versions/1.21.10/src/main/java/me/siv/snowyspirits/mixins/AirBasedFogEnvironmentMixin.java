package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AirBasedFogEnvironment;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AirBasedFogEnvironment.class)
public class AirBasedFogEnvironmentMixin {
    @WrapOperation(
            method = "getBaseColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getBaseColorRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger())
                return Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR ? 1.0f : 0.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getBaseColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getBaseColorThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger())
                return Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER ? 1.0f : 0.0f;
        return original.call(instance, v);
    }
}
