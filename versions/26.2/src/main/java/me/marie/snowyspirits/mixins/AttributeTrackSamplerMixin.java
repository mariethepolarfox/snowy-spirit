package me.marie.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import me.marie.snowyspirits.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.timeline.AttributeTrackSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AttributeTrackSampler.class)
public class AttributeTrackSamplerMixin {
    @WrapOperation(
            method = "applyTimeBased",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/clock/ClockManager;getTotalTicks(Lnet/minecraft/core/Holder;)J"
            )
    )
    private long modifyTotalTicks(ClockManager instance, Holder<WorldClock> worldClockHolder, Operation<Long> original) {
        if (Config.INSTANCE.getTimeChanger() && RenderSystem.isOnRenderThread()) {
            return Config.INSTANCE.getTime() % 24000L;
        }
        return original.call(instance, worldClockHolder);
    }
}
