package com.billybobbain.fractald

import kotlin.math.sqrt

/**
 * Mandelbrot set calculation engine.
 * Uses optimized integer math where possible for performance (Fractint-style).
 */
class MandelbrotEngine(
    private val width: Int,
    private val height: Int
) {
    // Current view coordinates (in complex plane)
    var centerX = -0.5
    var centerY = 0.0
    var zoom = 0.6
    var maxIterations = 256

    // Calculate Mandelbrot set for current view
    fun calculate(): Array<IntArray> {
        val result = Array(height) { IntArray(width) }

        // Calculate bounds in complex plane
        val aspectRatio = width.toDouble() / height
        val rangeY = 2.0 / zoom
        val rangeX = rangeY * aspectRatio

        val minX = centerX - rangeX / 2
        val maxX = centerX + rangeX / 2
        val minY = centerY - rangeY / 2
        val maxY = centerY + rangeY / 2

        // Use rectangular subdivision for optimization
        calculateRectangle(result, 0, 0, width - 1, height - 1, minX, maxX, minY, maxY)

        return result
    }

    // Recursive rectangular subdivision with border tracing
    private fun calculateRectangle(
        result: Array<IntArray>,
        x1: Int, y1: Int, x2: Int, y2: Int,
        minX: Double, maxX: Double, minY: Double, maxY: Double
    ) {
        val width = x2 - x1 + 1
        val height = y2 - y1 + 1

        // Base case: too small to subdivide, calculate directly
        if (width <= 2 || height <= 2) {
            for (py in y1..y2) {
                for (px in x1..x2) {
                    if (result[py][px] == 0) {
                        val x0 = minX + (px.toDouble() / this.width) * (maxX - minX)
                        val y0 = minY + (py.toDouble() / this.height) * (maxY - minY)
                        result[py][px] = calculatePoint(x0, y0)
                    }
                }
            }
            return
        }

        // Calculate border pixels
        var borderValue: Int? = null
        var allSame = true

        // Top and bottom edges
        for (px in x1..x2) {
            for (py in listOf(y1, y2)) {
                if (result[py][px] == 0) {
                    val x0 = minX + (px.toDouble() / this.width) * (maxX - minX)
                    val y0 = minY + (py.toDouble() / this.height) * (maxY - minY)
                    result[py][px] = calculatePoint(x0, y0)
                }

                if (borderValue == null) {
                    borderValue = result[py][px]
                } else if (result[py][px] != borderValue) {
                    allSame = false
                }
            }
        }

        // Left and right edges (skip corners already done)
        for (py in (y1 + 1) until y2) {
            for (px in listOf(x1, x2)) {
                if (result[py][px] == 0) {
                    val x0 = minX + (px.toDouble() / this.width) * (maxX - minX)
                    val y0 = minY + (py.toDouble() / this.height) * (maxY - minY)
                    result[py][px] = calculatePoint(x0, y0)
                }

                if (result[py][px] != borderValue) {
                    allSame = false
                }
            }
        }

        // If all border pixels are the same, fill interior
        if (allSame && borderValue != null) {
            for (py in (y1 + 1) until y2) {
                for (px in (x1 + 1) until x2) {
                    result[py][px] = borderValue
                }
            }
            return
        }

        // Otherwise, subdivide into 4 quadrants
        val midX = (x1 + x2) / 2
        val midY = (y1 + y2) / 2

        calculateRectangle(result, x1, y1, midX, midY, minX, maxX, minY, maxY)
        calculateRectangle(result, midX, y1, x2, midY, minX, maxX, minY, maxY)
        calculateRectangle(result, x1, midY, midX, y2, minX, maxX, minY, maxY)
        calculateRectangle(result, midX, midY, x2, y2, minX, maxX, minY, maxY)
    }

    // Calculate iterations for a single point
    private fun calculatePoint(x0: Double, y0: Double): Int {
        // Check if point is in the main cardioid
        val q = (x0 - 0.25) * (x0 - 0.25) + y0 * y0
        if (q * (q + (x0 - 0.25)) <= 0.25 * y0 * y0) {
            return maxIterations
        }

        // Check if point is in the period-2 bulb
        if ((x0 + 1.0) * (x0 + 1.0) + y0 * y0 <= 0.0625) {
            return maxIterations
        }

        var x = 0.0
        var y = 0.0
        var iteration = 0

        while (x * x + y * y <= 4.0 && iteration < maxIterations) {
            val xTemp = x * x - y * y + x0
            y = 2.0 * x * y + y0
            x = xTemp
            iteration++
        }

        // Smooth coloring using escape velocity
        if (iteration < maxIterations) {
            val log_zn = Math.log(x * x + y * y) / 2.0
            val nu = Math.log(log_zn / Math.log(2.0)) / Math.log(2.0)
            iteration = (iteration + 1 - nu).toInt()
        }

        return iteration
    }

    // Zoom in/out
    fun adjustZoom(factor: Double) {
        zoom *= factor
    }

    // Pan the view
    fun pan(deltaX: Double, deltaY: Double) {
        val aspectRatio = width.toDouble() / height
        val rangeY = 2.0 / zoom
        val rangeX = rangeY * aspectRatio

        centerX += deltaX * rangeX
        centerY += deltaY * rangeY
    }

    // Zoom to a specific point
    fun zoomTo(px: Int, py: Int, factor: Double) {
        // Convert pixel to complex coordinates
        val aspectRatio = width.toDouble() / height
        val rangeY = 2.0 / zoom
        val rangeX = rangeY * aspectRatio

        val minX = centerX - rangeX / 2
        val maxX = centerX + rangeX / 2
        val minY = centerY - rangeY / 2
        val maxY = centerY + rangeY / 2

        val newCenterX = minX + (px.toDouble() / width) * (maxX - minX)
        val newCenterY = minY + (py.toDouble() / height) * (maxY - minY)

        centerX = newCenterX
        centerY = newCenterY
        zoom *= factor
    }

    // Reset to default view (zoomed out to see whole set)
    fun reset() {
        centerX = -0.5
        centerY = 0.0
        zoom = 0.6
    }
}
