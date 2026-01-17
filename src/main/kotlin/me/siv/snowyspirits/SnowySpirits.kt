package me.siv.snowyspirits

import com.mojang.brigadier.CommandDispatcher
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import me.siv.snowyspirits.config.Config
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val MODID = "snowyspirits"

object SnowySpirits : ClientModInitializer, Logger by LoggerFactory.getLogger(MODID) {
    val mc: Minecraft = Minecraft.getInstance()

    val configurator = Configurator("snowyspirits")
    var config: ResourcefulConfig? = null

    override fun onInitializeClient() {
        config = Config.register(configurator)
        ClientCommandRegistrationCallback.EVENT.register(::onRegisterCommands)
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
}