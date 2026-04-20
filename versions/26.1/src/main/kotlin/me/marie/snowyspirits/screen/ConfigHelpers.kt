package me.marie.snowyspirits.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.Orientation
import me.marie.snowyspirits.SnowySpirits.mc
import me.marie.snowyspirits.screen.widget.SliderWidget
import me.marie.snowyspirits.screen.widget.UnlabeledSliderWidget
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

fun createColoredToggleButton(
    configBool: () -> Boolean,
    label: String,
    onToggle: (Boolean) -> Unit
): BaseWidget = createColoredToggleButton(configBool, Component.literal(label), onToggle)

fun createColoredToggleButton(
    configBool: () -> Boolean,
    label: Component,
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
        .withTooltip(label)
}

fun labeledToggle(
    translationKey: String,
    configBool: () -> Boolean,
    tooltipLabel: String,
    onToggle: (Boolean) -> Unit
): LayoutElement {
    return Widgets.labelled(
        mc.font,
        Component.translatable(translationKey),
        createColoredToggleButton(configBool, tooltipLabel, onToggle)
    ).withEqualSpacing(Orientation.HORIZONTAL)
}

fun <T : Enum<T>> labeledDropdown(
    labelKey: String,
    dropdownState: DropdownState<T>,
    entries: List<T>,
    nameTransform: (T) -> Component = { Component.literal(it.name.toTitleCase()) },
    valueCallback: (T) -> Unit,
): LayoutElement {
    return Widgets.labelled(
        mc.font,
        Component.translatable(labelKey),
        Widgets.dropdown(
            dropdownState,
            entries,
            { t -> nameTransform(t) },
            {
                it.withSize(100, 20).withTexture(UIConstants.BUTTON)
            }
        ) {
            it.withSize(100, 20 * entries.size + 4)
                .withTexture(UIConstants.MODAL_INSET)
                .withCallback { newVal -> valueCallback(newVal) }
        }
    ).withEqualSpacing(Orientation.HORIZONTAL)
}

fun labeledSlider(
    translationKey: String,
    state: ListenableState<Int>,
    min: Int,
    max: Int,
    width: Int = 100,
    height: Int = 20
): LayoutElement {
    return Widgets.labelled(
        mc.font,
        Component.translatable(translationKey),
        SliderWidget(state, min, max).withSize(width, height)
    ).withEqualSpacing(Orientation.HORIZONTAL)
}

fun labeledUnlabeledSlider(
    translationKey: String,
    state: ListenableState<Int>,
    min: Int,
    max: Int,
    width: Int = 100,
    height: Int = 20
): LayoutElement {
    return Widgets.labelled(
        mc.font,
        Component.translatable(translationKey),
        UnlabeledSliderWidget(state, min, max).withSize(width, height)
    ).withEqualSpacing(Orientation.HORIZONTAL)
}

fun addFrameToList(list: ListWidget, child: LayoutElement, height: Int) {
    list.add(
        Widgets.frame()
            .withSize(list.width, height)
            .withTexture(UIConstants.MODAL_INSET)
            .withContents { it.addChild(child) }
            .withContentFill()
            .withContentMargin(5)
    )
}

fun columnWithChildren(vararg children: LayoutElement?): LinearViewLayout {
    return Layouts.column().withGap(5).apply {
        children.filterNotNull().forEach { withChild(it) }
    }
}

fun <T : AbstractWidget> texture(identifier: Identifier, width: Int = 16, height: Int = 16, yOffset: Int = -1) : WidgetRenderer<T> {
    return WidgetRenderer { graphics: GuiGraphicsExtractor, context: WidgetRendererContext<T>, _: Float ->
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

fun String.toTitleCase(): String {
    return this.lowercase().replaceFirstChar { char -> char.uppercase() }
}