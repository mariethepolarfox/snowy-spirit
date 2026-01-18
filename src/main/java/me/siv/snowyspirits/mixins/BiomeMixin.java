package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import me.siv.snowyspirits.config.Config;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Biome.class)
public class BiomeMixin {
    @ModifyReturnValue(
            method = "getPrecipitationAt",
            at = @At("RETURN")
    )
    private Biome.Precipitation getPrecipitationAt(Biome.Precipitation original) {
        if (Config.INSTANCE.getIntrusive() && RenderSystem.isOnRenderThread())
            if (Config.INSTANCE.getWeatherChanger()) return Config.INSTANCE.getPrecipitation();
        return original;
    }
}
