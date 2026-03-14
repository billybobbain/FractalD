package com.billybobbain.fractald

/**
 * Common contract for all fractal engines: complex-plane view and iteration-count output.
 * Enables FractalCanvas to work with Mandelbrot, Julia, Burning Ship, Tricorn, etc.
 */
interface FractalEngine {
    var centerX: Double
    var centerY: Double
    var zoom: Double
    var maxIterations: Int

    /** Compute smooth iteration count per pixel; same dimensions as view. */
    fun calculate(): Array<DoubleArray>

    fun adjustZoom(factor: Double)
    fun pan(deltaX: Double, deltaY: Double)
    fun zoomTo(px: Int, py: Int, factor: Double)
    fun reset()
}
