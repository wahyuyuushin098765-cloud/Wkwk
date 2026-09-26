package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.engine.GameEngine

@Composable
fun MinimapView(
    engine: GameEngine,
    screenWidth: Float,
    screenHeight: Float,
    modifier: Modifier = Modifier
) {
    val mw = 120.dp
    val mh = 75.dp

    Box(
        modifier = modifier
            .size(mw, mh)
            .background(Color(0xE60E1008))
            .border(1.5.dp, Color(0xFF3A3A1E))
    ) {
        Canvas(
            modifier = Modifier
                .size(mw, mh)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratioX = offset.x / size.width
                        val ratioY = offset.y / size.height
                        engine.camX = (ratioX * GameEngine.BW).coerceIn(0f, GameEngine.BW)
                        engine.camY = (ratioY * GameEngine.BH).coerceIn(0f, GameEngine.BH)
                    }
                }
        ) {
            try {
                val sx = size.width / GameEngine.BW
                val sy = size.height / GameEngine.BH

                val territories = engine.getTerritoriesSnapshot()
                val units = engine.getAllUnitsSnapshot()

                // Draw territories
                for (te in territories) {
                    val path = Path()
                    var first = true
                    for (pt in te.polygon) {
                        val px = pt.x * sx
                        val py = pt.y * sy
                        if (first) {
                            path.moveTo(px, py)
                            first = false
                        } else {
                            path.lineTo(px, py)
                        }
                    }
                    path.close()

                    val fill = if (te.team == "p") Color(0x664A9A4A) else Color(te.colorHex)
                    val stroke = if (te.team == "p") Color(0xFF4A9A4A) else Color(te.borderHex)
                    drawPath(path = path, color = fill)
                    drawPath(path = path, color = stroke, style = Stroke(width = 0.8f))
                }

                // Draw units
                for (p in units) {
                    if (!p.isAlive || p.isHidden) continue
                    val px = p.x * sx
                    val py = p.y * sy
                    val uColor = if (p.isGeneral) {
                        Color.White
                    } else if (p.team == "p") {
                        Color(0xFFFFD432)
                    } else {
                        Color(0xFFC44040)
                    }
                    val r = if (p.isGeneral) 2.2f else 1.2f
                    drawCircle(color = uColor, radius = r, center = Offset(px, py))
                }

                // Draw Camera Viewport
                val camW = screenWidth / engine.camZ
                val camH = screenHeight / engine.camZ
                val vx = (engine.camX - camW / 2f) * sx
                val vy = (engine.camY - camH / 2f) * sy
                val vw = camW * sx
                val vh = camH * sy

                drawRect(
                    color = Color(0xFFD4A832),
                    topLeft = Offset(vx, vy),
                    size = Size(vw, vh),
                    style = Stroke(width = 1f)
                )
            } catch (_: Throwable) {
                // Ignore transient draw error
            }
        }
    }
}
