package me.siv.snowyspirits.utils

import me.siv.snowyspirits.SnowySpirits.mc
import me.siv.snowyspirits.config.Config
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3
import kotlin.random.Random

object LightningBoltUtil {

    val boltEntities: MutableList<LightningBolt> = mutableListOf()

    fun tickChunkThunder(chunk: LevelChunk) {
        if (mc.level == null || Config.lightningChance == 0) return
        if (Random.nextDouble() > (1.0 / Config.lightningChance)) return

        val pos = chunk.pos
        val minX = pos.minBlockX
        val minZ = pos.minBlockZ

        val randomBlockPos = mc.level!!.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING,
            mc.level!!.getBlockRandomPos(minX, 0, minZ, 15),
        )

        val bolt = EntityType.LIGHTNING_BOLT.create(mc.level!!, EntitySpawnReason.EVENT) ?: return
        bolt.snapTo(Vec3.atBottomCenterOf(randomBlockPos))
        bolt.setVisualOnly(true)
        boltEntities.add(bolt)
    }

    fun tickBolts() {
        val list = boltEntities.toList()
        list.forEach(::tickBolt)
    }

    private fun tickBolt(bolt: LightningBolt) {
        if (bolt.life == 2) {
            mc.level?.playLocalSound(
                bolt.x,
                bolt.y,
                bolt.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER,
                10000f,
                0.8f + Random.nextFloat() * 0.2f,
                false
            )
            mc.level?.playLocalSound(
                bolt.x,
                bolt.y,
                bolt.z,
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.WEATHER,
                2f,
                0.5f + Random.nextFloat() * 0.2f,
                false
            )
        }

        bolt.life -= 1
        if (bolt.life < 0) {
            if (bolt.flashes == 0) {
                boltEntities.remove(bolt)
            } else if (bolt.life < -Random.nextInt(10)) {
                bolt.flashes -= 1
                bolt.life = 1
                bolt.seed = Random.nextLong()
            }
        }

        if (bolt.life >= 0) {
            mc.level?.setSkyFlashTime(2)
        }
    }

}