package me.siv.snowyspirits.screen.widget

import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.State
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
//? if > 1.21.10 {
import net.minecraft.util.Util
//? } else {
/*import net.minecraft.Util*/
//? }

class UnlabeledSliderWidget<T: Number>(
    initState: State<T>,
    val minValue: T,
    val maxValue: T,
) : BaseWidget() {
    private val SCROLLBAR = UIConstants.id("lists/scroll/thumb")
    private val SCROLLBAR_WIDTH = 6

    @Suppress("UNCHECKED_CAST")
    private val adapter: NumberAdapter<T> = when (minValue) {
        is Double -> object : NumberAdapter<T> {
            override fun toDouble(value: T) = value.toDouble()
            override fun fromDouble(value: Double) = value as T
        }
        is Float -> object : NumberAdapter<T> {
            override fun toDouble(value: T) = value.toDouble()
            override fun fromDouble(value: Double) = value.toFloat() as T
        }
        is Int -> object : NumberAdapter<T> {
            override fun toDouble(value: T) = value.toInt().toDouble()
            override fun fromDouble(value: Double) = value.toInt() as T
        }
        is Long -> object : NumberAdapter<T> {
            override fun toDouble(value: T) = value.toLong().toDouble()
            override fun fromDouble(value: Double) = value.toLong() as T
        }
        else -> error("Unsupported number type")
    }

    private val state: State<T> = Util.make(ListenableState.of(initState)) {
        it.registerListener { newVal -> onChange(newVal) }
    }

    private var sliderPosition = calculateSliderPos()
    private var segments = this.width - SCROLLBAR_WIDTH

    private var onChange: (T) -> Unit = {}

    private var isDragging = false

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        if (!this.visible) return

        graphics.blitSprite(
            RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
            UIConstants.MODAL_INSET,
            this.x,
            this.y + (this.height - 6) / 2,
            segments + SCROLLBAR_WIDTH,
            6,
        )

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            SCROLLBAR,
            this.x + sliderPosition,
            this.y,
            SCROLLBAR_WIDTH,
            this.height,
        )
    }

    private fun calculateSliderPos(): Int {
        val current = adapter.toDouble(state.get())
        val min = adapter.toDouble(minValue)
        val max = adapter.toDouble(maxValue)

        if (max - min == 0.0) return 0

        return (((current - min) / (max - min)) * segments).toInt()
    }

    override fun mouseClicked(
        mouseButtonEvent: MouseButtonEvent,
        bl: Boolean
    ): Boolean {
        if (mouseButtonEvent.button() == 0) {
            isDragging = true
            setSliderPosFromEvent(mouseButtonEvent)
            return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseDragged(
        mouseButtonEvent: MouseButtonEvent,
        d: Double,
        e: Double
    ): Boolean {
        if (isDragging) {
            setSliderPosFromEvent(mouseButtonEvent)
            return true
        }
        return super.mouseDragged(mouseButtonEvent, d, e)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        isDragging = false
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun withSize(
        width: Int,
        height: Int
    ): BaseWidget {
        this.setSize(width, height)
        this.segments = this.width - SCROLLBAR_WIDTH
        this.sliderPosition = calculateSliderPos()
        return this
    }

    fun withCallback(
        onChange: (T) -> Unit
    ): UnlabeledSliderWidget<T> {
        this.onChange = onChange
        return this
    }

    private fun setSliderPosFromEvent(event: MouseButtonEvent) {
        if (segments <= 0) return
        val relativeX = (event.x - this.x - SCROLLBAR_WIDTH / 2).toInt().coerceIn(0, segments)

        sliderPosition = relativeX

        val min = adapter.toDouble(minValue)
        val max = adapter.toDouble(maxValue)

        val value = min + (relativeX.toDouble() / segments) * (max - min)
        state.set(adapter.fromDouble(value))
    }
}