package com.billybobbain.fractald.data

data class ViewState(
    val centerX: Double,
    val centerY: Double,
    val zoom: Double,
    val maxIterations: Int,
    val colorPalette: String
)
