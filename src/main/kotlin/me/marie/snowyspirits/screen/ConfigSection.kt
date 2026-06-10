package me.marie.snowyspirits.screen

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.Orientation
import me.marie.snowyspirits.SnowySpirits.mc
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component

class ConfigSection(
    private val titleKey: String,
    private val toggleKey: String,
    private val enabledGetter: () -> Boolean,
    private val onToggle: (Boolean) -> Unit,
    private val contentBuilder: () -> Layout?,
    private val explicitHeight: Int? = null,
    private val baseHeight: Int = 30,
    private val perContentHeight: Int = 25
) {
    fun build(toggleFactory: ( () -> Boolean, (Boolean) -> Unit ) -> LayoutElement): Pair<Layout, Int> {
        val column = Layouts.column().withGap(5)

        val header = toggleFactory(enabledGetter, onToggle)
        column.withChild(header)

        val content = contentBuilder()
        if (content != null) column.withChild(content)

        val height = explicitHeight ?: run {
            var newHeight = baseHeight
            if (content != null) newHeight += perContentHeight
            newHeight
        }

        return column to height
    }

    fun addTo(list: ListWidget, toggleFactory: ( () -> Boolean, (Boolean) -> Unit ) -> LayoutElement) {
        val (layout, height) = build(toggleFactory)
        list.add(
            Widgets.frame()
                .withSize(list.width, height)
                .withTexture(UIConstants.MODAL_INSET)
                .withContents { it.addChild(layout) }
                .withContentFill()
                .withContentMargin(5)
        )
    }

    fun addTo(list: ListWidget) {
        addTo(list) { getter, setter ->
            Widgets.labelled(
                mc.font,
                Component.translatable(titleKey),
                createColoredToggleButton(getter, Component.translatable(toggleKey)) { setter(it) }
            ).withEqualSpacing(Orientation.HORIZONTAL)
        }
    }
}

