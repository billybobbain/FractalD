package com.billybobbain.fractald

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/**
 * Color palette system with rotation for smooth rainbow cycling.
 * Classic Fractint-style palette rotation.
 */
object ColorPalette {

    // Apply palette to iteration values with time offset for rotation
    fun applyPalette(
        iterations: Array<DoubleArray>,
        maxIterations: Int,
        paletteType: PaletteType,
        timeOffset: Double
    ): Array<IntArray> {
        val height = iterations.size
        val width = iterations[0].size
        val result = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                result[y][x] = getColor(iterations[y][x], maxIterations, paletteType, timeOffset)
            }
        }

        return result
    }

    fun applyBlendedPalette(
        iterations: Array<DoubleArray>,
        maxIterations: Int,
        fromPalette: PaletteType,
        toPalette: PaletteType,
        blend: Double,
        timeOffset: Double
    ): Array<IntArray> {
        val height = iterations.size
        val width = iterations[0].size
        val result = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                result[y][x] = getBlendedColor(iterations[y][x], maxIterations, fromPalette, toPalette, blend, timeOffset)
            }
        }
        return result
    }

    private fun getBlendedColor(
        iteration: Double,
        maxIterations: Int,
        fromPalette: PaletteType,
        toPalette: PaletteType,
        blend: Double,
        timeOffset: Double
    ): Int {
        if (iteration >= maxIterations) return android.graphics.Color.BLACK
        val normalizedIter = (iteration + timeOffset * 50) % 256.0 / 255.0
        val colorA = getPaletteColor(normalizedIter, fromPalette)
        val colorB = getPaletteColor(normalizedIter, toPalette)
        val r = colorA.red + (colorB.red - colorA.red) * blend.toFloat()
        val g = colorA.green + (colorB.green - colorA.green) * blend.toFloat()
        val b = colorA.blue + (colorB.blue - colorA.blue) * blend.toFloat()
        return android.graphics.Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    private fun getColor(
        iteration: Double,
        maxIterations: Int,
        paletteType: PaletteType,
        timeOffset: Double
    ): Int {
        // Inside the set (black)
        if (iteration >= maxIterations) {
            return android.graphics.Color.BLACK
        }

        // Fractional iteration feeds directly into the continuous palette functions,
        // smoothly interpolating colors across what were previously hard iteration bands
        val normalizedIter = (iteration + timeOffset * 50) % 256.0 / 255.0
        val color = getPaletteColor(normalizedIter, paletteType)

        return android.graphics.Color.rgb(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }

    private fun getPaletteColor(t: Double, paletteType: PaletteType): Color {
        return when (paletteType) {
            PaletteType.CLASSIC -> getClassicColor(t)
            PaletteType.FIRE -> getFireColor(t)
            PaletteType.OCEAN -> getOceanColor(t)
            PaletteType.RAINBOW -> getRainbowColor(t)
            PaletteType.PSYCHEDELIC -> getPsychedelicColor(t)
            PaletteType.GRAYSCALE -> getGrayscaleColor(t)
        }
    }

    private fun getClassicColor(t: Double): Color {
        val r = (sin(t * PI * 2.0) + 1.0) / 2.0
        val g = (sin(t * PI * 2.0 + PI * 2.0 / 3.0) + 1.0) / 2.0
        val b = (sin(t * PI * 2.0 + PI * 4.0 / 3.0) + 1.0) / 2.0
        return Color(
            r.toFloat().coerceIn(0f, 1f),
            g.toFloat().coerceIn(0f, 1f),
            b.toFloat().coerceIn(0f, 1f)
        )
    }

    private fun getFireColor(t: Double): Color {
        val r = t
        val g = (t - 0.3).coerceAtLeast(0.0) * 1.5
        val b = (t - 0.7).coerceAtLeast(0.0) * 3.0
        return Color(
            r.toFloat().coerceIn(0f, 1f),
            g.toFloat().coerceIn(0f, 1f),
            b.toFloat().coerceIn(0f, 1f)
        )
    }

    private fun getOceanColor(t: Double): Color {
        val b = 0.3 + t * 0.7
        val g = t * 0.8
        val r = t * 0.3
        return Color(
            r.toFloat().coerceIn(0f, 1f),
            g.toFloat().coerceIn(0f, 1f),
            b.toFloat().coerceIn(0f, 1f)
        )
    }

    private fun getRainbowColor(t: Double): Color {
        val hue = ((t * 360) % 360).toFloat()
        return Color.hsv(hue, 1f, 1f)
    }

    private fun getPsychedelicColor(t: Double): Color {
        val r = (sin(t * PI * 6.0) + 1.0) / 2.0
        val g = (sin(t * PI * 8.0 + 1.0) + 1.0) / 2.0
        val b = (sin(t * PI * 10.0 + 2.0) + 1.0) / 2.0
        return Color(
            r.toFloat().coerceIn(0f, 1f),
            g.toFloat().coerceIn(0f, 1f),
            b.toFloat().coerceIn(0f, 1f)
        )
    }

    private fun getGrayscaleColor(t: Double): Color {
        val v = t.toFloat().coerceIn(0f, 1f)
        return Color(v, v, v)
    }

    enum class PaletteType {
        CLASSIC, FIRE, OCEAN, RAINBOW, PSYCHEDELIC, GRAYSCALE
    }
}
