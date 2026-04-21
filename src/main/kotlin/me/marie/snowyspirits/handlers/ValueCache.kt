package me.marie.snowyspirits.handlers

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.Heightmap

object ValueCache {
    @JvmStatic
    var rainLevel = 0f

    private fun cachedIsRaining(level: Level): Boolean {
        return level.canHaveWeather() && rainLevel > 0.2f
    }

    @JvmStatic
    fun isRainingAt(pos: BlockPos, level: Level): Boolean {
        return if (!cachedIsRaining(level)) {
            false
        } else if (!level.canSeeSky(pos)) {
            false
        } else if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).y > pos.y) {
            false
        } else {
            val biome = level.getBiome(pos).value()
            biomePrecipitation(biome, pos, level.seaLevel) == Biome.Precipitation.RAIN
        }
    }

    fun biomePrecipitation(biome: Biome, pos: BlockPos, seaLevel: Int): Biome.Precipitation {
        return if (!biome.hasPrecipitation()) {
            Biome.Precipitation.NONE
        } else {
            if (biome.coldEnoughToSnow(pos, seaLevel)) Biome.Precipitation.SNOW else Biome.Precipitation.RAIN
        }
    }
}