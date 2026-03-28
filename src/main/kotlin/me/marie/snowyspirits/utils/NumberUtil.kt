package me.marie.snowyspirits.utils

import kotlin.math.pow
import kotlin.math.round

fun Double.rounded(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return round(this * factor) / factor
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