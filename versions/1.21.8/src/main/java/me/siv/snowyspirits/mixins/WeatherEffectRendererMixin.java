package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
    @WrapOperation(
            method = "collectColumnInstances",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I")
    )
    private int extractRenderStateGetHeight(Level instance, Heightmap.Types types, int i, int j, Operation<Integer> original) {
        return Config.INSTANCE.getNoPrecipitationBlocking() ? instance.getMinY() : original.call(instance, types, i, j);
    }
}
