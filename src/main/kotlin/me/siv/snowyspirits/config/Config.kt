package me.siv.snowyspirits.config

import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
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
        this.translation = "config.snowyspirits.fun.weatherChanger"
    }

    var precipitation: Biome.Precipitation by enum(Biome.Precipitation.SNOW) {
        this.translation = "config.snowyspirits.fun.precipitation"
    }

    var noPrecipitationBlocking by boolean(true) {
        this.translation = "config.snowyspirits.fun.noPrecipitationBlocking"
    }

    var timeChanger by boolean(false) {
        this.translation = "config.snowyspirits.fun.timeChanger"
    }

    var time by long(0L) {
        this.translation = "config.snowyspirits.fun.time"
        this.range = 0L..24000L
    }
}