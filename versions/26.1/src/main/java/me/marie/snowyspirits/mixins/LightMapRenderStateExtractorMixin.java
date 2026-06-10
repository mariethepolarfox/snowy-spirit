package me.marie.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.marie.snowyspirits.config.Config;
import me.marie.snowyspirits.utils.EndFlashUtil;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapRenderStateExtractor.class)
public class LightMapRenderStateExtractorMixin {
    @ModifyExpressionValue(
            method = "extract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;endFlashState()Lnet/minecraft/client/renderer/EndFlashState;")
    )
    private EndFlashState endFlashState(EndFlashState original) {
        return Config.INSTANCE.getCustomEndFlashes() ? new EndFlashState() : original;
    }

    @ModifyExpressionValue(
            method = "extract",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EndFlashState;getIntensity(F)F")
    )
    private float extractIntensity(float original) {
        return Config.INSTANCE.getCustomEndFlashes() ? EndFlashUtil.endFlashState.getIntensity() : original;
    }
}
