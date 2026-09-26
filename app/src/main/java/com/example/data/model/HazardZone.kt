package com.example.data.model

data class HazardZone(
    val type: String, // "lumpur", "api", "air"
    val x: Float,
    val y: Float,
    val radius: Float,
    val polygon: List<Point2D>
)
