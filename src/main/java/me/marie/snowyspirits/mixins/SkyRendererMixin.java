package me.marie.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.marie.snowyspirits.config.Config;
import me.marie.snowyspirits.utils.EndFlashUtil;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;skybox()Lnet/minecraft/world/level/dimension/DimensionType$Skybox;")
    )
    private DimensionType.Skybox extractRenderStateSkyType(DimensionType.Skybox original) {
        return Config.INSTANCE.getSkyTypeChanger() ? DimensionType.Skybox.values()[Config.INSTANCE.getSkyType().ordinal()] : original;
    }

    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;endFlashState()Lnet/minecraft/client/renderer/EndFlashState;")
    )
    private EndFlashState endFlashState(EndFlashState original) {
        return Config.INSTANCE.getCustomEndFlashes() ? new EndFlashState() : original;
    }

    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EndFlashState;getIntensity(F)F")
    )
    private float extractIntensity(float original) {
        return Config.INSTANCE.getCustomEndFlashes() ? EndFlashUtil.endFlashState.getIntensity() : original;
    }

    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EndFlashState;getXAngle()F")
    )
    private float extractXAngle(float original) {
        return Config.INSTANCE.getCustomEndFlashes() ? EndFlashUtil.endFlashState.getXAngle() : original;
    }

    @ModifyExpressionValue(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EndFlashState;getYAngle()F")
    )
    private float extractYAngle(float original) {
        return Config.INSTANCE.getCustomEndFlashes() ? EndFlashUtil.endFlashState.getYAngle() : original;
    }
}
