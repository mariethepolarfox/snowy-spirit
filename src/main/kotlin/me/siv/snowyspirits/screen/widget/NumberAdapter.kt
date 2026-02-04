package me.siv.snowyspirits.screen.widget

interface NumberAdapter<T : Number> {
    fun toDouble(value: T): Double
    fun fromDouble(value: Double): T
}