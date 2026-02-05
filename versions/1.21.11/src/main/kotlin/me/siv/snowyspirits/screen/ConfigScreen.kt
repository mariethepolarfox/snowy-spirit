package me.siv.snowyspirits.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import earth.terrarium.olympus.client.components.compound.radio.RadioState
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.Overlay
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import me.siv.snowyspirits.SnowySpirits
import me.siv.snowyspirits.SnowySpirits.mc
import me.siv.snowyspirits.config.Config
import me.siv.snowyspirits.screen.widget.SliderWidget
import me.siv.snowyspirits.screen.widget.UnlabeledSliderWidget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
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

        val weatherChangerMainToggle = Widgets.labelled(
            mc.font,
            Component.translatable("config.snowyspirits.weatherChanger"),
            createColoredToggleButton(
                { Config.weatherChanger },
                "Weather Changer",
            ) { newVal ->
                Config.weatherChanger = newVal
                this.rebuildWidgets()
            }
        ).withEqualSpacing(Orientation.HORIZONTAL)

        var weatherChangerWeatherType: LayoutElement? = null
        var weatherChangerPrecipitationType: LayoutElement? = null
        var lightningChance: LayoutElement? = null

        if (Config.weatherChanger) {
            weatherChangerFrameHeight += 25
            weatherChangerWeatherType = Widgets.labelled(
                mc.font,
                Component.translatable("config.snowyspirits.weatherType"),
                Widgets.dropdown(
                    weatherDropdownState,
                    Weather.Type.entries,
                    { Component.literal(it.name.toTitleCase()) },
                    {
                        it.withSize(100, 20).withTexture(UIConstants.BUTTON)
                    }
                ) {
                    it.withSize(100, 20 * Weather.Type.entries.size + 4)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withCallback { newVal ->
                            Config.weatherType = newVal
                        }
                }
            ).withEqualSpacing(Orientation.HORIZONTAL)

            weatherChangerFrameHeight += 25
            weatherChangerPrecipitationType = Widgets.labelled(
                mc.font,
                Component.translatable("config.snowyspirits.precipitation"),
                Widgets.dropdown(
                    precipitationDropdownState,
                    Biome.Precipitation.entries,
                    { Component.literal(it.name.lowercase().replaceFirstChar { char -> char.uppercase() }) },
                    {
                        it.withSize(100, 20).withTexture(UIConstants.BUTTON)
                    }
                ) {
                    it.withSize(100, 20 * Biome.Precipitation.entries.size + 4)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withCallback { newVal ->
                            Config.precipitation = newVal
                        }
                }
            ).withEqualSpacing(Orientation.HORIZONTAL)

            if (Config.weatherType == Weather.Type.THUNDER) {
                weatherChangerFrameHeight += 25
                lightningChance = Widgets.labelled(
                    mc.font,
                    Component.translatable("config.snowyspirits.lightningChance"),
                    SliderWidget(
                        lightningChanceState,
                        0,
                        1_000_000
                    ).withSize(100, 20)
                ).withEqualSpacing(Orientation.HORIZONTAL)
            }
        }

        val noPrecipitationBlockingToggle = Widgets.labelled(
            mc.font,
            Component.translatable("config.snowyspirits.noPrecipitationBlocking"),
            createColoredToggleButton(
                { Config.noPrecipitationBlocking },
                "Weather Changer",
            ) { newVal ->
                Config.noPrecipitationBlocking = newVal
            }
        ).withEqualSpacing(Orientation.HORIZONTAL)

        val weatherChangerVerticalLayout = Layouts.column()
            .withGap(5)
            .withChild(weatherChangerMainToggle)
        if (weatherChangerWeatherType != null) weatherChangerVerticalLayout.withChild(weatherChangerWeatherType)
        if (weatherChangerPrecipitationType != null) weatherChangerVerticalLayout.withChild(weatherChangerPrecipitationType)
        if (lightningChance != null) weatherChangerVerticalLayout.withChild(lightningChance)

        var timeChangerFrameHeight = 30

        val timeChangerMainToggle = Widgets.labelled(
            mc.font,
            Component.translatable("config.snowyspirits.timeChanger"),
            createColoredToggleButton(
                { Config.timeChanger },
                "Time Changer",
            ) { newVal ->
                Config.timeChanger = newVal
                this.rebuildWidgets()
            }
        ).withEqualSpacing(Orientation.HORIZONTAL)

        var timeInput: LayoutElement? = null
        var timePreset: LayoutElement? = null

        if (Config.timeChanger) {
            timeChangerFrameHeight += 25
            timeInput = Widgets.labelled(
                mc.font,
                Component.translatable("config.snowyspirits.time"),
                SliderWidget(
                    timeState,
                    0,
                    24000
                ).withSize(100, 20)
            ).withEqualSpacing(Orientation.HORIZONTAL)

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

        val timeChangerVerticalLayout = Layouts.column()
            .withGap(5)
            .withChild(timeChangerMainToggle)
        if (timeInput != null) timeChangerVerticalLayout.withChild(timeInput)
        if (timePreset != null) timeChangerVerticalLayout.withChild(timePreset)

        var moonChangerFrameHeight = 30

        val moonChangerMainToggle = Widgets.labelled(
            mc.font,
            Component.translatable("config.snowyspirits.moonPhaseChanger"),
            createColoredToggleButton(
                { Config.moonPhaseChanger },
                "Moon Changer",
            ) { newVal ->
                Config.moonPhaseChanger = newVal
                this.rebuildWidgets()
            }
        ).withEqualSpacing(Orientation.HORIZONTAL)

        var moonPhaseWidget: LayoutElement? = null
        var moonRenderer: LayoutElement? = null

        if (Config.moonPhaseChanger) {
            moonChangerFrameHeight += 25
            moonPhaseWidget = Widgets.labelled(
                mc.font,
                Component.translatable("config.snowyspirits.moonPhase"),
                UnlabeledSliderWidget(
                    moonPhaseState,
                    0,
                    Config.MoonPhase.entries.size - 1
                ).withSize(100, 20)
            ).withEqualSpacing(Orientation.HORIZONTAL)

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

        val moonChangerVerticalLayout = Layouts.column()
            .withGap(5)
            .withChild(moonChangerMainToggle)
        if (moonPhaseWidget != null) moonChangerVerticalLayout.withChild(moonPhaseWidget)
        if (moonRenderer != null) moonChangerVerticalLayout.withChild(moonRenderer)


        var skyTypeFrameHeight = 30

        val skyTypeChangerMainToggle = Widgets.labelled(
            mc.font,
            Component.translatable("config.snowyspirits.skyTypeChanger"),
            createColoredToggleButton(
                { Config.skyTypeChanger },
                "Sky Type Changer",
            ) { newVal ->
                Config.skyTypeChanger = newVal
                this.rebuildWidgets()
            }
        ).withEqualSpacing(Orientation.HORIZONTAL)

        var skyTypeWidget: LayoutElement? = null

        if (Config.skyTypeChanger) {
            skyTypeFrameHeight += 25
            skyTypeWidget = Widgets.labelled(
                mc.font,
                Component.translatable("config.snowyspirits.skyType"),
                Widgets.dropdown(
                    skyTypeState,
                    Config.SkyType.entries,
                    { Component.literal(it.name.toTitleCase()) },
                    {
                        it.withSize(100, 20).withTexture(UIConstants.BUTTON)
                    }
                ) {
                    it.withSize(100, 20 * Config.SkyType.entries.size + 4)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withCallback { newVal ->
                            Config.skyType = newVal
                        }
                }
            ).withEqualSpacing(Orientation.HORIZONTAL)
        }

        val skyChangerVertivalLayout = Layouts.column()
            .withGap(5)
            .withChild(skyTypeChangerMainToggle)
        if (skyTypeWidget != null) skyChangerVertivalLayout.withChild(skyTypeWidget)

        when (tab) {
            0 -> {
                mainScrollableList.add(
                    Widgets.frame()
                        .withSize(mainScrollableList.width, weatherChangerFrameHeight)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withContents {
                            it.addChild(weatherChangerVerticalLayout)
                        }
                        .withContentFill()
                        .withContentMargin(5)
                )

                mainScrollableList.add(
                    Widgets.frame()
                        .withSize(mainScrollableList.width, 30)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withContents {
                            it.addChild(noPrecipitationBlockingToggle)
                        }
                        .withContentFill()
                        .withContentMargin(5)
                )
            }
            1 -> {
                mainScrollableList.add(
                    Widgets.frame()
                        .withSize(mainScrollableList.width, timeChangerFrameHeight)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withContents {
                            it.addChild(timeChangerVerticalLayout)
                        }
                        .withContentFill()
                        .withContentMargin(5)
                )

                mainScrollableList.add(
                    Widgets.frame()
                        .withSize(mainScrollableList.width, moonChangerFrameHeight)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withContents {
                            it.addChild(moonChangerVerticalLayout)
                        }
                        .withContentFill()
                        .withContentMargin(5)
                )
            }
            2 -> {
                mainScrollableList.add(
                    Widgets.frame()
                        .withSize(mainScrollableList.width, skyTypeFrameHeight)
                        .withTexture(UIConstants.MODAL_INSET)
                        .withContents {
                            it.addChild(skyChangerVertivalLayout)
                        }
                        .withContentFill()
                        .withContentMargin(5)
                )
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
                                Component.literal("Meow"),
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

    fun <T : AbstractWidget> texture(identifier: Identifier, width: Int = 16, height: Int = 16, yOffset: Int = -1) : WidgetRenderer<T> {
        return WidgetRenderer { graphics: GuiGraphics, context: WidgetRendererContext<T>, _: Float ->
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                identifier,
                context.x + (context.width - width) / 2,
                context.y + (context.height - height) / 2 + yOffset,
                0f, 0f,
                width, height,
                width, height,
            )
        }
    }

    fun createColoredToggleButton(
        configBool: () -> Boolean,
        label: String,
        onToggle: (Boolean) -> Unit
    ): BaseWidget {
        return Widgets.button()
            .withSize(20, 20)
            .withTexture(UIConstants.BUTTON)
            .withCallback {
                onToggle(!configBool())
            }
            .withRenderer { graphics, context, _ ->
                graphics.fill(
                    context.x + 2,
                    context.y + 2,
                    context.x + context.width - 2,
                    context.y + context.height - 4,
                    if (configBool()) 0x7755FF55 else 0x77FF5555
                )
            }
            .withTooltip(Component.literal(label))
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

    fun String.toTitleCase(): String {
        return this.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
}