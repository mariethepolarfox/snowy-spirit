package me.siv.snowyspirits.screen.widget

import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.ListenableState
import earth.terrarium.olympus.client.utils.State
import me.siv.snowyspirits.SnowySpirits.mc
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.util.Util
import kotlin.math.pow

class SliderWidget<T: Number>(
    initState: State<T>,
    val minValue: T,
    val maxValue: T,
) : BaseWidget() {
    private val SCROLLBAR = UIConstants.id("lists/scroll/thumb")
    private val SCROLLBAR_WIDTH = 6
    private val MAX_STRING_WIDTH = 40
    private val SIDE_PADDING = 4

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
        it.registerListener { newVal ->
            onChange(newVal)
            cacheDirty = true
        }
    }

    private var sliderPosition = calculateSliderPos()
    private var segments = this.width - SCROLLBAR_WIDTH - MAX_STRING_WIDTH - SIDE_PADDING * 2

    private var onChange: (T) -> Unit = {}

    private var isDragging = false
    private var listening = false

    private var cachedInput = ""

    private var cachedStateString = state.get().formatted()
    private var stateStringWidth = 0
    private var stateStringPosX = 0
    private var stateStringPosY = 0

    private var cacheDirty = true

    override fun renderWidget(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        if (!this.visible) return

        if (cacheDirty) updateStateString()

        graphics.blitSprite(
            RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
            UIConstants.MODAL_INSET,
            this.x + SIDE_PADDING * 2 + MAX_STRING_WIDTH,
            this.y + (this.height - 6) / 2,
            segments + SCROLLBAR_WIDTH,
            6,
        )

        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            SCROLLBAR,
            this.x + sliderPosition + SIDE_PADDING * 2 + MAX_STRING_WIDTH,
            this.y,
            SCROLLBAR_WIDTH,
            this.height,
        )

        if (!listening) {
            if (isOverStateString(mouseX, mouseY)) {
                graphics.blitSprite(
                    RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
                    UIConstants.id("textbox/disabled"),
                    stateStringPosX - SIDE_PADDING,
                    stateStringPosY - 3,
                    MAX_STRING_WIDTH + SIDE_PADDING + 2,
                    mc.font.lineHeight + 4,
                )
            } else {
                graphics.blitSprite(
                    RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
                    UIConstants.id("textbox/hovered"),
                    stateStringPosX - SIDE_PADDING,
                    stateStringPosY - 3,
                    MAX_STRING_WIDTH + SIDE_PADDING + 2,
                    mc.font.lineHeight + 4,
                )
            }
        } else {
            graphics.blitSprite(
                RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
                UIConstants.id("textbox/normal"),
                stateStringPosX - SIDE_PADDING,
                stateStringPosY - 3,
                (stateStringWidth).coerceAtLeast(MAX_STRING_WIDTH) + SIDE_PADDING + 2,
                mc.font.lineHeight + 4,
            )

            if (System.currentTimeMillis() % 1000 < 500)
                graphics.fill(
                    RenderPipelines.GUI,
                    stateStringPosX + stateStringWidth + 1,
                    stateStringPosY,
                    stateStringPosX + stateStringWidth + 2,
                    stateStringPosY + mc.font.lineHeight - 1,
                    0xFFFFFFFF.toInt()
                )
        }

        graphics.drawString(
            mc.font,
            cachedStateString,
            stateStringPosX,
            stateStringPosY,
            0xFFFFFFFF.toInt(),
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
            if (isOverStateString(mouseButtonEvent)) {
                listening = true
                cachedInput = state.get().toString()
                cacheDirty = true
            } else {
                if (listening) {
                    stopListening()
                    cacheDirty = true
                } else if (mouseButtonEvent.x >= (this.x + SIDE_PADDING * 2 + MAX_STRING_WIDTH)) {
                    isDragging = true
                    setSliderPosFromEvent(mouseButtonEvent)
                }
            }
            return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseDragged(
        mouseButtonEvent: MouseButtonEvent,
        d: Double,
        e: Double
    ): Boolean {
        if (isDragging && mouseButtonEvent.x >= (this.x + SIDE_PADDING * 2 + MAX_STRING_WIDTH)) {
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
        this.segments = this.width - SCROLLBAR_WIDTH - MAX_STRING_WIDTH - SIDE_PADDING * 2
        this.sliderPosition = calculateSliderPos()
        cacheDirty
        return this
    }

    override fun setX(i: Int) {
        cacheDirty = true
        super.setX(i)
    }

    override fun setY(i: Int) {
        cacheDirty = true
        super.setY(i)
    }

    fun withCallback(
        onChange: (T) -> Unit
    ): SliderWidget<T> {
        this.onChange = onChange
        return this
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (!listening) return super.keyPressed(keyEvent)

        when (keyEvent.key) {
            InputConstants.KEY_ESCAPE -> stopListening()

            InputConstants.KEY_NUMPADENTER, InputConstants.KEY_RETURN -> stopListening()

            InputConstants.KEY_BACKSPACE -> {
                if (cachedInput.isNotEmpty()) {
                    cachedInput = cachedInput.substring(0, cachedInput.length - 1)
                }
            }

            else -> {
                val char = Char(keyEvent.key)
                if (Character.isDigit(char) || char == '.' || char == '-') {
                    cachedInput += char
                }
            }
        }
        cacheDirty = true
        return true
    }

    private fun isOverStateString(event: MouseButtonEvent): Boolean =
        isOverStateString(event.x.toInt(), event.y.toInt())

    private fun isOverStateString(x: Int, y: Int): Boolean {
        return x >= stateStringPosX - SIDE_PADDING && x <= stateStringPosX + (stateStringWidth).coerceAtLeast(MAX_STRING_WIDTH) + 2 &&
               y >= stateStringPosY - 3 && y <= stateStringPosY + mc.font.lineHeight + 3
    }

    private fun setSliderPosFromEvent(event: MouseButtonEvent) {
        if (segments <= 0) return
        val relativeX = (event.x - this.x - SIDE_PADDING * 2 - MAX_STRING_WIDTH - SCROLLBAR_WIDTH / 2).toInt().coerceIn(0, segments)

        sliderPosition = relativeX

        val min = adapter.toDouble(minValue)
        val max = adapter.toDouble(maxValue)

        val value = min + (relativeX.toDouble() / segments) * (max - min)
        state.set(adapter.fromDouble(value))
    }

    private fun stopListening() {
        listening = false

        val parsed = cachedInput
            .toDoubleOrNull()
            ?.coerceIn(adapter.toDouble(minValue), adapter.toDouble(maxValue))
            ?.let(adapter::fromDouble)

        state.set(parsed ?: state.get())
        sliderPosition = calculateSliderPos()
    }

    private fun updateStateString() {
        cachedStateString = if (!listening) state.get().formatted() else cachedInput
        stateStringWidth = mc.font.width(cachedStateString)
        stateStringPosX = this.x + SIDE_PADDING
        stateStringPosY = this.y + this.height / 2 - mc.font.lineHeight / 2
    }

    private fun T.formatted(): String {
        return when (val num = adapter.toDouble(this)) {
            in 1_000_000_000.0..Double.MAX_VALUE -> "${(num / 1_000_000_000.0).roundToMaxDec(2)}B"
            in 1_000_000.0..999_999_999.0 -> "${(num / 1_000_000.0).roundToMaxDec(2)}M"
            in 1_000.0..999_999.0 -> "${(num / 1_000.0).roundToMaxDec(2)}K"
            else -> adapter.fromDouble(num.roundToMaxDec(2)).toString()
        }
    }

    fun Double.rounded(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(this * factor) / factor
    }

    fun Double.roundToMaxDec(maxDecimals: Int): Double {
        // format number to: 100.233 -> 100.2, 10.233 -> 10.23, 1.233 -> 1.233
        val str = this.toString()
        val indexOfDot = str.indexOf('.')
        if (indexOfDot == -1) return this
        val decimals = str.length - indexOfDot - 1
        val decToUse = if (decimals < maxDecimals) decimals else maxDecimals
        return this.rounded(decToUse)
    }
}