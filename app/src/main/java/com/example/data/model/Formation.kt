package com.example.data.model

import kotlin.math.*

data class FormationSlot(
    val ox: Float,
    val oy: Float,
    val ang: Float? = null
)

object FormationHelper {
    private const val PR = 9f
    private const val GAP = PR * 2.4f

    fun globusHoleCount(rings: Int): Int {
        var cnt = 1
        for (ring in 1 until rings) {
            val rad = ring * GAP
            cnt += max(6, (PI * 2 * rad / GAP).roundToInt())
        }
        return cnt
    }

    private fun spatialKeyOfSlot(s: FormationSlot, mode: String, simplexOri: Int, duplexOri: Int): Float {
        return when (mode) {
            "simplex", "duplex" -> {
                val horiz = if (mode == "simplex") (simplexOri % 2 == 0) else (duplexOri % 2 == 0)
                if (horiz) s.ox else s.oy
            }
            "vshape" -> sqrt(s.ox * s.ox + s.oy * s.oy)
            else -> atan2(s.oy, s.ox)
        }
    }

    fun spatialSortKey(p: UnitPiece, cx0: Float, cy0: Float, mode: String, simplexOri: Int, duplexOri: Int): Float {
        return when (mode) {
            "simplex", "duplex" -> {
                val horiz = if (mode == "simplex") (simplexOri % 2 == 0) else (duplexOri % 2 == 0)
                val lineAxis = if (horiz) (p.y - cy0) else (p.x - cx0)
                val along = if (horiz) (p.x - cx0) else (p.y - cy0)
                lineAxis * 100000f + along
            }
            "vshape" -> {
                val side = if ((p.y - cy0) >= 0) 0f else 1f
                val d = hypot((p.x - cx0).toDouble(), (p.y - cy0).toDouble()).toFloat()
                side * 1000000f + d
            }
            else -> atan2(p.y - cy0, p.x - cx0)
        }
    }

    fun formatOffsets(
        n: Int,
        mode: String,
        holeCount: Int = 0,
        simplexOri: Int = 0,
        duplexOri: Int = 0,
        vshapeDir: Int = 0
    ): List<FormationSlot> {
        if (n <= 0) return emptyList()

        return when (mode) {
            "globus" -> {
                val slots = mutableListOf<FormationSlot>()
                slots.add(FormationSlot(0f, 0f, 0f))
                var ring = 1
                while (slots.size < n + holeCount + 40) {
                    val rad = ring * GAP
                    val cnt = max(6, (PI * 2 * rad / GAP).roundToInt())
                    for (k in 0 until cnt) {
                        val ang = (PI * 2 * k / cnt).toFloat()
                        slots.add(FormationSlot(cos(ang) * rad, sin(ang) * rad, ang))
                    }
                    ring++
                    if (ring > 80) break
                }
                slots.sortBy { slot: FormationSlot -> slot.ox * slot.ox + slot.oy * slot.oy }
                val start = min(holeCount, slots.size)
                val end = min(start + n, slots.size)
                val picked = slots.subList(start, end).toMutableList()
                picked.sortBy { slot: FormationSlot -> spatialKeyOfSlot(slot, mode, simplexOri, duplexOri) }
                picked
            }
            "simplex", "duplex" -> {
                val lines = if (mode == "simplex") 1 else 2
                val horiz = if (mode == "simplex") (simplexOri % 2 == 0) else (duplexOri % 2 == 0)
                val maxPerLine = ceil(n.toDouble() / lines).toInt() + 30
                val allSlots = mutableListOf<FormationSlot>()

                for (line in 0 until lines) {
                    val lineOff = (line - (lines - 1) / 2f) * GAP * 1.8f
                    for (pos in 0 until maxPerLine) {
                        val k = ceil(pos / 2.0).toInt() * (if (pos % 2 == 0) 1 else -1)
                        val along = k * GAP
                        val ox = if (horiz) along else lineOff
                        val oy = if (horiz) lineOff else along
                        allSlots.add(FormationSlot(ox, oy, null))
                    }
                }

                val byLine = List(lines) { line ->
                    allSlots.filterIndexed { idx, _ -> idx / maxPerLine == line }
                }
                val picked = mutableListOf<FormationSlot>()
                val lineIndices = IntArray(lines)

                while (picked.size < n) {
                    for (line in 0 until lines) {
                        if (picked.size < n && lineIndices[line] < byLine[line].size) {
                            picked.add(byLine[line][lineIndices[line]])
                            lineIndices[line]++
                        }
                    }
                }

                picked.sortWith { a, b ->
                    val la = if (horiz) a.oy else a.ox
                    val lb = if (horiz) b.oy else b.ox
                    if (la != lb) la.compareTo(lb)
                    else spatialKeyOfSlot(a, mode, simplexOri, duplexOri).compareTo(spatialKeyOfSlot(b, mode, simplexOri, duplexOri))
                }
                picked
            }
            "vshape" -> {
                val rot = when (vshapeDir % 4) {
                    0 -> 0f
                    1 -> (PI / 2).toFloat()
                    2 -> PI.toFloat()
                    else -> (-PI / 2).toFloat()
                }
                val cs = cos(rot)
                val sn = sin(rot)
                val maxPos = ceil(n / 2.0).toInt() + 30
                data class TempV(val lx: Float, val ly: Float)
                val tempSlots = mutableListOf<TempV>()
                tempSlots.add(TempV(0f, 0f))
                for (pos in 1 until maxPos) {
                    val along = pos * GAP * 0.85f
                    val spread = pos * GAP * 0.85f
                    tempSlots.add(TempV(-along, spread))
                    tempSlots.add(TempV(-along, -spread))
                }
                tempSlots.sortBy { it.lx * it.lx + it.ly * it.ly }
                val picked = tempSlots.take(n).map { s ->
                    val ox = s.lx * cs - s.ly * sn
                    val oy = s.lx * sn + s.ly * cs
                    FormationSlot(ox, oy, null)
                }.toMutableList()

                picked.sortBy { slot: FormationSlot -> spatialKeyOfSlot(slot, mode, simplexOri, duplexOri) }
                picked
            }
            else -> emptyList()
        }
    }
}
