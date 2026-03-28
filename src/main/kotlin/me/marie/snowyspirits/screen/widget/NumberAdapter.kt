package me.marie.snowyspirits.screen.widget

interface NumberAdapter<T : Number> {
    fun toDouble(value: T): Double
    fun fromDouble(value: Double): T
}