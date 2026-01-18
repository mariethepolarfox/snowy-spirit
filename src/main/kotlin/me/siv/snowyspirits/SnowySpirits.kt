package me.siv.snowyspirits

import com.mojang.brigadier.CommandDispatcher
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import me.siv.snowyspirits.config.Config
import me.siv.snowyspirits.utils.LightningBoltUtil
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext
import net.minecraft.gametest.framework.TestEnvironmentDefinition
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.chunk.LevelChunk
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val MODID = "snowyspirits"

object SnowySpirits : ClientModInitializer, Logger by LoggerFactory.getLogger(MODID) {
    val mc: Minecraft = Minecraft.getInstance()

    val configurator = Configurator("snowyspirits")
    var config: ResourcefulConfig? = null

    val chunkList: MutableList<LevelChunk> = mutableListOf()

    override fun onInitializeClient() {
        config = Config.register(configurator)

        ClientCommandRegistrationCallback.EVENT.register(::onRegisterCommands)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            tickThunder()
            LightningBoltUtil.tickBolts()
        }

        // Handle chunks
        ClientChunkEvents.CHUNK_LOAD.register { level, chunk -> chunkList.add(chunk) }
        ClientChunkEvents.CHUNK_UNLOAD.register { level, chunk -> chunkList.remove(chunk) }
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { _, world -> chunkList.clear() }
    }

    private fun onRegisterCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        buildContext: CommandBuildContext
    ) {
        dispatcher.register(
            ClientCommandManager.literal("snowyspirits").executes { context ->
                mc.schedule { mc.setScreen(ResourcefulConfigScreen.make(config).build()) }
                1
            })
    }

    private fun tickThunder() {
        if (Config.weatherChanger && Config.weatherType == TestEnvironmentDefinition.Weather.Type.THUNDER) {
            chunkList.forEach(LightningBoltUtil::tickChunkThunder)
        }
    }
}