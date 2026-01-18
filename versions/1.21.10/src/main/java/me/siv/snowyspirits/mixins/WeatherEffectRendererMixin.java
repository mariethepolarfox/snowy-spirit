package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
    @ModifyReturnValue(
            method = "getPrecipitationAt",
            at = @At("RETURN")
    )
    private Biome.Precipitation getPrecipitationAt(Biome.Precipitation original) {
        return Config.INSTANCE.getWeatherChanger() ? Config.INSTANCE.getPrecipitation() : original;
    }

    @WrapOperation(
            method = "tickRainParticles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float tickRainParticlesRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return 1.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getRainLevel(F)F")
    )
    private float extractRenderStateRainLevel(Level instance, float f, Operation<Float> original) {
        if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return 1.0f;
        return original.call(instance, f);
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I")
    )
    private int extractRenderStateGetHeight(Level instance, net.minecraft.world.level.levelgen.Heightmap.Types heightmapType, int x, int z, Operation<Integer> original) {
        return Config.INSTANCE.getNoPrecipitationBlocking() ? instance.getMinY() : original.call(instance, heightmapType, x, z);
    }
}
