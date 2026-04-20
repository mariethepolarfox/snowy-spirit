package me.marie.snowyspirits.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.compound.radio.RadioState
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import me.marie.snowyspirits.SnowySpirits
import me.marie.snowyspirits.SnowySpirits.mc
import me.marie.snowyspirits.config.Config
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.data.AtlasIds
import net.minecraft.gametest.framework.TestEnvironmentDefinition.Weather
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.level.biome.Biome

class ConfigScreen : Overlay(null) {
    private var screen: Layout? = null
    private var tab = 0

    private val tabRadioState = RadioState.of("Weather", 0)
    private val timePresetRadioState = RadioState.empty<String>()

    override fun init() {
        super.init()

        val weatherDropdownState = DropdownState.of(Config.weatherType)
        val precipitationDropdownState = DropdownState.of(Config.precipitation)
        val skyTypeState = DropdownState.of(Config.skyType)

        val lightningChanceState = ListenableState<Int>.of(Config.lightningChance)
        lightningChanceState.registerListener { newValue ->
            Config.lightningChance = newValue
        }

        val timeState = ListenableState<Int>.of(Config.time.toInt())
        timeState.registerListener { newValue ->
            val long = newValue.toLong().coerceIn(0L, 24000L)
            Config.time = long
        }

        val moonPhaseState = ListenableState<Int>.of(Config.moonPhase.ordinal)
        moonPhaseState.registerListener { newValue ->
            Config.moonPhase = Config.MoonPhase.entries[newValue]
        }

        val mainScrollableList = ListWidget(width / 4 - 10, height / 3 - mc.font.lineHeight * 2 - 20)

        var weatherChangerFrameHeight = 30

        var weatherChangerWeatherType: LayoutElement? = null
        var weatherChangerPrecipitationType: LayoutElement? = null
        var lightningChance: LayoutElement? = null

        if (Config.weatherChanger) {
            weatherChangerFrameHeight += 25
            weatherChangerWeatherType = labeledDropdown(
                "config.snowyspirits.weatherType",
                weatherDropdownState,
                Weather.Type.entries,
            ) { Config.weatherType = it }

            weatherChangerFrameHeight += 25
            weatherChangerPrecipitationType = labeledDropdown(
                "config.snowyspirits.precipitation",
                precipitationDropdownState,
                Biome.Precipitation.entries,
                { Component.literal(it.name.lowercase().replaceFirstChar { char -> char.uppercase() }) }
            ) { Config.precipitation = it }

            if (Config.weatherType == Weather.Type.THUNDER) {
                weatherChangerFrameHeight += 25
                lightningChance = labeledSlider(
                    "config.snowyspirits.lightningChance",
                    lightningChanceState,
                    0,
                    1_000_000
                )
            }
        }

        val noPrecipitationBlockingToggle = labeledToggle(
            "config.snowyspirits.noPrecipitationBlocking",
            { Config.noPrecipitationBlocking },
            "Precipitation Blocking"
        ) { newVal ->
            Config.noPrecipitationBlocking = newVal
        }

        val weatherInnerContent = Layouts.column().withGap(5).apply {
            if (weatherChangerWeatherType != null) this.withChild(weatherChangerWeatherType)
            if (weatherChangerPrecipitationType != null) this.withChild(weatherChangerPrecipitationType)
            if (lightningChance != null) this.withChild(lightningChance)
        }

        var timeChangerFrameHeight = 30

        var timeInput: LayoutElement? = null
        var timePreset: LayoutElement? = null

        if (Config.timeChanger) {
            timeChangerFrameHeight += 25
            timeInput = labeledSlider(
                "config.snowyspirits.time",
                timeState,
                0,
                24000
            )

            timeChangerFrameHeight += 29
            timePreset = Widgets.radio(
                timePresetRadioState,
                { builder ->
                    builder.withOption("Sunrise")
                        .withOption("Noon")
                        .withOption("Sunset")
                        .withOption("Midnight")
                        .withSize(mainScrollableList.width - 20, 24)
                        .withoutEntrySprites()
                        .withRenderer { string, bool ->
                            val timeString = ((((getTimeFromString(string) + 18000L) % 24000L) / 24000.0) * 64).toInt().toString().padStart(2, '0')
                            WidgetRenderers.layered(
                                WidgetRenderers.sprite(if (bool) UIConstants.PRIMARY_BUTTON else UIConstants.BUTTON),
                                texture(
                                    Identifier.withDefaultNamespace("textures/item/clock_$timeString.png")
                                )
                            )
                        }
                        .withCallback { newVal ->
                            val time = getTimeFromString(newVal)
                            Config.time = time
                            this.rebuildWidgets()
                        }
                }
            ) { }
        }

        val timeInnerContent = Layouts.column()
            .withGap(5)
            .apply {
                if (timeInput != null) this.withChild(timeInput)
                if (timePreset != null) this.withChild(timePreset)
            }

        var moonChangerFrameHeight = 30

        var moonPhaseWidget: LayoutElement? = null
        var moonRenderer: LayoutElement? = null

        if (Config.moonPhaseChanger) {
            moonChangerFrameHeight += 25
            moonPhaseWidget = labeledUnlabeledSlider(
                "config.snowyspirits.moonPhase",
                moonPhaseState,
                0,
                Config.MoonPhase.entries.size - 1
            )

            moonChangerFrameHeight += 69
            moonRenderer = Widgets.renderable { graphics, context, _ ->
                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    mc.atlasManager.getAtlasOrThrow(AtlasIds.CELESTIALS)
                        .getSprite(Identifier.withDefaultNamespace("moon/${Config.moonPhase.name.lowercase()}")),
                    context.x + (context.width - 64) / 2,
                    context.y,
                    64,
                    64
                )
            }
        }

