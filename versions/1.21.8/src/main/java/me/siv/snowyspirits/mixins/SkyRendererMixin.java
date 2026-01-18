package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @WrapOperation(
            method = "renderSunMoonAndStars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(IFLnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void extractRenderStateMoonPhase(SkyRenderer instance, int i, float f, MultiBufferSource multiBufferSource, PoseStack poseStack, Operation<Void> original) {
        float brightness = f;
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR
            ) {
                brightness = 0F;
            }
        }
        int moonPhase = Config.INSTANCE.getMoonPhaseChanger() ? Config.INSTANCE.getMoonPhase().getPhase() : i;
        original.call(instance, moonPhase, brightness, multiBufferSource, poseStack);
    }

    @WrapOperation(
            method = "renderSunMoonAndStars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void extractRenderStateSun(SkyRenderer instance, float f, MultiBufferSource multiBufferSource, PoseStack poseStack, Operation<Void> original) {
        float brightness = f;
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR
            ) {
                brightness = 0F;
            }
        }
        original.call(instance, brightness, multiBufferSource, poseStack);
    }

    @WrapOperation(
            method = "renderSunMoonAndStars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void extractRenderStateStars(SkyRenderer instance, float f, PoseStack poseStack, Operation<Void> original) {
        float brightness = f;
        if (Config.INSTANCE.getWeatherChanger()) {
            if (Config.INSTANCE.getPrecipitation() == Biome.Precipitation.RAIN
                    && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR
            ) {
                brightness = 0F;
            }
        }
        original.call(instance, brightness, poseStack);
    }
}
