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
import me.marie.snowyspirits.utils.EndFlashUtil
//~ if <= 1.21.11 'GuiGraphicsExtractor' -> 'GuiGraphics as GuiGraphicsExtractor'
import net.minecraft.client.gui.GuiGraphicsExtractor
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

        val endFlashIntervalState = ListenableState<Int>.of(Config.endFlashInterval)
        endFlashIntervalState.registerListener { newValue ->
            Config.endFlashInterval = newValue
            EndFlashUtil.resetState()
        }

        val endFlashMinDurationState = ListenableState<Int>.of(Config.minEndFlashDuration)
        endFlashMinDurationState.registerListener { newValue ->
            Config.minEndFlashDuration = newValue
            EndFlashUtil.resetState()
        }

        val endFlashMaxDurationState = ListenableState<Int>.of(Config.maxEndFlashDuration)
        endFlashMaxDurationState.registerListener { newValue ->
            Config.maxEndFlashDuration = newValue
            EndFlashUtil.resetState()
        }

        val mainScrollableList = ListWidget(width / 4 - 10, height / 3 - mc.font.lineHeight * 2 - 20)

        val (weatherInnerContent, weatherChangerFrameHeight) = createWeatherChangerWidget(weatherDropdownState, precipitationDropdownState, lightningChanceState)

        val noPrecipitationBlockingToggle = labeledToggle(
            "config.snowyspirits.noPrecipitationBlocking",
            { Config.noPrecipitationBlocking },
            "Precipitation Blocking"
        ) { newVal ->
            Config.noPrecipitationBlocking = newVal
        }

        val (timeInnerContent, timeChangerFrameHeight) = createTimeChangerWidget(timeState, mainScrollableList)
        val (moonInnerContent, moonChangerFrameHeight) = createMoonChangerWidget(moonPhaseState)
        val (skyInnerContent, skyTypeFrameHeight) = createSkyTypeChangerWidget(skyTypeState)
        val (endFlashInnerContent, endFlashFrameHeight) = createEndFlashChangerWidget(endFlashIntervalState, endFlashMinDurationState, endFlashMaxDurationState)

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
                    explicitHeight = weatherChangerFrameHeight,
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
                    explicitHeight = timeChangerFrameHeight,
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
                    explicitHeight = moonChangerFrameHeight,
                ).addTo(mainScrollableList)
            }
            2 -> {
                ConfigSection(
                    titleKey = "config.snowyspirits.skyTypeChanger",
                    toggleKey = "config.snowyspirits.skyTypeChanger.toggle",
                    enabledGetter = { Config.skyTypeChanger },
                    onToggle = { newVal ->
                        Config.skyTypeChanger = newVal
                        EndFlashUtil.resetState()
                        this.rebuildWidgets()
                    },
                    contentBuilder = { skyInnerContent },
                    explicitHeight = skyTypeFrameHeight,
                ).addTo(mainScrollableList)

                ConfigSection(
                    "config.snowyspirits.customEndFlashes",
                    "config.snowyspirits.customEndFlashes.toggle",
                    enabledGetter = { Config.customEndFlashes },
                    onToggle = { newVal ->
                        Config.customEndFlashes = newVal
                        this.rebuildWidgets()
                    },
                    contentBuilder = { endFlashInnerContent },
                    explicitHeight = endFlashFrameHeight,
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

    private fun createWeatherChangerWidget(
        weatherDropdownState: DropdownState<Weather.Type>,
        precipitationDropdownState: DropdownState<Biome.Precipitation>,
        lightningChanceState: ListenableState<Int>,
    ): Pair<Layout?, Int> {
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

        return columnWithChildren(
            weatherChangerWeatherType,
            weatherChangerPrecipitationType,
            lightningChance,
        ) to weatherChangerFrameHeight
    }

    private fun createTimeChangerWidget(
        timeState: ListenableState<Int>,
        mainScrollableList: ListWidget,
    ): Pair<Layout?, Int> {
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

        return columnWithChildren(timeInput, timePreset) to timeChangerFrameHeight
    }

    private fun createMoonChangerWidget(
        moonPhaseState: ListenableState<Int>,
    ): Pair<Layout?, Int> {
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

        return columnWithChildren(moonPhaseWidget, moonRenderer) to moonChangerFrameHeight
    }

    private fun createSkyTypeChangerWidget(
        skyTypeState: DropdownState<Config.SkyType>,
    ): Pair<Layout?, Int> {
        var skyTypeFrameHeight = 30
        var skyTypeWidget: LayoutElement? = null

        if (Config.skyTypeChanger) {
            skyTypeFrameHeight += 25
            skyTypeWidget = labeledDropdown(
                "config.snowyspirits.skyType",
                skyTypeState,
                Config.SkyType.entries,
            ) {
                Config.skyType = it
                EndFlashUtil.resetState()
            }
        }

        return columnWithChildren(skyTypeWidget) to skyTypeFrameHeight
    }

    private fun createEndFlashChangerWidget(
        intervalState: ListenableState<Int>,
        minDurationState: ListenableState<Int>,
        maxDurationState: ListenableState<Int>,
    ): Pair<Layout?, Int> {
        var endFlashFrameHeight = 30

        var interval: LayoutElement? = null
        var minDuration: LayoutElement? = null
        var maxDuration: LayoutElement? = null

        if (Config.customEndFlashes) {
            endFlashFrameHeight += 25
            interval = labeledSlider(
                "config.snowyspirits.customEndFlashes.interval",
                intervalState,
                1,
                600,
                ignoreMax = true,
            )

            endFlashFrameHeight += 25
            minDuration = labeledSlider(
                "config.snowyspirits.customEndFlashes.minDuration",
                minDurationState,
                1,
                100,
                ignoreMax = true,
            )

            endFlashFrameHeight += 25
            maxDuration = labeledSlider(
                "config.snowyspirits.customEndFlashes.maxDuration",
                maxDurationState,
                1,
                360,
                ignoreMax = true,
            )
        }

        return columnWithChildren(interval, minDuration, maxDuration) to endFlashFrameHeight
    }

    private fun getTimeFromString(preset: String): Long {
        return when (preset) {
            "Sunrise" -> 0L
            "Noon" -> 6000L
            "Sunset" -> 12000L
            "Midnight" -> 18000L
            else -> 0L
        }
    }

    //~ if <= 1.21.11 'extractBackground' -> 'renderBackground'
    override fun extractBackground(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        val screen = screen ?: return
        //~ if <= 1.21.11 'extractBackground' -> 'renderBackground'
        super.extractBackground(graphics, mouseX, mouseY, partialTick)

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