        val moonInnerContent = columnWithChildren(moonPhaseWidget, moonRenderer)

        var skyTypeFrameHeight = 30
        var skyTypeWidget: LayoutElement? = null

        if (Config.skyTypeChanger) {
            skyTypeFrameHeight += 25
            skyTypeWidget = labeledDropdown(
                "config.snowyspirits.skyType",
                skyTypeState,
                Config.SkyType.entries,
            ) { Config.skyType = it }
        }

        val skyInnerContent = columnWithChildren(skyTypeWidget)

        when (tab) {
            0 -> {
                ConfigSection(
                    "config.snowyspirits.weatherChanger",
                    "config.snowyspirits.weatherChanger.toggle",
                    enabledGetter = { Config.weatherChanger },
                    onToggle = { newVal ->
                        Config.weatherChanger = newVal
                        this.rebuildWidgets()
                    },
                    contentBuilder = { weatherInnerContent },
                    explicitHeight = weatherChangerFrameHeight
                ).addTo(mainScrollableList)

                addFrameToList(mainScrollableList, noPrecipitationBlockingToggle, 30)
            }
            1 -> {
                ConfigSection(
                    "config.snowyspirits.timeChanger",
                    "config.snowyspirits.timeChanger.toggle",
                    enabledGetter = { Config.timeChanger },
                    onToggle = { newVal ->
                        Config.timeChanger = newVal
                        this.rebuildWidgets()
                    },
                    contentBuilder = { timeInnerContent },
                    explicitHeight = timeChangerFrameHeight
                ).addTo(mainScrollableList)

                ConfigSection(
                    "config.snowyspirits.moonPhaseChanger",
                    "config.snowyspirits.moonPhaseChanger.toggle",
                    enabledGetter = { Config.moonPhaseChanger },
                    onToggle = { newVal ->
                        Config.moonPhaseChanger = newVal
                        this.rebuildWidgets()
                    },
                    contentBuilder = { moonInnerContent },
                    explicitHeight = moonChangerFrameHeight
                ).addTo(mainScrollableList)
            }
            2 -> {
                ConfigSection(
                    titleKey = "config.snowyspirits.skyTypeChanger",
                    toggleKey = "config.snowyspirits.skyTypeChanger.toggle",
                    enabledGetter = { Config.skyTypeChanger },
                    onToggle = { newVal ->
                        Config.skyTypeChanger = newVal
                        this.rebuildWidgets()
                    },
                    contentBuilder = { skyInnerContent },
                    explicitHeight = skyTypeFrameHeight
                ).addTo(mainScrollableList)
            }
            else -> {}
        }

        val content = Layouts.column()
            .withChild(
                Widgets.frame()
                    .withSize(width / 4, height / 3)
                    .withTexture(UIConstants.MODAL_INSET)
                    .withContents { it.addChild(mainScrollableList) }
                    .withContentFill()
                    .withContentMargin(10)
            )

        val tabLayout = Layouts.row()
            .withGap(0)
            .withChild(
                Widgets.radio(
                    tabRadioState,
                    { builder ->
                        builder.withOption("Weather")
                            .withOption("Time")
                            .withOption("Rendering")
                            .withSize((width / 4), 20)
                            .withoutEntrySprites()
                            .withRenderer { string, bool ->
                                WidgetRenderers.layered(
                                    WidgetRenderers.sprite(if (bool) UIConstants.PRIMARY_BUTTON else UIConstants.BUTTON),
                                    WidgetRenderers.text(Component.literal(string))
                                )
                            }
                            .withCallback { newVal ->
                                tab = when (newVal) {
                                    "Weather" -> 0
                                    "Time" -> 1
                                    "Rendering" -> 2
                                    else -> 0
                                }
                                this.repositionElements()
                            }
                    }
                ) {}
            )

        this.screen = Layouts.column()
            .withGap(0)
            .withChild(
                Widgets.frame()
                    .withSize(width / 4, mc.font.lineHeight * 2)
                    .withTexture(UIConstants.MODAL_HEADER)
                    .withContents {
                        it.addChild(
                            Widgets.labelled(
                                mc.font,
                                Component.literal("Snowy Config"),
                                Widgets.button()
                                    .withTexture(null)
                                    .withRenderer(WidgetRenderers.sprite(UIConstants.MODAL_CLOSE))
                                    .withCallback {
                                        this.onClose()
                                    }
                                    .withTooltip(Component.literal("Close"))
                                    .withSize(mc.font.lineHeight, mc.font.lineHeight)
                            ).withEqualSpacing(Orientation.HORIZONTAL)
                        )
                    }
                    .withContentFill()
                    .withContentMargin(mc.font.lineHeight / 2)
            )
            .withChild(
                Widgets.frame()
                    .withSize(width / 4, 20)
                    .withTexture(UIConstants.MODAL_HEADER)
                    .withContents {
                        it.addChild(tabLayout)
                    }
                    .withContentFill()
                    .withContentMargin(0)
            )
            .withChildren(content)
            .build { widget -> this.addRenderableWidget(widget) }

        FrameLayout.centerInRectangle(this.screen!!, this.rectangle)
    }

    fun getTimeFromString(preset: String): Long {
        return when (preset) {
            "Sunrise" -> 0L
            "Noon" -> 6000L
            "Sunset" -> 12000L
            "Midnight" -> 18000L
            else -> 0L
        }
    }

    override fun renderBackground(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        val screen = screen ?: return
        super.renderBackground(graphics, mouseX, mouseY, partialTick)

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            UIConstants.MODAL,
            screen.x - 2, screen.y - 2,
            screen.width + 4, screen.height + 4
        )
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun onClose() {
        SnowySpirits.saveConfig()
        super.onClose()
    }
}