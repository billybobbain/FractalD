package com.billybobbain.fractald.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ThumbnailGenerator {
    fun generateThumbnail(coloredData: Array<IntArray>, width: Int, height: Int): ByteArray {
        // Create a smaller thumbnail (max 200x200)
        val thumbnailSize = 200
        val scale = maxOf(width, height).toFloat() / thumbnailSize
        val thumbnailWidth = (width / scale).toInt()
        val thumbnailHeight = (height / scale).toInt()

        // Create bitmap at thumbnail size
        val bitmap = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888)

        // Sample pixels from coloredData
        for (y in 0 until thumbnailHeight) {
            for (x in 0 until thumbnailWidth) {
                val sourceX = (x * scale).toInt().coerceIn(0, width - 1)
                val sourceY = (y * scale).toInt().coerceIn(0, height - 1)
                val color = coloredData[sourceY][sourceX]
                bitmap.setPixel(x, y, color)
            }
        }

        // Compress to PNG
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        bitmap.recycle()

        return outputStream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}
