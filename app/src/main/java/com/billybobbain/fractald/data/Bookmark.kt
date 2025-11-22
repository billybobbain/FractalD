package com.billybobbain.fractald.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val centerX: Double,
    val centerY: Double,
    val zoom: Double,
    val maxIterations: Int,
    val colorPalette: String,
    val thumbnail: ByteArray?, // PNG image data
    val timestamp: Long = System.currentTimeMillis()
) {
    // Override equals and hashCode to handle ByteArray properly
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bookmark

        if (id != other.id) return false
        if (name != other.name) return false
        if (centerX != other.centerX) return false
        if (centerY != other.centerY) return false
        if (zoom != other.zoom) return false
        if (maxIterations != other.maxIterations) return false
        if (colorPalette != other.colorPalette) return false
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + centerX.hashCode()
        result = 31 * result + centerY.hashCode()
        result = 31 * result + zoom.hashCode()
        result = 31 * result + maxIterations
        result = 31 * result + colorPalette.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
