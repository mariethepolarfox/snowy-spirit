package me.marie.snowyspirits.mixins;

import me.marie.snowyspirits.config.Config;
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
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }

    @Override
    public float getRainLevel(float f) {
        if (Config.INSTANCE.getWeatherChanger())
            return Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR ? 1.0f : 0.0f;
        return super.getRainLevel(f);
    }

    @Override
    public float getThunderLevel(float f) {
        if (Config.INSTANCE.getWeatherChanger())
            return Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER ? 1.0f : 0.0f;
        return super.getThunderLevel(f);
    }

    @Override
    public boolean isRaining() {
        if (Config.INSTANCE.getWeatherChanger())
            return Config.INSTANCE.getWeatherType() != TestEnvironmentDefinition.Weather.Type.CLEAR;
        return super.isRaining();
    }

    @Override
    public boolean isThundering() {
        if (Config.INSTANCE.getWeatherChanger())
            return Config.INSTANCE.getWeatherType() == TestEnvironmentDefinition.Weather.Type.THUNDER;
        return super.isThundering();
    }

    @Override
    public Biome.@NotNull Precipitation precipitationAt(@NonNull BlockPos blockPos) {
        if (Config.INSTANCE.getWeatherChanger()) return Config.INSTANCE.getPrecipitation();
        return super.precipitationAt(blockPos);
    }
}
