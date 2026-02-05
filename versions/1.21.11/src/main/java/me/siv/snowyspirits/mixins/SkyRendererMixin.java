package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;skybox()Lnet/minecraft/world/level/dimension/DimensionType$Skybox;")
    )
    private DimensionType.Skybox extractRenderStateSkyType(DimensionType instance, Operation<DimensionType.Skybox> original) {
        return Config.INSTANCE.getSkyTypeChanger() ? DimensionType.Skybox.values()[Config.INSTANCE.getSkyType().ordinal()] : original.call(instance);
    }
}
