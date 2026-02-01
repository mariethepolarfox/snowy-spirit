package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.level.MoonPhase;
import org.objectweb.asm.Opcodes;
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

    @WrapOperation(
            method = "extractRenderState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/state/SkyRenderState;moonPhase:Lnet/minecraft/world/level/MoonPhase;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void extractRenderStateMoonPhase(SkyRenderState instance, MoonPhase value, Operation<Void> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getMoonPhaseChanger())
                instance.moonPhase = MoonPhase.values()[Config.INSTANCE.getMoonPhase().getPhase()];
            else
                instance.moonPhase = value;
    }
}
