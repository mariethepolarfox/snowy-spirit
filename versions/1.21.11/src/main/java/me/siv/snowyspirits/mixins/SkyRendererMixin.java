package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float extractRenderStateRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger())
                return Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR ? 1.0f : 0.0f;
        return original.call(instance, v);
    }
}
