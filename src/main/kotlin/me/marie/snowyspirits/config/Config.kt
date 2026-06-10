package me.marie.snowyspirits.config

import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import net.minecraft.gametest.framework.TestEnvironmentDefinition
import net.minecraft.world.level.biome.Biome

object Config : ConfigKt("snowyspirits/config") {
    override val name = Literal("Snowy Spirits")

    init {
        separator {
            title = "General"
            description = "Mrow :3"
        }
    }

    var weatherChanger by boolean(true) {
        this.translation = "config.snowyspirits.weatherChanger"
    }

    var weatherType: TestEnvironmentDefinition.Weather.Type by enum(TestEnvironmentDefinition.Weather.Type.RAIN) {
        this.translation = "config.snowyspirits.weatherType"
    }

    var precipitation: Biome.Precipitation by enum(Biome.Precipitation.SNOW) {
        this.translation = "config.snowyspirits.precipitation"
        this.condition = { weatherType != TestEnvironmentDefinition.Weather.Type.CLEAR }
    }

    var lightningChance by int(100000) {
        this.translation = "config.snowyspirits.lightningChance"
        this.condition = {  weatherType == TestEnvironmentDefinition.Weather.Type.THUNDER }
    }

    var noPrecipitationBlocking by boolean(true) {
        this.translation = "config.snowyspirits.noPrecipitationBlocking"
    }

    init {
        separator {
            title = "Time"
            description = "Mrow :3"
        }
    }

    var timeChanger by boolean(false) {
        this.translation = "config.snowyspirits.timeChanger"
    }

    var time by long(0L) {
        this.translation = "config.snowyspirits.time"
        this.range = 0L..24000L
    }

    var moonPhaseChanger by boolean(false) {
        this.translation = "config.snowyspirits.moonPhaseChanger"
    }

    var moonPhase by enum(MoonPhase.FULL_MOON) {
        this.translation = "config.snowyspirits.moonPhase"
    }

    init {
        separator {
            title = "Other"
            description = "Mrow :3"
        }
    }

    var skyTypeChanger by boolean(false) {
        this.translation = "config.snowyspirits.skyTypeChanger"
    }

    var skyType by enum(SkyType.END) {
        this.translation = "config.snowyspirits.skyType"
    }

    init {
        separator {
            title = "Custom End Flashes"
            description = "Mrow :3"
        }
    }

    var customEndFlashes by boolean(false) {
        this.translation = "config.snowyspirits.customEndFlashes"
    }

    var endFlashInterval by int(30) {
        this.translation = "config.snowyspirits.customEndFlashes.interval"
        this.range = 1..Int.MAX_VALUE
    }

    var minEndFlashDuration by int(5) {
        this.translation = "config.snowyspirits.customEndFlashes.minDuration"
        this.range = 1..Int.MAX_VALUE
    }

    var maxEndFlashDuration by int(19) {
        this.translation = "config.snowyspirits.customEndFlashes.maxDuration"
        this.range = 1..Int.MAX_VALUE
    }

    enum class MoonPhase(val phase: Int) {
        FULL_MOON(0),
        WANING_GIBBOUS(1),
        THIRD_QUARTER(2),
        WANING_CRESCENT(3),
        NEW_MOON(4),
        WAXING_CRESCENT(5),
        FIRST_QUARTER(6),
        WAXING_GIBBOUS(7)
    }

    enum class SkyType {
        NONE,
        OVERWORLD,
        END,
    }
}