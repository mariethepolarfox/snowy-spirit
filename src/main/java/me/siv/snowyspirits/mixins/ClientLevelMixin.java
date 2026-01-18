package me.siv.snowyspirits.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.siv.snowyspirits.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Override
    public float getRainLevel(float f) {
        if (Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger()&& Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return 1.0f;
        return super.getRainLevel(f);
    }

    @Override
    public float getThunderLevel(float f) {
        if (Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER) return 1.0f;
        return super.getThunderLevel(f);
    }

    @Override
    public boolean isRaining() {
        if (Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return true;
        return super.isRaining();
    }

    @Override
    public boolean isThundering() {
        if (Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER) return true;
        return super.isThundering();
    }

    @Override
    public Biome.@NotNull Precipitation precipitationAt(BlockPos blockPos) {
        if (Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger()) return Config.INSTANCE.getPrecipitation();
        return super.precipitationAt(blockPos);
    }

    @WrapOperation(
            method = "getSkyColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getSkyColorRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger()&& Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return 1.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getSkyColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getSkyColorThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER) return 1.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getCloudColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getCloudColorRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return 1.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getCloudColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getCloudColorThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER) return 1.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getSkyDarken",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float getSkyDarkenRainLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR) return 1.0f;
        return original.call(instance, v);
    }

    @WrapOperation(
            method = "getSkyDarken",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")
    )
    private float getSkyDarkenThunderLevel(ClientLevel instance, float v, Operation<Float> original) {
        if (!Config.INSTANCE.getIntrusive())
            if (Config.INSTANCE.getWeatherChanger() && Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER) return 1.0f;
        return original.call(instance, v);
    }
}
