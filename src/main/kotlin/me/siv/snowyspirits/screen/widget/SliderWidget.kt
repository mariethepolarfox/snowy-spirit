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
import java.util.function.Consumer

class SliderWidget<T: Number>(
    initState: State<T>,
    val minValue: T,
    val maxValue: T,
) : BaseWidget() {
    private val SCROLLBAR = UIConstants.id("lists/scroll/thumb")
    private val SCROLLBAR_WIDTH = 6

    private val state: State<T> = Util.make(ListenableState.of(initState)) {
        it.registerListener { newVal -> onChange.accept(newVal) }
    }

    private var sliderPosition = calculateSliderPos()
    private var segments = this.width - SCROLLBAR_WIDTH

    private var onChange = Consumer { s: T? -> }

    private var isDragging = false
    private var listening = false

    private var cachedInput = ""

    private var stateStringWidth = 0
    private var stateStringPosX = 0
    private var stateStringPosY = 0

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
            this.width,
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

        val stateString = (if (!listening) state.get().toString() else cachedInput) + if (isDragging) " (Dragging)" else ""
        stateStringWidth = mc.font.width(stateString)
        stateStringPosX = this.x + this.width / 2 - stateStringWidth / 2
        stateStringPosY = this.y + this.height / 2 - mc.font.lineHeight / 2

        if (isOverStateString(mouseX, mouseY) && !listening) {
            graphics.blitSprite(
                RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
                UIConstants.id("textbox/disabled"),
                stateStringPosX - 2,
                stateStringPosY - 3,
                stateStringWidth + 4,
                mc.font.lineHeight + 4,
            )
        } else if (listening) {
            graphics.blitSprite(
                RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
                UIConstants.id("textbox/normal"),
                stateStringPosX - 2,
                stateStringPosY - 3,
                stateStringWidth + 4,
                mc.font.lineHeight + 4,
            )
        }

        graphics.drawString(
            mc.font,
            stateString,
            this.x + this.width / 2 - stateStringWidth / 2,
            this.y + this.height / 2 - mc.font.lineHeight / 2,
            0xFFFFFFFF.toInt(),
        )
    }

    fun calculateSliderPos(): Int {
        return when (val current = state.get()) {
            is Double -> (((current - minValue.toDouble()) / (maxValue.toDouble() - minValue.toDouble())) * segments).toInt()
            is Float -> (((current - minValue.toFloat()) / (maxValue.toFloat() - minValue.toFloat())) * segments).toInt()
            is Long -> (((current - minValue.toLong()).toDouble() / (maxValue.toLong() - minValue.toLong()).toDouble()) * segments).toInt()
            is Int -> (((current - minValue.toInt()).toDouble() / (maxValue.toInt() - minValue.toInt()).toDouble()) * segments).toInt()
            else -> 0
        }
    }

    override fun mouseClicked(
        mouseButtonEvent: MouseButtonEvent,
        bl: Boolean
    ): Boolean {
        if (mouseButtonEvent.button() == 0) {
            if (isOverStateString(mouseButtonEvent)) {
                listening = true
                cachedInput = state.get().toString()
            } else {
                if (listening) {
                    stopListening()
                } else {
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

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (!listening) return super.keyPressed(keyEvent)

        when (keyEvent.key) {
            InputConstants.KEY_ESCAPE -> {
                stopListening()
                return true
            }

            InputConstants.KEY_NUMPADENTER, InputConstants.KEY_RETURN -> {
                stopListening()
                return true
            }

            InputConstants.KEY_BACKSPACE -> {
                if (cachedInput.isNotEmpty()) {
                    cachedInput = cachedInput.substring(0, cachedInput.length - 1)
                }
                return true
            }

            else -> {
                val char = Char(keyEvent.key)
                if (Character.isDigit(char) || char == '.' || char == '-') {
                    cachedInput += char
                }
                return true
            }
        }
    }

    fun isOverStateString(event: MouseButtonEvent): Boolean {
        return isOverStateString(event.x.toInt(), event.y.toInt())
    }

    fun isOverStateString(x: Int, y: Int): Boolean {
        return x > stateStringPosX && x < stateStringPosX + stateStringWidth &&
               y > stateStringPosY && y < stateStringPosY + mc.font.lineHeight
    }

    fun setSliderPosFromEvent(event: MouseButtonEvent) {
        val relativeX = (event.x.toInt() - this.x - (SCROLLBAR_WIDTH / 2)).coerceIn(0, segments)
        sliderPosition = relativeX

        @Suppress("UNCHECKED_CAST")
        val newValue = when (state.get()) {
            is Double -> {
                (minValue.toDouble() + (relativeX.toDouble() / segments) * (maxValue.toDouble() - minValue.toDouble())) as? T ?: minValue
            }
            is Float -> {
                (minValue.toFloat() + (relativeX.toFloat() / segments) * (maxValue.toFloat() - minValue.toFloat())) as? T ?: minValue
            }
            is Long -> {
                (minValue.toLong() + ((relativeX.toDouble() / segments) * (maxValue.toLong() - minValue.toLong())).toLong())  as? T ?: minValue
            }
            is Int -> {
                (minValue.toInt() + ((relativeX.toDouble() / segments) * (maxValue.toInt() - minValue.toInt())).toInt()) as? T ?: minValue
            }
            else -> minValue
        }

        state.set(newValue)
    }

    fun stopListening() {
        listening = false

        @Suppress("UNCHECKED_CAST")
        val parsedValue: T? = when (state.get()) {
            is Double -> cachedInput.toDoubleOrNull()?.coerceIn(minValue.toDouble(), maxValue.toDouble()) as? T
            is Float -> cachedInput.toFloatOrNull()?.coerceIn(minValue.toFloat(), maxValue.toFloat()) as? T
            is Long -> cachedInput.toLongOrNull()?.coerceIn(minValue.toLong(), maxValue.toLong()) as? T
            is Int -> cachedInput.toIntOrNull()?.coerceIn(minValue.toInt(), maxValue.toInt()) as? T
            else -> null
        }

        state.set(parsedValue ?: state.get())
        sliderPosition = calculateSliderPos()
    }
}