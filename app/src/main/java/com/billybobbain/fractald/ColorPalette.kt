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
        iterations: Array<IntArray>,
        maxIterations: Int,
        paletteType: PaletteType,
        timeOffset: Double
    ): Array<IntArray> {
        val height = iterations.size
        val width = iterations[0].size
        val result = Array(height) { IntArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val iter = iterations[y][x]
                result[y][x] = getColor(iter, maxIterations, paletteType, timeOffset)
            }
        }

        return result
    }

    private fun getColor(
        iteration: Int,
        maxIterations: Int,
        paletteType: PaletteType,
        timeOffset: Double
    ): Int {
        // Inside the set (black)
        if (iteration >= maxIterations) {
            return android.graphics.Color.BLACK
        }

        // Add time offset for palette rotation
        val normalizedIter = (iteration + timeOffset * 50) % 256 / 255.0

        val color = when (paletteType) {
            PaletteType.CLASSIC -> getClassicColor(normalizedIter)
            PaletteType.FIRE -> getFireColor(normalizedIter)
            PaletteType.OCEAN -> getOceanColor(normalizedIter)
            PaletteType.RAINBOW -> getRainbowColor(normalizedIter)
            PaletteType.PSYCHEDELIC -> getPsychedelicColor(normalizedIter)
            PaletteType.GRAYSCALE -> getGrayscaleColor(normalizedIter)
        }

        return android.graphics.Color.rgb(
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
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
