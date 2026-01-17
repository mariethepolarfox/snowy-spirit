package me.siv.snowyspirit.config

import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import net.minecraft.world.level.biome.Biome

object Config : ConfigKt("snowyspirit/config") {
    override val name = Literal("Snowy Spirit")

    init {
        separator {
            title = "General"
            description = "Mrow :3"
        }
    }

    var weatherChanger by boolean(true) {
        this.translation = "config.snowyspirit.fun.weatherChanger"
    }

    var precipitation: Biome.Precipitation by enum(Biome.Precipitation.SNOW) {
        this.translation = "config.snowyspirit.fun.precipitation"
    }

    var noPrecipitationBlocking by boolean(true) {
        this.translation = "config.snowyspirit.fun.noPrecipitationBlocking"
    }

    var timeChanger by boolean(false) {
        this.translation = "config.snowyspirit.fun.timeChanger"
    }

    var time by long(0L) {
        this.translation = "config.snowyspirit.fun.time"
        this.range = 0L..24000L
    }
}