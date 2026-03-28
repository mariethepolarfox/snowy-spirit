package me.marie.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.marie.snowyspirits.config.Config;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I")
    )
    private int extractRenderStateGetHeight(Level instance, net.minecraft.world.level.levelgen.Heightmap.Types heightmapType, int x, int z, Operation<Integer> original) {
        return Config.INSTANCE.getNoPrecipitationBlocking() ? instance.getMinY() : original.call(instance, heightmapType, x, z);
    }
}
