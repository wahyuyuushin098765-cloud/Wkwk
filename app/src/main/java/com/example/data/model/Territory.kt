package com.example.data.model

data class Point2D(val x: Float, val y: Float)

data class BoundingBox(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)

data class Territory(
    val id: Int,
    val name: String,
    val ruler: String,
    val polygon: List<Point2D>,
    val cx: Float,
    val cy: Float,
    val radius: Float,
    var team: String = "n", // "n" = neutral/enemy, "p" = player
    val enemyCount: Int = 40,
    val colorHex: Long = 0x4D546326,
    val borderHex: Long = 0xFF3A441A,
    var isRevealed: Boolean = false,
    var everRevealed: Boolean = false,
    var converted: Boolean = false,
    var state: String = "idle", // "idle" or "lead"
    var formation: String = "globus",
    var runnerIdx: Int? = null,
    var cmdTimer: Float = 0f,
    var dominantAxis: String? = null,
    var formationKey: String? = null
) {
    val boundingBox: BoundingBox by lazy {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (pt in polygon) {
            if (pt.x < minX) minX = pt.x
            if (pt.x > maxX) maxX = pt.x
            if (pt.y < minY) minY = pt.y
            if (pt.y > maxY) maxY = pt.y
        }
        BoundingBox(minX, minY, maxX, maxY)
    }

    fun containsPoint(px: Float, py: Float): Boolean {
        if (px < boundingBox.minX || px > boundingBox.maxX || py < boundingBox.minY || py > boundingBox.maxY) {
            return false
        }
        var inside = false
        val n = polygon.size
        var j = n - 1
        for (i in 0 until n) {
            val xi = polygon[i].x
            val yi = polygon[i].y
            val xj = polygon[j].x
            val yj = polygon[j].y
            val intersect = ((yi > py) != (yj > py)) && (px < (xj - xi) * (py - yi) / (yj - yi + 0.00001f) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }
}
