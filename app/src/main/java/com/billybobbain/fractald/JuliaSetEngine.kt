package com.billybobbain.fractald

/**
 * Julia set: z_{n+1} = z_n^2 + c with c fixed; iterate over starting points z_0 in the plane.
 */
class JuliaSetEngine(
    private val width: Int,
    private val height: Int
) : FractalEngine {

    override var centerX = 0.0
    override var centerY = 0.0
    override var zoom = 0.6
    override var maxIterations = 256

    /** Fixed complex constant c = cRe + i*cIm (e.g. from "pick c from Mandelbrot"). */
    var cRe = -0.7
    var cIm = 0.27015

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
                        val z0Re = minX + (px.toDouble() / width) * (maxX - minX)
                        val z0Im = minY + (py.toDouble() / height) * (maxY - minY)
                        result[py][px] = calculatePoint(z0Re, z0Im)
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
                    val z0Re = minX + (px.toDouble() / width) * (maxX - minX)
                    val z0Im = minY + (py.toDouble() / height) * (maxY - minY)
                    result[py][px] = calculatePoint(z0Re, z0Im)
                }
                if (borderValue == null) borderValue = result[py][px]
                else if (result[py][px] != borderValue) allSame = false
            }
        }

        for (py in (y1 + 1) until y2) {
            for (px in listOf(x1, x2)) {
                if (result[py][px] < 0) {
                    val z0Re = minX + (px.toDouble() / width) * (maxX - minX)
                    val z0Im = minY + (py.toDouble() / height) * (maxY - minY)
                    result[py][px] = calculatePoint(z0Re, z0Im)
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

    private fun calculatePoint(z0Re: Double, z0Im: Double): Double {
        var x = z0Re
        var y = z0Im
        var iteration = 0

        while (x * x + y * y <= 4.0 && iteration < maxIterations) {
            val xTemp = x * x - y * y + cRe
            y = 2.0 * x * y + cIm
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
        centerX = 0.0
        centerY = 0.0
        zoom = 0.6
    }
}
