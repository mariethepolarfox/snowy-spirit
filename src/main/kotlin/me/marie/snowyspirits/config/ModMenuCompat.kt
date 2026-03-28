package me.marie.snowyspirits.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.marie.snowyspirits.screen.ConfigScreen
import net.minecraft.client.gui.screens.Screen

class ModMenuCompat : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory<Screen> { parent: Screen? ->
            try {
                ConfigScreen()
            } catch (e: Exception) {
                println("Cannot open config before it is initialized")
                e.printStackTrace()
                parent
            }
        }
    }
}