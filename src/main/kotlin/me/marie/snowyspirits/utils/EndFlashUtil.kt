package me.marie.snowyspirits.utils

import me.marie.snowyspirits.SnowySpirits.mc
import me.marie.snowyspirits.config.Config
import net.minecraft.client.gui.screens.WinScreen
import net.minecraft.client.resources.sounds.DirectionalSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import kotlin.math.sin
import kotlin.random.Random

object EndFlashUtil {
    @JvmField
    var endFlashState = CustomEndFlashState()

    fun resetState() {
        endFlashState.flashSeed = 0L
    }

    fun tick() {
        if (mc.isPaused) return
        if (!Config.customEndFlashes) return
        if (!Config.skyTypeChanger) return
        if (Config.skyType != Config.SkyType.END) return
        endFlashState.tick()
    }

}

class CustomEndFlashState {
    val clockTime get() = System.currentTimeMillis()

    var flashSeed = 0L
    var offset = 0L
    var duration = 1L
    var soundPlayed = false
    var xAngle = 0f
    var yAngle = 0f

    fun tick() {
        val newSeed = clockTime / (Config.endFlashInterval * 1000L)
        if (flashSeed != newSeed) {
            val intervalMs = Config.endFlashInterval * 1000L
            val maxDurationMs = Config.maxEndFlashDuration * 1000L
            val minDurationMs = (Config.minEndFlashDuration * 1000L).coerceAtMost(maxDurationMs - 1)

            val maxOffsetMs = intervalMs / 3

            offset = Random.nextLong(0L, maxOffsetMs + 1)

            val durationUpperBound = minOf(
                maxDurationMs,
                intervalMs - offset
            )

            duration = if (durationUpperBound <= minDurationMs) {
                durationUpperBound.coerceAtLeast(1L)
            } else {
                Random.nextLong(
                    minDurationMs,
                    durationUpperBound + 1
                )
            }

            soundPlayed = false
            xAngle = Random.nextDouble(-60.0, 10.0).toFloat()
            yAngle = Random.nextDouble(-180.0, 180.0).toFloat()
            flashSeed = newSeed
        }
    }

    fun getIntensity(): Float {
        val clockTimeWithinInterval = clockTime % (Config.endFlashInterval * 1000L)
        val intensity = if (clockTimeWithinInterval >= offset && clockTimeWithinInterval <= (offset + duration)) {
            sin(((clockTimeWithinInterval - offset).toDouble() * Math.PI) / duration).toFloat()
        } else {
            0f
        }

        if (intensity > 0.3f && !soundPlayed) {
            soundPlayed = true
            if (mc.screen is WinScreen || mc.isPaused) return intensity
            playSound()
        }
        return intensity
    }

    private fun playSound() {
        mc.soundManager.playDelayed(
            DirectionalSoundInstance(
                SoundEvents.WEATHER_END_FLASH,
                SoundSource.WEATHER,
                RandomSource.create(flashSeed),
                mc.gameRenderer.mainCamera,
                xAngle,
                yAngle,
            ),
            30,
        )
    }
}
