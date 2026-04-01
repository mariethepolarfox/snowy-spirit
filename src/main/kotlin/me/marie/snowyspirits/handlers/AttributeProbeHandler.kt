package me.marie.snowyspirits.handlers

import me.marie.snowyspirits.config.Config
import net.minecraft.world.attribute.EnvironmentAttribute
import net.minecraft.world.attribute.EnvironmentAttributes
import net.minecraft.world.level.MoonPhase

object AttributeProbeHandler {
    @JvmStatic
    fun <T : Any> handleProbe(probe: EnvironmentAttribute<T>, f: Float): T? {
        return when (probe) {
            EnvironmentAttributes.MOON_PHASE -> {
                if (Config.moonPhaseChanger) {
                    MoonPhase.entries[Config.moonPhase.phase] as? T
                } else {
                    null
                }
            }
            else -> null
        }
    }

}