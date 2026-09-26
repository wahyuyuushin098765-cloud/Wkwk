package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Point2D
import com.example.data.model.Territory
import com.example.data.model.UnitPiece
import com.example.engine.GameEngine
import kotlin.math.*

@Composable
fun GameCanvas(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    val generalAvatarBitmap = remember(context) {
        try {
            android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.img_general_avatar_1790441484671)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    val mapTextureBitmap = remember(context) {
        try {
            android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.img_map_texture_1790441469649)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { screenOffset ->
                        val worldPt = screenToWorld(
                            screenOffset.x,
                            screenOffset.y,
                            engine.camX,
                            engine.camY,
                            engine.camZ,
                            canvasSize.width,
                            canvasSize.height,
                            engine.shakeX,
                            engine.shakeY
                        )
                        engine.orderMove(worldPt.x, worldPt.y)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        dragStart = startOffset
                        dragCurrent = startOffset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val start = dragStart
                        if (start != null) {
                            dragCurrent = (dragCurrent ?: start) + dragAmount
                            val cur = dragCurrent!!
                            val diffX = abs(cur.x - start.x)
                            val diffY = abs(cur.y - start.y)

                            // If drag distance is very small, it's a potential tap/selection; if big and selection is empty, we pan camera
                            if (engine.selectedIndices.isEmpty() && (diffX > 15f || diffY > 15f)) {
                                engine.camX -= dragAmount.x / engine.camZ
                                engine.camY -= dragAmount.y / engine.camZ
                                engine.camX = engine.camX.coerceIn(0f, GameEngine.BW)
                                engine.camY = engine.camY.coerceIn(0f, GameEngine.BH)
                            }
                        }
                    },
                    onDragEnd = {
                        val s = dragStart
                        val c = dragCurrent
                        if (s != null && c != null) {
                            val dx = abs(c.x - s.x)
                            val dy = abs(c.y - s.y)
                            if (dx > 20f || dy > 20f) {
                                val wStart = screenToWorld(s.x, s.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
                                val wEnd = screenToWorld(c.x, c.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
                                engine.selectInBounds(wStart.x, wStart.y, wEnd.x, wEnd.y)
                            }
                        }
                        dragStart = null
                        dragCurrent = null
                    },
                    onDragCancel = {
                        dragStart = null
                        dragCurrent = null
                    }
                )
            }
    ) {
        canvasSize = size

        try {
            // 1. Draw Tactical Ocean Base
            drawRect(color = Color(0xFF10160F))

            // 1b. Draw Screen-Bounded Map Texture (avoids huge 14k texture allocation)
            if (mapTextureBitmap != null && engine.camZ > 0.05f) {
                try {
                    val halfVisW = size.width / (2f * engine.camZ)
                    val halfVisH = size.height / (2f * engine.camZ)
                    val wLeft = (engine.camX - halfVisW).coerceIn(0f, GameEngine.BW)
                    val wTop = (engine.camY - halfVisH).coerceIn(0f, GameEngine.BH)
                    val wRight = (engine.camX + halfVisW).coerceIn(0f, GameEngine.BW)
                    val wBottom = (engine.camY + halfVisH).coerceIn(0f, GameEngine.BH)

                    if (wRight > wLeft && wBottom > wTop) {
                        val p0 = worldToScreen(wLeft, wTop, engine.camX, engine.camY, engine.camZ, size.width, size.height, engine.shakeX, engine.shakeY)
                        val p1 = worldToScreen(wRight, wBottom, engine.camX, engine.camY, engine.camZ, size.width, size.height, engine.shakeX, engine.shakeY)

                        val srcX = ((wLeft / GameEngine.BW) * mapTextureBitmap.width).toInt().coerceIn(0, mapTextureBitmap.width - 1)
                        val srcY = ((wTop / GameEngine.BH) * mapTextureBitmap.height).toInt().coerceIn(0, mapTextureBitmap.height - 1)
                        val srcW = (((wRight - wLeft) / GameEngine.BW) * mapTextureBitmap.width).toInt().coerceIn(1, mapTextureBitmap.width - srcX)
                        val srcH = (((wBottom - wTop) / GameEngine.BH) * mapTextureBitmap.height).toInt().coerceIn(1, mapTextureBitmap.height - srcY)

                        val dstX = p0.x.toInt()
                        val dstY = p0.y.toInt()
                        val dstW = max(1, (p1.x - p0.x).toInt())
                        val dstH = max(1, (p1.y - p0.y).toInt())

                        drawImage(
                            image = mapTextureBitmap,
                            srcOffset = androidx.compose.ui.unit.IntOffset(srcX, srcY),
                            srcSize = androidx.compose.ui.unit.IntSize(srcW, srcH),
                            dstOffset = androidx.compose.ui.unit.IntOffset(dstX, dstY),
                            dstSize = androidx.compose.ui.unit.IntSize(dstW, dstH)
                        )
                    }
                } catch (_: Throwable) {
                    // Safe fallback
                }
            }

            // Snapshots to guarantee thread safety and prevent ConcurrentModificationException
            val seaLabels = engine.getSeaLabelsSnapshot()
            val rocks = engine.getRocksSnapshot()
            val territories = engine.getTerritoriesSnapshot()
            val hazardZones = engine.getHazardZonesSnapshot()
            val aliveUnits = engine.getAliveUnitsSnapshot()
            val selectedIndices = engine.getSelectedIndicesSnapshot()
            val particles = engine.getParticlesSnapshot()

            // 2. Draw Sea Labels
            drawSeaLabels(seaLabels, engine, size)

            // 3. Draw Outer Rock Borders
            drawOuterRocks(rocks, engine, size)

            // 4. Draw Territories
            drawTerritories(territories, engine, size)

            // 5. Draw Hazard Zones (Lumpur, Api, Air Suci)
            drawHazardZones(hazardZones, engine, size)

            // 6. Draw Units
            drawUnits(aliveUnits, selectedIndices, territories, engine, size, generalAvatarBitmap)

            // 7. Draw Selection Box
            val s = dragStart
            val c = dragCurrent
            if (s != null && c != null && selectedIndices.isNotEmpty()) {
                val left = min(s.x, c.x)
                val top = min(s.y, c.y)
                val w = abs(c.x - s.x)
                val h = abs(c.y - s.y)
                drawRect(
                    color = Color(0x22D4A832),
                    topLeft = Offset(left, top),
                    size = Size(w, h)
                )
                drawRect(
                    color = Color(0xFFD4A832),
                    topLeft = Offset(left, top),
                    size = Size(w, h),
                    style = Stroke(width = 2f)
                )
            }

            // 8. Draw Particles
            for (pt in particles) {
                val sp = worldToScreen(pt.x, pt.y, engine.camX, engine.camY, engine.camZ, size.width, size.height, engine.shakeX, engine.shakeY)
                val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(pt.colorHex).copy(alpha = alpha),
                    radius = max(1.5f, pt.size * engine.camZ),
                    center = sp
                )
            }
        } catch (_: Throwable) {
            // Safeguard against any unexpected draw crash
        }
    }
}

