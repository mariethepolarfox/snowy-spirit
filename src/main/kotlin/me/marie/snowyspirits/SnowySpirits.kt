package me.marie.snowyspirits

import com.mojang.brigadier.CommandDispatcher
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import me.marie.snowyspirits.config.Config
import me.marie.snowyspirits.screen.ConfigScreen
import me.marie.snowyspirits.utils.EndFlashUtil
import me.marie.snowyspirits.utils.LightningUtil
import net.fabricmc.api.ClientModInitializer
//? if <=1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands*/
//? } else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
//? }
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//? if <=1.21.11 {
/*import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents as ClientLevelEvents*/
//? } else {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents
//? }
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext
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
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            LightningUtil.tickThunder(chunkList.toList())
            EndFlashUtil.tick()
        }

        // Handle chunks
        ClientChunkEvents.CHUNK_LOAD.register { _, chunk -> chunkList.add(chunk) }
        ClientChunkEvents.CHUNK_UNLOAD.register { _, chunk -> chunkList.remove(chunk) }

        ClientLevelEvents
            ./*? if <=1.21.11 {*//*AFTER_CLIENT_WORLD_CHANGE*//*? } else {*/AFTER_CLIENT_LEVEL_CHANGE/*? }*/
            .register { _, _ -> chunkList.clear() }
    }

    private fun onRegisterCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        buildContext: CommandBuildContext
    ) {
        dispatcher.register(
            ClientCommands.literal("snowyspirits").executes { _ ->
                mc.schedule { mc.setScreen(ConfigScreen()) }
                1
            })
    }

    fun saveConfig() {
        config?.save()
    }
}