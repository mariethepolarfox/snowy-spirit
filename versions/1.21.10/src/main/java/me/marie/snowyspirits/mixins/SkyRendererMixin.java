package me.marie.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.marie.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I")
    )
    private int extractRenderStateMoonPhase(ClientLevel instance, Operation<Integer> original) {
        return Config.INSTANCE.getMoonPhaseChanger() ? Config.INSTANCE.getMoonPhase().getPhase() : original.call(instance);
    }

    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;skyType()Lnet/minecraft/client/renderer/DimensionSpecialEffects$SkyType;")
    )
    private DimensionSpecialEffects.SkyType extractRenderStateSkyType(DimensionSpecialEffects instance, Operation<DimensionSpecialEffects.SkyType> original) {
        return Config.INSTANCE.getSkyTypeChanger() ? DimensionSpecialEffects.SkyType.values()[Config.INSTANCE.getSkyType().ordinal()] : original.call(instance);
    }
}