private fun DrawScope.drawSeaLabels(seaLabels: List<com.example.data.model.SeaLabel>, engine: GameEngine, canvasSize: Size) {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    for (sea in seaLabels) {
        val sp = worldToScreen(sea.x, sea.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
        if (sp.x < -150f || sp.x > canvasSize.width + 150f || sp.y < -50f || sp.y > canvasSize.height + 50f) continue

        val fontSize = max(11f, 16f * sea.scale * engine.camZ)
        paint.textSize = fontSize
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = max(2f, 3.5f * engine.camZ)
        paint.color = android.graphics.Color.argb(200, 8, 20, 30)
        drawContext.canvas.nativeCanvas.drawText(sea.name, sp.x, sp.y, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = android.graphics.Color.argb(220, 215, 238, 248)
        drawContext.canvas.nativeCanvas.drawText(sea.name, sp.x, sp.y, paint)
    }
}

private fun DrawScope.drawOuterRocks(rocks: List<GameEngine.Rock>, engine: GameEngine, canvasSize: Size) {
    for (rk in rocks) {
        val sp = worldToScreen(rk.x, rk.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
        val rr = rk.r * engine.camZ
        if (sp.x < -rr || sp.x > canvasSize.width + rr || sp.y < -rr || sp.y > canvasSize.height + rr) continue

        val shade = (26 + (rk.shade * 36f).toInt()).coerceIn(20, 80)
        val rColor = Color(shade, (shade * 0.78f).toInt(), (shade * 0.58f).toInt())
        drawCircle(color = rColor, radius = rr, center = sp)
        drawCircle(color = Color(0x55000000), radius = rr, center = sp, style = Stroke(width = max(1f, engine.camZ)))
    }
}

private fun DrawScope.drawTerritories(territories: List<Territory>, engine: GameEngine, canvasSize: Size) {
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        isFakeBoldText = true
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    for (te in territories) {
        val path = Path()
        var first = true
        for (pt in te.polygon) {
            val sp = worldToScreen(pt.x, pt.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
            if (first) {
                path.moveTo(sp.x, sp.y)
                first = false
            } else {
                path.lineTo(sp.x, sp.y)
            }
        }
        path.close()

        val fillColor = if (te.team == "p") Color(0x384A9A4A) else Color(te.colorHex)
        val strokeColor = if (te.team == "p") Color(0xFF4A9A4A) else Color(te.borderHex)

        drawPath(path = path, color = fillColor)
        drawPath(path = path, color = strokeColor, style = Stroke(width = max(1.5f, 2.5f * engine.camZ)))

        val centerScreen = worldToScreen(te.cx, te.cy, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
        val textSize = max(11f, 17f * engine.camZ)
        textPaint.textSize = textSize

        textPaint.style = android.graphics.Paint.Style.STROKE
        textPaint.strokeWidth = max(2.5f, 4f * engine.camZ)
        textPaint.color = android.graphics.Color.BLACK
        drawContext.canvas.nativeCanvas.drawText(te.name, centerScreen.x, centerScreen.y, textPaint)

        textPaint.style = android.graphics.Paint.Style.FILL
        textPaint.color = if (te.team == "p") android.graphics.Color.rgb(190, 230, 190) else android.graphics.Color.rgb(255, 247, 216)
        drawContext.canvas.nativeCanvas.drawText(te.name, centerScreen.x, centerScreen.y, textPaint)

        if (te.team == "p") {
            val subY = centerScreen.y + max(16f, 20f * engine.camZ)
            textPaint.style = android.graphics.Paint.Style.STROKE
            drawContext.canvas.nativeCanvas.drawText("[TAKLUK]", centerScreen.x, subY, textPaint)
            textPaint.style = android.graphics.Paint.Style.FILL
            drawContext.canvas.nativeCanvas.drawText("[TAKLUK]", centerScreen.x, subY, textPaint)
        }
    }
}

private fun DrawScope.drawHazardZones(hazardZones: List<com.example.data.model.HazardZone>, engine: GameEngine, canvasSize: Size) {
    for (zone in hazardZones) {
        val spCenter = worldToScreen(zone.x, zone.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
        val sr = zone.radius * engine.camZ
        if (spCenter.x < -sr || spCenter.x > canvasSize.width + sr || spCenter.y < -sr || spCenter.y > canvasSize.height + sr) continue

        val path = Path()
        var first = true
        for (pt in zone.polygon) {
            val sp = worldToScreen(pt.x, pt.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
            if (first) {
                path.moveTo(sp.x, sp.y)
                first = false
            } else {
                path.lineTo(sp.x, sp.y)
            }
        }
        path.close()

        when (zone.type) {
            "lumpur" -> {
                drawPath(path = path, color = Color(0x776B4423))
                drawPath(path = path, color = Color(0xFF6B4423), style = Stroke(width = max(1.5f, 2f * engine.camZ)))
            }
            "air" -> {
                drawPath(path = path, color = Color(0x66C8F4FF))
                drawPath(path = path, color = Color(0xFFEAFCFF), style = Stroke(width = max(1.5f, 2f * engine.camZ)))
            }
            "api" -> {
                drawPath(path = path, color = Color(0x88FF6414))
                drawPath(path = path, color = Color(0xFFFF7A1A), style = Stroke(width = max(2f, 3f * engine.camZ)))
            }
        }
    }
}

private fun DrawScope.drawUnits(
    aliveUnits: List<UnitPiece>,
    selectedIndices: Set<Int>,
    territories: List<Territory>,
    engine: GameEngine,
    canvasSize: Size,
    avatarBitmap: ImageBitmap?
) {
    val pgen = aliveUnits.find { it.isGeneral && it.team == "p" }

    val namePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        isFakeBoldText = true
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = android.graphics.Paint.Align.CENTER
    }

    for (p in aliveUnits) {
        val sp = worldToScreen(p.x, p.y, engine.camX, engine.camY, engine.camZ, canvasSize.width, canvasSize.height, engine.shakeX, engine.shakeY)
        val r = GameEngine.PR * engine.camZ * (if (p.isGeneral) 2.2f else 1f)
        if (sp.x < -30f || sp.x > canvasSize.width + 30f || sp.y < -30f || sp.y > canvasSize.height + 30f) continue

        // General Sight fog limit
        if (pgen != null && pgen.isAlive && p != pgen) {
            val gdx = p.x - pgen.x
            val gdy = p.y - pgen.y
            if (gdx * gdx + gdy * gdy > GameEngine.GEN_SIGHT * GameEngine.GEN_SIGHT) continue
        }

        val isPlayer = p.team == "p"
        val isMyGeneral = isPlayer && p.isGeneral

        // Drop shadow
        drawCircle(
            color = Color(0x44000000),
            radius = r,
            center = Offset(sp.x + 2f * engine.camZ, sp.y + 2f * engine.camZ)
        )

        if (isMyGeneral && avatarBitmap != null) {
            // Draw general avatar circle
            val avatarSize = max(1, (r * 2).toInt())
            drawImage(
                image = avatarBitmap,
                dstOffset = androidx.compose.ui.unit.IntOffset((sp.x - r).toInt(), (sp.y - r).toInt()),
                dstSize = androidx.compose.ui.unit.IntSize(avatarSize, avatarSize)
            )
            drawCircle(color = Color(0xFFD4A832), radius = r, center = sp, style = Stroke(width = max(2f, 3f * engine.camZ)))
        } else {
            // Unit body circle with front/back orientation
            val frontColor = if (p.hitFlash > 0f) Color.White else if (isPlayer) Color(0xFFD4A832) else Color(0xFFC44040)
            val backColor = if (isPlayer) Color(0xFF7A5A10) else Color(0xFF6A1A1A)

            drawArc(
                color = frontColor,
                startAngle = (Math.toDegrees(p.angle.toDouble()) - 90).toFloat(),
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(sp.x - r, sp.y - r),
                size = Size(r * 2, r * 2)
            )
            drawArc(
                color = backColor,
                startAngle = (Math.toDegrees(p.angle.toDouble()) + 90).toFloat(),
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(sp.x - r, sp.y - r),
                size = Size(r * 2, r * 2)
            )

            // Dividing line between front and back
            val perpA = p.angle + (PI / 2).toFloat()
            val lx1 = sp.x + cos(perpA) * r
            val ly1 = sp.y + sin(perpA) * r
            val lx2 = sp.x - cos(perpA) * r
            val ly2 = sp.y - sin(perpA) * r
            drawLine(color = Color(0x88FFFFFF), start = Offset(lx1, ly1), end = Offset(lx2, ly2), strokeWidth = max(1f, 1.5f * engine.camZ))
        }

        // Direction arrow
        val arrowDist = r + 4f * engine.camZ
        val arrowSize = 3.5f * engine.camZ
        val ax = sp.x + cos(p.angle) * arrowDist
        val ay = sp.y + sin(p.angle) * arrowDist
        val aColor = if (isPlayer) Color(0xFFFFE066) else Color(0xFFFF6666)

        val arrowPath = Path().apply {
            moveTo(ax + cos(p.angle) * arrowSize, ay + sin(p.angle) * arrowSize)
            lineTo(ax + cos(p.angle + 2.4f) * arrowSize, ay + sin(p.angle + 2.4f) * arrowSize)
            lineTo(ax + cos(p.angle - 2.4f) * arrowSize, ay + sin(p.angle - 2.4f) * arrowSize)
            close()
        }
        drawPath(path = arrowPath, color = aColor)

        // General gold ring
        if (p.isGeneral) {
            val ringColor = if (isPlayer) Color(0xFFFFF2A8) else Color(0xFFFFB0B0)
            drawCircle(color = ringColor, radius = r * 1.25f, center = sp, style = Stroke(width = max(2f, 3f * engine.camZ)))
        }

        // Selection circle
        if (selectedIndices.contains(p.id)) {
            drawCircle(color = Color(0xFFFFE066), radius = r + 2.5f * engine.camZ, center = sp, style = Stroke(width = max(1.5f, 2.5f * engine.camZ)))
        }

        // HP Bar
        val mh = p.maxHp
        if ((p.hp < mh || p.isGeneral) && p.hp > 0f) {
            val barW = r * 1.8f
            val barH = max(2f, 3f * engine.camZ)
            val barX = sp.x - barW / 2
            val barY = sp.y - r - 7f * engine.camZ
            drawRect(color = Color(0xAA000000), topLeft = Offset(barX, barY), size = Size(barW, barH))
            val hpColor = when {
                p.hp > mh * 0.5f -> Color(0xFF4A9A4A)
                p.hp > mh * 0.25f -> Color(0xFF9A9A2A)
                else -> Color(0xFF9A2A2A)
            }
            drawRect(color = hpColor, topLeft = Offset(barX, barY), size = Size(barW * (p.hp / mh).coerceIn(0f, 1f), barH))
        }

        // General Name above unit
        if (isMyGeneral) {
            val nameY = sp.y - r - 16f * engine.camZ
            namePaint.textSize = max(10f, 13f * engine.camZ)
            namePaint.style = android.graphics.Paint.Style.STROKE
            namePaint.strokeWidth = 3f
            namePaint.color = android.graphics.Color.BLACK
            drawContext.canvas.nativeCanvas.drawText(engine.playerName, sp.x, nameY, namePaint)
            namePaint.style = android.graphics.Paint.Style.FILL
            namePaint.color = android.graphics.Color.WHITE
            drawContext.canvas.nativeCanvas.drawText(engine.playerName, sp.x, nameY, namePaint)
        } else if (!isPlayer && p.isGeneral && p.territoryIndex in territories.indices) {
            val rulerName = territories[p.territoryIndex].ruler
            val nameY = sp.y - r - 16f * engine.camZ
            namePaint.textSize = max(10f, 13f * engine.camZ)
            namePaint.style = android.graphics.Paint.Style.STROKE
            namePaint.strokeWidth = 3f
            namePaint.color = android.graphics.Color.BLACK
            drawContext.canvas.nativeCanvas.drawText(rulerName, sp.x, nameY, namePaint)
            namePaint.style = android.graphics.Paint.Style.FILL
            namePaint.color = android.graphics.Color.WHITE
            drawContext.canvas.nativeCanvas.drawText(rulerName, sp.x, nameY, namePaint)
        }
    }
}

fun worldToScreen(
    bx: Float,
    by: Float,
    camX: Float,
    camY: Float,
    camZ: Float,
    screenWidth: Float,
    screenHeight: Float,
    shakeX: Float = 0f,
    shakeY: Float = 0f
): Offset {
    val sx = (bx - camX) * camZ + screenWidth / 2f + shakeX
    val sy = (by - camY) * camZ + screenHeight / 2f + shakeY
    return Offset(sx, sy)
}

fun screenToWorld(
    sx: Float,
    sy: Float,
    camX: Float,
    camY: Float,
    camZ: Float,
    screenWidth: Float,
    screenHeight: Float,
    shakeX: Float = 0f,
    shakeY: Float = 0f
): Point2D {
    val bx = (sx - screenWidth / 2f - shakeX) / camZ + camX
    val by = (sy - screenHeight / 2f - shakeY) / camZ + camY
    return Point2D(bx, by)
}
