package com.billybobbain.fractald

/**
 * Tricorn (Mandelbar): z_{n+1} = conjugate(z_n)^2 + c = (Re(z) - i*Im(z))^2 + c.
 * So (x - iy)^2 = x^2 - y^2 - i*2xy, hence new_x = x^2 - y^2 + cx, new_y = -2*x*y + cy.
 */
class TricornEngine(
    private val width: Int,
    private val height: Int
) : FractalEngine {

    override var centerX = -0.5
    override var centerY = 0.0
    override var zoom = 0.6
    override var maxIterations = 256

    override fun calculate(): Array<DoubleArray> {
        val result = Array(height) { DoubleArray(width) { -1.0 } }

        val aspectRatio = width.toDouble() / height
        val rangeY = 2.0 / zoom
        val rangeX = rangeY * aspectRatio

        val minX = centerX - rangeX / 2
        val maxX = centerX + rangeX / 2
        val minY = centerY - rangeY / 2
        val maxY = centerY + rangeY / 2

        calculateRectangle(result, 0, 0, width - 1, height - 1, minX, maxX, minY, maxY)

        return result
    }

    private fun calculateRectangle(
        result: Array<DoubleArray>,
        x1: Int, y1: Int, x2: Int, y2: Int,
        minX: Double, maxX: Double, minY: Double, maxY: Double
    ) {
        val w = x2 - x1 + 1
        val h = y2 - y1 + 1

        if (w <= 2 || h <= 2) {
            for (py in y1..y2) {
                for (px in x1..x2) {
                    if (result[py][px] < 0) {
                        val x0 = minX + (px.toDouble() / width) * (maxX - minX)
                        val y0 = minY + (py.toDouble() / height) * (maxY - minY)
                        result[py][px] = calculatePoint(x0, y0)
                    }
                }
            }
            return
        }

        var borderValue: Double? = null
        var allSame = true

        for (px in x1..x2) {
            for (py in listOf(y1, y2)) {
                if (result[py][px] < 0) {
                    val x0 = minX + (px.toDouble() / width) * (maxX - minX)
                    val y0 = minY + (py.toDouble() / height) * (maxY - minY)
                    result[py][px] = calculatePoint(x0, y0)
                }
                if (borderValue == null) borderValue = result[py][px]
                else if (result[py][px] != borderValue) allSame = false
            }
        }

        for (py in (y1 + 1) until y2) {
            for (px in listOf(x1, x2)) {
                if (result[py][px] < 0) {
                    val x0 = minX + (px.toDouble() / width) * (maxX - minX)
                    val y0 = minY + (py.toDouble() / height) * (maxY - minY)
                    result[py][px] = calculatePoint(x0, y0)
                }
                if (result[py][px] != borderValue) allSame = false
            }
        }

        if (allSame && borderValue != null) {
            for (py in (y1 + 1) until y2) {
                for (px in (x1 + 1) until x2) {
                    result[py][px] = borderValue
                }
            }
            return
        }

        val midX = (x1 + x2) / 2
        val midY = (y1 + y2) / 2
        calculateRectangle(result, x1, y1, midX, midY, minX, maxX, minY, maxY)
        calculateRectangle(result, midX, y1, x2, midY, minX, maxX, minY, maxY)
        calculateRectangle(result, x1, midY, midX, y2, minX, maxX, minY, maxY)
        calculateRectangle(result, midX, midY, x2, y2, minX, maxX, minY, maxY)
    }

    private fun calculatePoint(x0: Double, y0: Double): Double {
        var x = 0.0
        var y = 0.0
        var iteration = 0

        while (x * x + y * y <= 4.0 && iteration < maxIterations) {
            // conjugate(z)^2 = (x - iy)^2 = x^2 - y^2 - i*2xy
            val xTemp = x * x - y * y + x0
            y = -2.0 * x * y + y0
            x = xTemp
            iteration++
        }

        if (x * x + y * y <= 4.0) {
            return maxIterations.toDouble()
        }

        val log_zn = Math.log(x * x + y * y) / 2.0
        val nu = Math.log(log_zn / Math.log(2.0)) / Math.log(2.0)
        return iteration + 1.0 - nu
    }

    override fun adjustZoom(factor: Double) {
        zoom *= factor
    }

    override fun pan(deltaX: Double, deltaY: Double) {
        val aspectRatio = width.toDouble() / height
        val rangeY = 2.0 / zoom
        val rangeX = rangeY * aspectRatio
        centerX += deltaX * rangeX
        centerY += deltaY * rangeY
    }

    override fun zoomTo(px: Int, py: Int, factor: Double) {
        val aspectRatio = width.toDouble() / height
        val rangeY = 2.0 / zoom
        val rangeX = rangeY * aspectRatio
        val minX = centerX - rangeX / 2
        val maxX = centerX + rangeX / 2
        val minY = centerY - rangeY / 2
        val maxY = centerY + rangeY / 2
        centerX = minX + (px.toDouble() / width) * (maxX - minX)
        centerY = minY + (py.toDouble() / height) * (maxY - minY)
        zoom *= factor
    }

    override fun reset() {
        centerX = -0.5
        centerY = 0.0
        zoom = 0.6
    }
}
