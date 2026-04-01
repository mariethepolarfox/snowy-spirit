package me.marie.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.marie.snowyspirits.handlers.AttributeProbeHandler;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnvironmentAttributeProbe.class)
public class EnvironmentAttributeProbeMixin {
    @WrapOperation(
            method = "getValue",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/EnvironmentAttributeProbe$ValueProbe;get(Lnet/minecraft/world/attribute/EnvironmentAttribute;F)Ljava/lang/Object;")
    )
    private <T> T modifyGetValueReturn(EnvironmentAttributeProbe.ValueProbe instance, EnvironmentAttribute<T> environmentAttribute, float f, Operation<T> original) {
        T value = AttributeProbeHandler.handleProbe(environmentAttribute, f);
        if (value != null) {
            return value;
        }
        return original.call(instance, environmentAttribute, f);
    }
}
