package com.example.engine

import com.example.data.model.*
import kotlin.math.*
import kotlin.random.Random

class GameEngine {

    companion object {
        const val BW = 16800f
        const val BH = 9108f
        const val PR = 9f
        const val SR = 34f
        const val ORB = 82.5f
        const val AR = 180f
        const val MS = 50f
        const val DR = 26f
        val FH = (PI * 0.42).toFloat()
        const val BORDER = 390f
        const val GEN_SIGHT = 1000f
        const val COLR = PR * 2f
        const val RSR = SR * 1.15f
        const val BDR = ORB * 1.15f
        const val GCELL = 100f
        const val CCELL = 40f
    }

    var playerName: String = "Jenderal"
    var playerAvatarId: Int = 0

    val units = mutableListOf<UnitPiece>()
    val territories = mutableListOf<Territory>()
    val seaLabels = mutableListOf<SeaLabel>()
    val rocks = mutableListOf<Rock>()
    val particles = mutableListOf<Particle>()
    val selectedIndices = mutableSetOf<Int>()

    val hazardZones = mutableListOf<HazardZone>()
    private var hazardTimer = 0f

    var playerGeneralIndex: Int = 22
    val playerGeneral: UnitPiece?
        get() = if (playerGeneralIndex in units.indices) units[playerGeneralIndex] else null

    var camX = 1450f
    var camY = 2500f
    var camZ = 0.85f

    var speed = 1.5f
    var isPaused = false
    var isGameOver = false
    var gameOverMessage = ""
    var isVictory = false

    var moveMode = "atk" // "atk" or "goto"
    var formMode: String? = null // null, "globus", "simplex", "duplex", "vshape"
    var simplexOri = 0
    var duplexOri = 0
    var vshapeDir = 0
    val genModes = listOf("kiri", "bawah", "kanan", "atas", "tengah")
    var genModeIdx = 4
    val genMode: String
        get() = genModes[genModeIdx]

    var shake = 0f
    var shakeX = 0f
    var shakeY = 0f

    private var wilTimer = 0f
    private var gidCount = mutableMapOf<Int, Int>()
    private var nextGroupId = 1

    var onToastMessage: ((String) -> Unit)? = null
    var onTerritoryStatusChanged: (() -> Unit)? = null

    data class Rock(val x: Float, val y: Float, val r: Float, val shade: Float)
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        val maxLife: Float,
        val size: Float,
        val colorHex: Long
    )

    val lock = Any()

    init {
        synchronized(lock) {
            initTerritories()
            initSeaLabels()
            initRocks()
            initUnits()
            initHazards()
        }
    }

    fun resetGame(name: String = playerName, avatar: Int = playerAvatarId) {
        synchronized(lock) {
            playerName = name
            playerAvatarId = avatar
            units.clear()
            territories.clear()
            rocks.clear()
            particles.clear()
            selectedIndices.clear()
            hazardZones.clear()
            gidCount.clear()

            camX = 1450f
            camY = 2500f
            camZ = 0.85f
            isPaused = false
            isGameOver = false
            gameOverMessage = ""
            isVictory = false
            shake = 0f
            shakeX = 0f
            shakeY = 0f

            initTerritories()
            initSeaLabels()
            initRocks()
            initUnits()
            initHazards()
        }
    }

    fun getAliveUnitsSnapshot(): List<UnitPiece> {
        return synchronized(lock) {
            units.filter { it.isAlive && !it.isHidden }.sortedBy { it.y }
        }
    }

    fun getAllUnitsSnapshot(): List<UnitPiece> {
        return synchronized(lock) {
            units.toList()
        }
    }

    fun getTerritoriesSnapshot(): List<Territory> {
        return synchronized(lock) {
            territories.toList()
        }
    }

    fun getSeaLabelsSnapshot(): List<SeaLabel> {
        return synchronized(lock) {
            seaLabels.toList()
        }
    }

    fun getRocksSnapshot(): List<Rock> {
        return synchronized(lock) {
            rocks.toList()
        }
    }

    fun getHazardZonesSnapshot(): List<HazardZone> {
        return synchronized(lock) {
            hazardZones.toList()
        }
    }

    fun getParticlesSnapshot(): List<Particle> {
        return synchronized(lock) {
            particles.toList()
        }
    }

    fun getSelectedIndicesSnapshot(): Set<Int> {
        return synchronized(lock) {
            selectedIndices.toSet()
        }
    }

    private fun initSeaLabels() {
        seaLabels.clear()
        seaLabels.addAll(
            listOf(
                SeaLabel("Sagara Kidul", 500f, 7400f, 1.15f),
                SeaLabel("Sagara Amarah", 500f, 3700f, 1.0f),
                SeaLabel("Sagara Kalapa", 600f, 1700f, 1.0f),
                SeaLabel("Palung Sancang", 150f, 7000f, 0.9f),
                SeaLabel("Selat Jaladri", 2800f, 6900f, 0.85f),
                SeaLabel("Teluk Layung", 1900f, 5200f, 0.85f),
                SeaLabel("Segara Anakan", 1200f, 3300f, 0.85f),
                SeaLabel("Leuwi Buaya", 3800f, 6650f, 0.85f),
                SeaLabel("Perairan Nusa Papak", 3400f, 5900f, 0.85f),
                SeaLabel("Muara Jati", 4500f, 900f, 0.85f)
            )
        )
    }

    private fun initRocks() {
        rocks.clear()
        fun addBand(n: Int, xf: () -> Float, yf: () -> Float) {
            for (k in 0 until n) {
                rocks.add(Rock(xf(), yf(), 7f + Random.nextFloat() * 16f, Random.nextFloat()))
            }
        }
        val topBottomCount = (BW / 38).roundToInt()
        val sideCount = (BH / 38).roundToInt()
        addBand(topBottomCount, { Random.nextFloat() * BW }, { -Random.nextFloat() * BORDER * 0.95f })
        addBand(topBottomCount, { Random.nextFloat() * BW }, { BH + Random.nextFloat() * BORDER * 0.95f })
        addBand(sideCount, { -Random.nextFloat() * BORDER * 0.95f }, { Random.nextFloat() * BH })
        addBand(sideCount, { BW + Random.nextFloat() * BORDER * 0.95f }, { Random.nextFloat() * BH })
    }

    private fun initUnits() {
        units.clear()
        val homeX = 1400f
        val homeY = 2500f

        // 50 Player units in grid formation
        for (i in 0 until 50) {
            val rw = i % 10
            val cl = i / 10
            val ux = homeX - 180f + rw * 40f + (Random.nextFloat() - 0.5f) * 12f
            val uy = homeY - 144f + cl * 36f + (Random.nextFloat() - 0.5f) * 12f
            val p = UnitPiece(
                id = i,
                x = ux,
                y = uy,
                team = "p",
                territoryIndex = -1,
                angle = 0f
            )
            units.add(p)
        }

        playerGeneralIndex = 22
        val pgen = units[playerGeneralIndex]
        pgen.isGeneral = true
        pgen.maxHp = 400f
        pgen.hp = 400f

        var nextId = 50
        val splitXSancang = 2300f

        for (t in territories.indices) {
            val te = territories[t]
            var generalPos = Point2D(te.cx, te.cy)
            val enemyCount = te.enemyCount

            for (j in 0 until enemyCount) {
                var spawn = if (te.name == "Hutan Sancang") {
                    randPointInPolyMinX(te, splitXSancang)
                } else {
                    randPointInPoly(te)
                }

                if (j == 0) {
                    generalPos = spawn
                } else if (j <= 12) {
                    for (k2 in 0 until 12) {
                        val an = Random.nextFloat() * 6.283f
                        val rd = 45f + Random.nextFloat() * 70f
                        val qx = generalPos.x + cos(an) * rd
                        val qy = generalPos.y + sin(an) * rd
                        if (te.containsPoint(qx, qy)) {
                            spawn = Point2D(qx, qy)
                            break
                        }
                    }
                }

                val p = UnitPiece(
                    id = nextId++,
                    x = spawn.x,
                    y = spawn.y,
                    team = "e",
                    territoryIndex = t,
                    angle = Math.PI.toFloat(),
                    isHidden = true
                )

                if (j == 0) {
                    p.isGeneral = true
                    p.maxHp = 300f
                    p.hp = 300f
                    te.state = "idle"
                    val forms = listOf("globus", "simplex", "duplex", "vshape")
                    te.formation = forms[Random.nextInt(forms.size)]
                }

                p.role = if (j > 12 && j % 3 == 0) "inf" else "atk"
                units.add(p)
            }
        }
    }

    private fun randPointInPoly(te: Territory): Point2D {
        val bb = te.boundingBox
        for (i in 0 until 24) {
            val rx = bb.minX + Random.nextFloat() * (bb.maxX - bb.minX)
            val ry = bb.minY + Random.nextFloat() * (bb.maxY - bb.minY)
            if (te.containsPoint(rx, ry)) return Point2D(rx, ry)
        }
        return Point2D(te.cx, te.cy)
    }

    private fun randPointInPolyMinX(te: Territory, minX: Float): Point2D {
        val bb = te.boundingBox
        val loX = max(bb.minX, minX)
        for (i in 0 until 24) {
            val rx = loX + Random.nextFloat() * (bb.maxX - loX)
            val ry = bb.minY + Random.nextFloat() * (bb.maxY - bb.minY)
            if (te.containsPoint(rx, ry)) return Point2D(rx, ry)
        }
        return randPointInPoly(te)
    }

    private fun initHazards() {
        hazardZones.clear()
        val types = listOf("lumpur", "api", "air")
        val pgen = playerGeneral

        for (t in territories.indices) {
            val te = territories[t]
            if (Random.nextFloat() > 0.65f) continue
            val ty = types[Random.nextInt(types.size)]
            val nz = if (Random.nextFloat() < 0.4f) 2 else 1

            for (z in 0 until nz) {
                for (k in 0 until 25) {
                    val pt = if (te.name == "Hutan Sancang") randPointInPolyMinX(te, 2300f) else randPointInPoly(te)
                    val r = ((if (ty == "api") 50f else 70f) + Random.nextFloat() * 60f) * 3f
                    if (WaterGrid.isWater(pt.x, pt.y)) continue
                    if (pgen != null && distance(pt.x, pt.y, pgen.x, pgen.y) < 400f) continue
                    if (distance(pt.x, pt.y, te.cx, te.cy) < r + 120f) continue

                    val poly = generateBlob(pt.x, pt.y, r)
                    hazardZones.add(HazardZone(ty, pt.x, pt.y, r, poly))
                    break
                }
            }
        }
    }

    private fun generateBlob(cx: Float, cy: Float, r: Float): List<Point2D> {
        val sides = 8 + Random.nextInt(5)
        val pts = mutableListOf<Point2D>()
        for (k in 0 until sides) {
            val ang = (PI * 2 * k / sides + Random.nextFloat() * 0.25f).toFloat()
            val rad = r * (0.68f + Random.nextFloat() * 0.58f)
            pts.add(Point2D(cx + cos(ang) * rad, cy + sin(ang) * rad))
        }
        return pts
    }

    fun selectInBounds(x0: Float, y0: Float, x1: Float, y1: Float) {
        synchronized(lock) {
            val lx = min(x0, x1)
            val rx = max(x0, x1)
            val ly = min(y0, y1)
            val ry = max(y0, y1)

            selectedIndices.clear()
            for (i in units.indices) {
                val p = units[i]
                if (p.isAlive && p.team == "p" && p.x in lx..rx && p.y in ly..ry) {
                    selectedIndices.add(i)
                }
            }
        }
    }

    fun clearSelection() {
        synchronized(lock) {
            selectedIndices.clear()
        }
    }

    fun orderMove(targetX: Float, targetY: Float) {
        synchronized(lock) {
            if (selectedIndices.isEmpty()) return
            val idxList = selectedIndices.toList()

        var generalIdx = -1
        val unitIdxs = idxList.toMutableList()
        val currentPgenIdx = playerGeneralIndex
        if (formMode != null && unitIdxs.size > 1) {
            val gi = unitIdxs.indexOf(currentPgenIdx)
            if (gi >= 0) {
                generalIdx = currentPgenIdx
                unitIdxs.removeAt(gi)
            }
        }

        val holeCount = if (generalIdx >= 0 && formMode == "globus" && genMode == "tengah") {
            min(FormationHelper.globusHoleCount(3), 30)
        } else 0

        val offsets = if (formMode != null) {
            FormationHelper.formatOffsets(
                unitIdxs.size,
                formMode!!,
                holeCount,
                simplexOri,
                duplexOri,
                vshapeDir
            )
        } else null

        val finalOffsets = if (offsets == null || offsets.size < unitIdxs.size) {
            var cx0 = 0f
            var cy0 = 0f
            for (idx in unitIdxs) {
                cx0 += units[idx].x
                cy0 += units[idx].y
            }
            cx0 /= unitIdxs.size
            cy0 /= unitIdxs.size
            unitIdxs.map { idx ->
                val p = units[idx]
                FormationSlot(p.x - cx0, p.y - cy0, null)
            }
        } else {
            var sameGroup = true
            val refGid = units[unitIdxs[0]].groupId
            for (idx in unitIdxs) {
                val p = units[idx]
                if (p.formation != formMode || p.groupId != refGid || p.groupId == 0) {
                    sameGroup = false
                    break
                }
            }
            if (sameGroup) {
                val seen = BooleanArray(unitIdxs.size)
                for (idx in unitIdxs) {
                    val si = units[idx].slotIdx
                    if (si < 0 || si >= unitIdxs.size || seen[si]) {
                        sameGroup = false
                        break
                    }
                    seen[si] = true
                }
            }
            if (!sameGroup) {
                var cx0 = 0f
                var cy0 = 0f
                for (idx in unitIdxs) {
                    cx0 += units[idx].x
                    cy0 += units[idx].y
                }
                cx0 /= unitIdxs.size
                cy0 /= unitIdxs.size
                unitIdxs.sortBy { idx ->
                    FormationHelper.spatialSortKey(units[idx], cx0, cy0, formMode!!, simplexOri, duplexOri)
                }
                for (k in unitIdxs.indices) {
                    units[unitIdxs[k]].slotIdx = k
                }
            }
            offsets
        }

        val gid = nextGroupId++
        val isGoto = (moveMode == "goto")

        for (k in unitIdxs.indices) {
            val p = units[unitIdxs[k]]
            p.obey = isGoto
            p.order = "move"
            p.targetIdx = null
            p.zone = null
            p.isEngaged = false
            p.isRetreating = false
            p.orbiting = null
            p.isGotoBlocked = false
            p.holdBack = false

            val slotK = if (formMode != null) p.slotIdx else k
            val safeSlot = slotK.coerceIn(0, finalOffsets.size - 1)
            p.ox = finalOffsets[safeSlot].ox
            p.oy = finalOffsets[safeSlot].oy
            p.gtx = targetX
            p.gty = targetY
            p.groupId = gid
            p.formation = formMode
            p.formAng = finalOffsets[safeSlot].ang
            p.kxHole = holeCount
        }

        if (formMode != null) {
            gidCount[gid] = unitIdxs.size
        }

        if (generalIdx < 0 && unitIdxs.contains(currentPgenIdx)) {
            val g2 = units[currentPgenIdx]
            g2.baseGtx = targetX
            g2.baseGty = targetY
            g2.gotoOrder = isGoto
            g2.formSnap = null
            g2.genModeSnap = null
        }

        if (generalIdx >= 0) {
            val g = units[generalIdx]
            var gox = 0f
            var goy = 0f
            if (genMode != "tengah" && finalOffsets.isNotEmpty()) {
                var minOx = Float.MAX_VALUE
                var maxOx = -Float.MAX_VALUE
                var minOy = Float.MAX_VALUE
                var maxOy = -Float.MAX_VALUE
                for (slot in finalOffsets) {
                    if (slot.ox < minOx) minOx = slot.ox
                    if (slot.ox > maxOx) maxOx = slot.ox
                    if (slot.oy < minOy) minOy = slot.oy
                    if (slot.oy > maxOy) maxOy = slot.oy
                }
                val margin = PR * 4.5f
                when (genMode) {
                    "kiri" -> {
                        gox = minOx - margin
                        goy = (minOy + maxOy) / 2f
                    }
                    "kanan" -> {
                        gox = maxOx + margin
                        goy = (minOy + maxOy) / 2f
                    }
                    "atas" -> {
                        goy = minOy - margin
                        gox = (minOx + maxOx) / 2f
                    }
                    "bawah" -> {
                        goy = maxOy + margin
                        gox = (minOx + maxOx) / 2f
                    }
                }
            }
            g.obey = true
            g.order = "move"
            g.targetIdx = null
            g.zone = null
            g.isEngaged = false
            g.isRetreating = false
            g.orbiting = null
            g.isGotoBlocked = false
            g.formation = null
            g.groupId = gid
            g.ox = 0f
            g.oy = 0f
            g.gtx = targetX + gox
            g.gty = targetY + goy
            g.baseGtx = g.gtx
            g.baseGty = g.gty
            g.formSnap = formMode
            g.genModeSnap = genMode
            g.gotoOrder = isGoto
        }

        val modeLabel = if (isGoto) "GoTo" else "Serang"
        val formLabel = if (formMode != null) " [$formMode]" else ""
        onToastMessage?.invoke("${unitIdxs.size + (if (generalIdx >= 0) 1 else 0)} bidak: Bergerak ($modeLabel)$formLabel")
        }
    }

    fun cycleGeneralMode() {
        val label = synchronized(lock) {
            genModeIdx = (genModeIdx + 1) % genModes.size
            genMode.replaceFirstChar { it.uppercase() }
        }
        onToastMessage?.invoke("Jenderal: $label")
    }

    fun setFormation(mode: String?) {
        val name = synchronized(lock) {
            if (formMode == mode && mode != null) {
                when (mode) {
                    "simplex" -> simplexOri = (simplexOri + 1) % 2
                    "duplex" -> duplexOri = (duplexOri + 1) % 2
                    "vshape" -> vshapeDir = (vshapeDir + 1) % 4
                }
            } else {
                formMode = mode
            }
            when (formMode) {
                "globus" -> "Globus"
                "simplex" -> "Acies Simplex (${if (simplexOri % 2 == 0) "H" else "V"})"
                "duplex" -> "Acies Duplex (${if (duplexOri % 2 == 0) "H" else "V"})"
                "vshape" -> "V-Shape (${listOf("Kanan", "Bawah", "Kiri", "Atas")[vshapeDir % 4]})"
                else -> "Free (Bentuk bebas)"
            }
        }
        onToastMessage?.invoke("Formasi: $name")
    }

    fun update(deltaSeconds: Float) {
        synchronized(lock) {
            if (isPaused || isGameOver) return
            val dt = min(deltaSeconds * speed, 0.075f)

        if (shake > 0f) {
            shake *= 0.88f
            shakeX = (Random.nextFloat() - 0.5f) * shake
            shakeY = (Random.nextFloat() - 0.5f) * shake
            if (shake < 0.3f) {
                shake = 0f
                shakeX = 0f
                shakeY = 0f
            }
        }

        val aliveList = mutableListOf<UnitPiece>()
        for (p in units) {
            if (p.isAlive && !p.isHidden) {
                p.isEngaged = false
                aliveList.add(p)
            }
        }

        // Formation reflow if someone died
        val byGid = aliveList.filter { it.team == "p" && it.formation != null && it.groupId != 0 }
            .groupBy { it.groupId }

        for ((gid, members) in byGid) {
            val count = members.size
            if (gidCount[gid] == count) continue
            gidCount[gid] = count
            if (count == 0) continue

            val formMd = members[0].formation ?: continue
            val kx = members[0].kxHole
            val offs = FormationHelper.formatOffsets(count, formMd, kx, simplexOri, duplexOri, vshapeDir)
            if (offs.isEmpty()) continue

            val sorted = members.sortedBy { it.slotIdx }
            for (k in sorted.indices) {
                val member = sorted[k]
                member.slotIdx = k
                val safeK = k.coerceIn(0, offs.size - 1)
                member.ox = offs[safeK].ox
                member.oy = offs[safeK].oy
                member.formAng = offs[safeK].ang
            }
        }

        // Validate targets
        for (p in aliveList) {
            val tIdx = p.targetIdx
            if (tIdx != null) {
                if (tIdx !in units.indices || !units[tIdx].isAlive) {
                    p.targetIdx = null
                    p.zone = null
                    p.isGotoBlocked = false
                }
            }
        }

        // Spatial grid for target acquisition
        val pGrid = buildGrid(aliveList, GCELL)
        val qCand = mutableListOf<Int>()

        for (p in aliveList) {
            if (p.isRetreating || p.targetIdx != null) continue
            if (p.obey && p.order == "move") continue

            var bestIdx: Int? = null
            var bestD = AR
            queryGrid(pGrid, GCELL, p.x, p.y, AR, qCand)

            for (idx in qCand) {
                val o = aliveList[idx]
                if (o.team == p.team) continue
                val d = distance(p.x, p.y, o.x, o.y)
                if (d < bestD) {
                    bestD = d
                    bestIdx = o.id
                }
            }
            if (bestIdx != null) {
                p.targetIdx = bestIdx
                p.zone = null
            }
        }

        // GoTo blocking: hit enemy blocking path
        for (p in aliveList) {
            if (p.isRetreating || p.targetIdx != null || !p.obey || p.team != "p" || p.order != "move") continue
            val destX = (p.gtx ?: p.x) + p.ox
            val destY = (p.gty ?: p.y) + p.oy
            if (distance(p.x, p.y, destX, destY) <= 4f) continue

            var bestIdx: Int? = null
            var bestD = SR * 1.4f
            queryGrid(pGrid, GCELL, p.x, p.y, bestD, qCand)
            val moveAng = angleTo(p.x, p.y, destX, destY)

            for (idx in qCand) {
                val o = aliveList[idx]
                if (o.team == p.team) continue
                val d = distance(p.x, p.y, o.x, o.y)
                if (d >= bestD) continue
                if (abs(normAngle(angleTo(p.x, p.y, o.x, o.y) - moveAng)) > FH * 0.75f) continue
                bestD = d
                bestIdx = o.id
            }
            if (bestIdx != null) {
                p.targetIdx = bestIdx
                p.zone = null
                p.isGotoBlocked = true
            }
        }

        // Threat redirect: target closest attacker targeting self
        val attackersOf = mutableMapOf<Int, MutableList<UnitPiece>>()
        for (o in aliveList) {
            val t = o.targetIdx
            if (t != null && !o.isRetreating) {
                attackersOf.getOrPut(t) { mutableListOf() }.add(o)
            }
        }

        for (p in aliveList) {
            val tIdx = p.targetIdx ?: continue
            if (p.isRetreating) continue
            val target = units.getOrNull(tIdx) ?: continue
            val curD = distance(p.x, p.y, target.x, target.y)
            val atkers = attackersOf[p.id] ?: continue

            var bestThreatIdx: Int? = null
            var bestThreatD = curD
            for (o in atkers) {
                val od = distance(p.x, p.y, o.x, o.y)
                if (od < bestThreatD) {
                    bestThreatD = od
                    bestThreatIdx = o.id
                }
            }
            if (bestThreatIdx != null) {
                p.targetIdx = bestThreatIdx
                p.zone = null
                p.orbiting = null
            }
        }

        // Backstab priority for units already in back zone
        for (p in aliveList) {
            if (p.isRetreating || p.obey) continue
            var bestEIdx: Int? = null
            var bestED = BDR
            queryGrid(pGrid, GCELL, p.x, p.y, BDR, qCand)

            for (idx in qCand) {
                val e = aliveList[idx]
                if (e.team == p.team) continue
                val d = distance(p.x, p.y, e.x, e.y)
                if (d > bestED) continue
                if (!isBackZone(zoneOf(e, p))) continue
                bestED = d
                bestEIdx = e.id
            }
            if (bestEIdx != null && p.targetIdx != bestEIdx) {
                val target = units[bestEIdx]
                p.targetIdx = bestEIdx
                p.zone = zoneOf(target, p)
                p.orbiting = false
            }
        }

        // Assign zones for multiple attackers on same target
        val groupsByTarget = mutableMapOf<Int, MutableList<UnitPiece>>()
        for (p in aliveList) {
            val t = p.targetIdx
            if (t != null && !p.isRetreating) {
                groupsByTarget.getOrPut(t) { mutableListOf() }.add(p)
            }
        }

        groupsByTarget.forEach { (tIdx, atkers) ->
            val tgt = units.getOrNull(tIdx) ?: return@forEach
            val takenFront = mutableMapOf<String, UnitPiece?>("FL" to null, "FR" to null)
            val unassigned = mutableListOf<UnitPiece>()

            for (p in atkers) {
                val z = p.zone
                if (z == "FL" || z == "FR") {
                    if (takenFront[z] == null) {
                        takenFront[z] = p
                    } else {
                        unassigned.add(p)
                    }
                } else if (z == "BL" || z == "BR") {
                    // keep back zone
                } else {
                    unassigned.add(p)
                }
            }

            unassigned.sortBy { distance(it.x, it.y, tgt.x, tgt.y) }
            for (p in unassigned) {
                val natural = zoneOf(tgt, p)
                val side = if (natural == "FL" || natural == "BL") "L" else "R"
                val frontZone = if (side == "L") "FL" else "FR"
                val otherFront = if (side == "L") "FR" else "FL"
                val backZone = if (side == "L") "BL" else "BR"

                val assigned = when {
                    takenFront[frontZone] == null -> {
                        takenFront[frontZone] = p
                        frontZone
                    }
                    takenFront[otherFront] == null -> {
                        takenFront[otherFront] = p
                        otherFront
                    }
                    else -> backZone
                }
                p.zone = assigned
                p.orbiting = null
            }
        }

        // Orbit check for flankers
        for (p in aliveList) {
            val tIdx = p.targetIdx ?: continue
            val z = p.zone ?: continue
            if (p.isRetreating) continue
            val tgt = units.getOrNull(tIdx) ?: continue

            if (isBackZone(z)) {
                if (p.orbiting != false) {
                    val curZone = zoneOf(tgt, p)
                    p.orbiting = !isBackZone(curZone)
                }
            } else {
                p.orbiting = false
            }
        }

        // Auto-face
        for (p in aliveList) {
            if (p.team == "p" && p.formation != null && p.formAng != null) {
                val destX = (p.gtx ?: p.x) + p.ox
                val destY = (p.gty ?: p.y) + p.oy
                if (distance(p.x, p.y, destX, destY) <= SR * 1.2f) {
                    val t = p.targetIdx?.let { units.getOrNull(it) }
                    val tg = if (t != null) angleTo(p.x, p.y, t.x, t.y) else p.formAng!!
                    val df = normAngle(tg - p.angle)
                    p.angle += df * min(1f, 3f * dt)
                    continue
                }
            }

            var faceTarget: Point2D? = p.targetIdx?.let { units.getOrNull(it)?.let { target -> Point2D(target.x, target.y) } }
            if (faceTarget == null && p.order == "move") {
                val destX = (p.gtx ?: p.x) + p.ox
                val destY = (p.gty ?: p.y) + p.oy
                if (distance(p.x, p.y, destX, destY) > 4f) {
                    faceTarget = Point2D(destX, destY)
                }
            }

            if (faceTarget != null) {
                val tg = angleTo(p.x, p.y, faceTarget.x, faceTarget.y)
                val df = normAngle(tg - p.angle)
                p.angle += df * min(1f, 3f * dt)
            }
        }

        for (p in aliveList) {
            if (p.role == "inf" || p.isGeneral) {
                p.targetIdx = null
                p.zone = null
                p.orbiting = null
            }
        }

        // General standby mode
        val pgen = playerGeneral
        if (pgen != null && pgen.isAlive) {
            val protectedCenter = pgen.gotoOrder || (pgen.formSnap == "globus" && pgen.genModeSnap == "tengah")
            var nd = Float.MAX_VALUE
            var threatE: UnitPiece? = null
            for (pe in aliveList) {
                if (pe.team == "e") {
                    val d = distance(pe.x, pe.y, pgen.x, pgen.y)
                    if (d < nd) {
                        nd = d
                        threatE = pe
                    }
                }
            }

            val standbyR = AR * 1.5f
            if (!protectedCenter && threatE != null && nd < standbyR) {
                var sx = 0f
                var sy = 0f
                var sn = 0
                for (pe in aliveList) {
                    if (pe.team == "p" && !pe.isGeneral && distance(pe.x, pe.y, pgen.x, pgen.y) < 600f) {
                        sx += pe.x
                        sy += pe.y
                        sn++
                    }
                }
                if (sn > 0) {
                    val cx = sx / sn
                    val cy = sy / sn
                    val dxh = cx - threatE.x
                    val dyh = cy - threatE.y
                    val dlh = max(0.001f, sqrt(dxh * dxh + dyh * dyh))
                    pgen.obey = true
                    pgen.order = "move"
                    pgen.formation = null
                    pgen.groupId = 0
                    pgen.ox = 0f
                    pgen.oy = 0f
                    pgen.gtx = cx + dxh / dlh * 120f
                    pgen.gty = cy + dyh / dlh * 120f
                }
            } else if (pgen.baseGtx != null) {
                pgen.obey = true
                pgen.order = "move"
                pgen.gtx = pgen.baseGtx
                pgen.gty = pgen.baseGty
            }
        }

        // Enemy Commander AI & Informant logic
        updateEnemyCommanderAI(dt, aliveList)
        updateHazardZones(dt, aliveList)

        // Movement step
        for (p in aliveList) {
            var dx = 0f
            var dy = 0f
            val sp = MS * speedModifier(p.x, p.y) * (if (p.isRunner) 1.25f else 1f)

            if (p.isRetreating) {
                dx = -cos(p.angle) * sp
                dy = -sin(p.angle) * sp
                p.retreatTimer -= dt
                if (p.retreatTimer <= 0f) {
                    p.isRetreating = false
                }
            } else if (isAnchored(p)) {
                dx = 0f
                dy = 0f
            } else if (p.targetIdx != null && p.zone != null && !p.isGotoBlocked) {
                val tgt = units.getOrNull(p.targetIdx!!)
                if (tgt != null) {
                    if (p.orbiting == true) {
                        val curA = angleTo(tgt.x, tgt.y, p.x, p.y)
                        val curD = distance(p.x, p.y, tgt.x, tgt.y)
                        val angStep = p.orbitDir * 1.4f * dt * min(1f, ORB / max(curD, 1f))
                        val seekA = curA + angStep
                        val seekX = (tgt.x + cos(seekA) * ORB).coerceIn(PR, BW - PR)
                        val seekY = (tgt.y + sin(seekA) * ORB).coerceIn(PR, BH - PR)
                        val a2 = angleTo(p.x, p.y, seekX, seekY)
                        dx = cos(a2) * sp
                        dy = sin(a2) * sp
                    } else {
                        val d = distance(p.x, p.y, tgt.x, tgt.y)
                        if (d > SR * 0.85f) {
                            val a2 = angleTo(p.x, p.y, tgt.x, tgt.y)
                            dx = cos(a2) * sp
                            dy = sin(a2) * sp
                        }
                    }
                }
            } else if (p.isGotoBlocked && p.order == "move") {
                val destX = (p.gtx ?: p.x) + p.ox
                val destY = (p.gty ?: p.y) + p.oy
                val dd = distance(p.x, p.y, destX, destY)
                if (dd > 4f) {
                    val a2 = angleTo(p.x, p.y, destX, destY)
                    dx = cos(a2) * sp
                    dy = sin(a2) * sp
                }
            } else if ((p.team == "p" || p.isCommander) && p.order == "move") {
                val destX = (p.gtx ?: p.x) + p.ox
                val destY = (p.gty ?: p.y) + p.oy
                val dd = distance(p.x, p.y, destX, destY)
                if (dd > 4f) {
                    val a2 = angleTo(p.x, p.y, destX, destY)
                    dx = cos(a2) * sp
                    dy = sin(a2) * sp
                } else {
                    p.order = null
                }
            } else if (p.team == "e" && p.territoryIndex >= 0 && !p.isCommander) {
                val te = territories[p.territoryIndex]
                val d2 = distance(p.x, p.y, te.cx, te.cy)
                if (d2 > te.radius * 0.65f) {
                    val a2 = angleTo(p.x, p.y, te.cx, te.cy)
                    dx = cos(a2) * sp * 0.5f
                    dy = sin(a2) * sp * 0.5f
                }
            }

            p.x = (p.x + dx * dt).coerceIn(PR, BW - PR)
            p.y = (p.y + dy * dt).coerceIn(PR, BH - PR)
        }

        // Collision push resolution
        val cGrid = buildGrid(aliveList, CCELL)
        val cCand = mutableListOf<Int>()
        val colr2 = COLR * COLR

        for (i in aliveList.indices) {
            val p = aliveList[i]
            queryGrid(cGrid, CCELL, p.x, p.y, COLR, cCand)
            for (j in cCand) {
                if (j <= i) continue
                val o = aliveList[j]
                val vx = p.x - o.x
                val vy = p.y - o.y
                val d2 = vx * vx + vy * vy
                if (d2 >= colr2 || d2 < 0.0001f) continue
                val d = sqrt(d2)
                val overlap = COLR - d
                val ux = vx / d
                val uy = vy / d
                val wobble = overlap * 0.12f
                val tx = if (i % 2 == 0) -uy else uy
                val ty = if (i % 2 == 0) ux else -ux

                val pushX = ux * overlap * 0.5f + tx * wobble
                val pushY = uy * overlap * 0.5f + ty * wobble
                p.x = (p.x + pushX).coerceIn(PR, BW - PR)
                p.y = (p.y + pushY).coerceIn(PR, BH - PR)
                o.x = (o.x - pushX).coerceIn(PR, BW - PR)
                o.y = (o.y - pushY).coerceIn(PR, BH - PR)
            }
        }

        // Early retreat trigger if 2+ enemies attacking front
        for (tgt in aliveList) {
            if (!tgt.isAlive || tgt.isRetreating) continue
            val atkers = groupsByTarget[tgt.id] ?: continue
            var frontCount = 0
            for (p in atkers) {
                if (p.isRetreating || p.zone == null || p.orbiting == true) continue
                if (!isFrontZone(p.zone!!)) continue
                if (distance(p.x, p.y, tgt.x, tgt.y) > RSR) continue
                if (!isInFrontCone(p, tgt)) continue
                frontCount++
            }
            if (frontCount >= 2) {
                tgt.isRetreating = true
                tgt.retreatTimer = 0.6f
                tgt.targetIdx = null
                tgt.zone = null
            }
        }

        // Combat damage & Instant kill
        data class AttackEntry(val front: MutableList<UnitPiece>, val back: MutableList<UnitPiece>)
        val dmgMap = mutableMapOf<Int, AttackEntry>()

        for (p in aliveList) {
            if (!p.isAlive || p.isGeneral || p.isRetreating || p.targetIdx == null || p.zone == null || p.orbiting == true) continue
            val tgt = units.getOrNull(p.targetIdx!!) ?: continue
            val d = distance(p.x, p.y, tgt.x, tgt.y)
            if (d > SR) continue
            if (!isInFrontCone(p, tgt)) continue

            val entry = dmgMap.getOrPut(tgt.id) { AttackEntry(mutableListOf(), mutableListOf()) }
            if (isFrontZone(p.zone!!)) entry.front.add(p) else entry.back.add(p)
        }

        dmgMap.forEach { (tIdx, entry) ->
            val tgt = units.getOrNull(tIdx) ?: return@forEach
            if (tgt.isRetreating) return@forEach
            tgt.isEngaged = true

            // Instant kill if attacked simultaneously from front AND back (non-general)
            if (entry.front.isNotEmpty() && entry.back.isNotEmpty() && !tgt.isGeneral) {
                tgt.hp = 0f
                tgt.isAlive = false
                spawnParticles(tgt.x, tgt.y, "d")
                shake = 6f
                return@forEach
            }

            // 2+ front attacks force retreat
            if (entry.front.size >= 2 && !tgt.isGeneral) {
                tgt.isRetreating = true
                tgt.retreatTimer = 0.6f
                tgt.targetIdx = null
                tgt.zone = null
                return@forEach
            }

            val totalAtkers = entry.front.size + entry.back.size
            val mult = if (tgt.isGeneral) {
                if (entry.front.isNotEmpty() && entry.back.isNotEmpty()) 1.2f else 0.6f
            } else 1f

            tgt.hp -= DR * dt * totalAtkers * mult
            tgt.hitFlash = 0.12f

            if (tgt.hp <= 0f) {
                tgt.isAlive = false
                spawnParticles(tgt.x, tgt.y, "d")
                shake = 4f
            }
        }

        // Flank coordination hold-back
        for (tgt in aliveList) {
            if (!tgt.isAlive) continue
            val atkers = groupsByTarget[tgt.id] ?: continue
            var flankerNotDone = false
            for (f in atkers) {
                if (f.zone?.let { isBackZone(it) } == true && f.orbiting == true) {
                    flankerNotDone = true
                    break
                }
            }
            for (p in atkers) {
                if (p.zone?.let { isFrontZone(it) } != true) continue
                if (p.obey) {
                    p.holdBack = false
                    continue
                }
                if (flankerNotDone && (p.holdBack || tgt.isRetreating)) {
                    p.holdBack = true
                    p.isRetreating = true
                    p.retreatTimer = max(p.retreatTimer, 0.2f)
                } else if (!flankerNotDone && p.holdBack) {
                    p.holdBack = false
                    p.isRetreating = false
                }
            }
        }

        // Hit flash decay
        for (p in aliveList) {
            if (p.hitFlash > 0f) p.hitFlash -= dt
        }

        // Particles update
        for (i in particles.indices.reversed()) {
            val pt = particles[i]
            pt.x += pt.vx * dt
            pt.y += pt.vy * dt
            pt.vx *= 0.94f
            pt.vy *= 0.94f
            pt.life -= dt
            if (pt.life <= 0f) {
                particles.removeAt(i)
            }
        }

        // Periodic territory and victory checks
        wilTimer += dt
        if (wilTimer >= 0.25f) {
            wilTimer = 0f
            updateTerritories()
        }

        // General warning & defeat checks
        if (pgen != null) {
            pgen.isRetreating = false
            if (pgen.hp < pgen.maxHp * 0.4f && !pgen.warn && pgen.isAlive) {
                pgen.warn = true
                onToastMessage?.invoke("JENDERAL TERANCAM!")
            } else if (pgen.hp > pgen.maxHp * 0.6f) {
                pgen.warn = false
            }
        }

        checkVictoryDefeat()
        }
    }

    private fun updateEnemyCommanderAI(dt: Float, aliveList: List<UnitPiece>) {
        val playerList = aliveList.filter { it.team == "p" && it.isAlive }
        if (playerList.isEmpty()) return

        for (t in territories.indices) {
            val te = territories[t]
            if (te.isRevealed) te.everRevealed = true
            if (te.team == "p" || !te.isRevealed) continue

            val gen = units.find { it.isAlive && it.territoryIndex == t && it.isGeneral } ?: continue
            val members = aliveList.filter { it.team == "e" && it.territoryIndex == t && it.isAlive && !it.isHidden && !it.isGeneral }

            te.cmdTimer += dt
            if (te.cmdTimer < 0.3f) continue
            te.cmdTimer = 0f

            if (te.state != "lead") {
                var spot: UnitPiece? = null
                val allE = members + gen
                for (e in allE) {
                    for (p in playerList) {
                        if (distance(e.x, e.y, p.x, p.y) < AR * 1.15f) {
                            spot = e
                            break
                        }
                    }
                    if (spot != null) break
                }
                if (spot != null) {
                    if (spot.isGeneral) {
                        startEnemyLead(te, t)
                    } else if (te.runnerIdx == null) {
                        val runner = members.find { it.role == "inf" } ?: spot
                        te.runnerIdx = runner.id
                        runner.isRunner = true
                        runner.isCommander = true
                        runner.obey = true
                    }
                }
                val rId = te.runnerIdx
                if (rId != null) {
                    val r = units.find { it.id == rId }
                    if (r != null && r.isAlive) {
                        r.order = "move"
                        r.gtx = gen.x
                        r.gty = gen.y
                        r.ox = 0f
                        r.oy = 0f
                        if (distance(r.x, r.y, gen.x, gen.y) < PR * 3.6f) {
                            startEnemyLead(te, t)
                        }
                    }
                }
                if (te.state != "lead") continue
            }

            if (members.isEmpty()) continue

            var ex = 0f
            var ey = 0f
            for (m in members) {
                ex += m.x
                ey += m.y
            }
            ex /= members.size
            ey /= members.size

            var closestPlayer: UnitPiece? = null
            var minD = Float.MAX_VALUE
            for (p in playerList) {
                val d = distance(p.x, p.y, ex, ey)
                if (d < minD) {
                    minD = d
                    closestPlayer = p
                }
            }
            val target = closestPlayer ?: continue
            val dx = target.x - ex
            val dy = target.y - ey
            val dist = max(1f, sqrt(dx * dx + dy * dy))
            val ux = dx / dist
            val uy = dy / dist
            val ang = atan2(dy, dx)

            var dom = te.dominantAxis
            if (dom == null || (dom == "x" && abs(dy) > abs(dx) * 1.3f) || (dom == "y" && abs(dx) > abs(dy) * 1.3f)) {
                dom = if (abs(dx) >= abs(dy)) "x" else "y"
            }
            te.dominantAxis = dom

            val vd = if (dom == "x") (if (dx >= 0) 0 else 2) else (if (dy >= 0) 1 else 3)
            val mode = te.formation
            val key = "$mode$dom$vd${members.size}"

            if (te.formationKey != key) {
                te.formationKey = key
                val sOri = if (dom == "x") 1 else 0
                val dOri = sOri
                val offs = FormationHelper.formatOffsets(members.size, mode, 0, sOri, dOri, vd)
                if (offs.isNotEmpty()) {
                    val sorted = members.sortedBy { FormationHelper.spatialSortKey(it, ex, ey, mode, sOri, dOri) }
                    for (k in sorted.indices) {
                        sorted[k].slotIdx = k
                        val safeK = k.coerceIn(0, offs.size - 1)
                        sorted[k].ox = offs[safeK].ox
                        sorted[k].oy = offs[safeK].oy
                        sorted[k].formAng = offs[safeK].ang
                    }
                }
            }

            val stand = PR * 2.4f * (sqrt(members.size.toDouble()).toFloat() * 0.6f + 1f)
            val mv = max(0f, min(70f, dist - stand))
            val fx = ex + ux * mv
            val fy = ey + uy * mv
            val bh = max(50f, PR * 2.4f * (sqrt(members.size.toDouble()).toFloat() / 2f + 2f))

            for (m in members) {
                m.isCommander = true
                m.obey = false
                m.formation = mode
                m.groupId = 0
                m.order = "move"
                m.role = "atk"
                m.gtx = fx
                m.gty = fy
                if (mode != "globus") m.formAng = ang
            }

            gen.isCommander = true
            gen.obey = true
            gen.order = "move"
            gen.formation = null
            gen.ox = 0f
            gen.oy = 0f
            val bk = if (mode == "globus") 0f else bh
            gen.gtx = fx - ux * bk
            gen.gty = fy - uy * bk
        }
    }

    private fun startEnemyLead(te: Territory, territoryIdx: Int) {
        te.state = "lead"
        te.runnerIdx = null
        for (q in units) {
            if (q.team == "e" && q.territoryIndex == territoryIdx && q.isAlive) {
                q.role = "atk"
                q.isCommander = true
            }
        }
        val formName = when (te.formation) {
            "globus" -> "Globus"
            "simplex" -> "Acies Simplex"
            "duplex" -> "Acies Duplex"
            "vshape" -> "V-Shape"
            else -> te.formation
        }
        onToastMessage?.invoke("Jenderal ${te.ruler} dari ${te.name} memimpin pasukan ($formName)!")
    }

    private fun updateHazardZones(dt: Float, aliveList: List<UnitPiece>) {
        hazardTimer += dt
        if (hazardTimer < 0.25f) return
        val d = hazardTimer
        hazardTimer = 0f

        for (z in hazardZones) {
            if (z.type == "lumpur") continue
            val r2 = z.radius * z.radius
            for (p in aliveList) {
                val dx = p.x - z.x
                val dy = p.y - z.y
                if (dx * dx + dy * dy >= r2) continue
                if (z.type == "api") {
                    p.hp -= 10f * d
                    p.hitFlash = 0.15f
                    if (p.hp <= 0f) {
                        p.hp = 0f
                        p.isAlive = false
                        spawnParticles(p.x, p.y, "d")
                    }
                } else if (z.type == "air") {
                    val mh = p.maxHp
                    if (p.hp < mh) {
                        p.hp = min(mh, p.hp + 8f * d)
                    }
                }
            }
        }
    }

    private fun updateTerritories() {
        var territoryCountChanged = false
        for (t in territories.indices) {
            val te = territories[t]
            if (te.team == "p") continue

            var enemyIn = 0
            var playerIn = 0

            for (p in units) {
                if (!p.isAlive) continue
                if (te.containsPoint(p.x, p.y)) {
                    if (p.team == "e" && p.territoryIndex == t) enemyIn++
                    else if (p.team == "p") playerIn++
                }
            }

            var justRevealed = false
            if (playerIn > 0 && !te.isRevealed) {
                te.isRevealed = true
                justRevealed = true
                for (p in units) {
                    if (p.team == "e" && p.territoryIndex == t && p.isAlive) {
                        p.isHidden = false
                    }
                }
            } else if (playerIn == 0 && te.isRevealed && te.state != "lead") {
                te.isRevealed = false
                for (p in units) {
                    if (p.team == "e" && p.territoryIndex == t && p.isAlive) {
                        p.isHidden = true
                    }
                }
            }

            val genAlive = units.any { it.isAlive && it.territoryIndex == t && it.isGeneral }
            if (!justRevealed && enemyIn == 0 && playerIn > 0 && !genAlive) {
                te.team = "p"
                territoryCountChanged = true
                onToastMessage?.invoke("Wilayah \"${te.name}\" berhasil ditaklukkan!")
            }

            // General defeated: convert surviving soldiers to ally
            if (!genAlive && !te.converted) {
                te.converted = true
                var nc = 0
                for (p in units) {
                    if (p.team == "e" && p.territoryIndex == t && p.isAlive) {
                        p.team = "p"
                        p.territoryIndex = -1
                        p.isHidden = false
                        p.targetIdx = null
                        p.zone = null
                        p.order = null
                        p.obey = false
                        p.formation = null
                        p.groupId = 0
                        p.isRetreating = false
                        p.holdBack = false
                        p.orbiting = null
                        p.isCommander = false
                        p.role = "atk"
                        p.isRunner = false
                        p.hitFlash = 0.4f
                        nc++
                    }
                }
                for (p in units) {
                    if (p.targetIdx != null && units.getOrNull(p.targetIdx!!)?.team == p.team) {
                        p.targetIdx = null
                        p.zone = null
                        p.orbiting = null
                    }
                }
                if (nc > 0) {
                    onToastMessage?.invoke("Jenderal ${te.ruler} gugur! $nc prajurit bergabung menjadi sekutu!")
                }
            }
        }
        if (territoryCountChanged) {
            onTerritoryStatusChanged?.invoke()
        }
    }

    private fun checkVictoryDefeat() {
        if (isGameOver) return
        val pgen = playerGeneral
        val playerAlive = units.count { it.isAlive && it.team == "p" }
        val enemyTotalAlive = units.count { it.isAlive && it.team == "e" }

        if (playerAlive == 0 || (pgen != null && !pgen.isAlive)) {
            isGameOver = true
            isVictory = false
            gameOverMessage = if (pgen != null && !pgen.isAlive) "KEKALAHAN! Jenderal telah gugur." else "KEKALAHAN! Seluruh pasukan sekutu gugur."
            onToastMessage?.invoke(gameOverMessage)
        } else if (enemyTotalAlive == 0) {
            isGameOver = true
            isVictory = true
            gameOverMessage = "KEMENANGAN MUTLAK! Seluruh kerajaan telah dipersatukan!"
            onToastMessage?.invoke(gameOverMessage)
        }
    }

    private fun spawnParticles(x: Float, y: Float, type: String) {
        val count = if (type == "d") 10 else 4
        for (i in 0 until count) {
            val a = (Random.nextFloat() * PI * 2).toFloat()
            val s = if (type == "d") 30f + Random.nextFloat() * 50f else 15f + Random.nextFloat() * 25f
            val life = 0.3f + Random.nextFloat() * 0.3f
            val color = if (type == "d") {
                if (Random.nextBoolean()) 0xFFFF4444 else 0xFFFF8833
            } else 0xFFFFDD66
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(a) * s,
                    vy = sin(a) * s,
                    life = life,
                    maxLife = life,
                    size = if (type == "d") 2.5f + Random.nextFloat() * 2.5f else 1.5f,
                    colorHex = color
                )
            )
        }
    }

    fun speedModifier(x: Float, y: Float): Float {
        var sm = if (WaterGrid.isWater(x, y)) WaterGrid.WATER_SPEED_MULTIPLIER else 1f
        synchronized(lock) {
            for (z in hazardZones) {
                if (z.type == "lumpur") {
                    val dx = x - z.x
                    val dy = y - z.y
                    if (dx * dx + dy * dy < z.radius * z.radius) {
                        sm *= 0.55f
                        break
                    }
                }
            }
        }
        return sm
    }

    private fun isAnchored(p: UnitPiece): Boolean {
        if (p.team != "p" && !p.isCommander) return false
        if (p.formation != null && p.formAng != null) {
            val dX = (p.gtx ?: p.x) + p.ox
            val dY = (p.gty ?: p.y) + p.oy
            if (distance(p.x, p.y, dX, dY) <= SR * 1.2f) return true
        }
        if (p.obey && p.order != "move") return true
        return false
    }

    private fun zoneOf(tgt: UnitPiece, p: UnitPiece): String {
        val rel = normAngle(angleTo(tgt.x, tgt.y, p.x, p.y) - tgt.angle)
        val front = abs(rel) < (PI / 2).toFloat()
        val left = rel < 0f
        return if (front) {
            if (left) "FL" else "FR"
        } else {
            if (left) "BL" else "BR"
        }
    }

    private fun isFrontZone(z: String): Boolean = z == "FL" || z == "FR"
    private fun isBackZone(z: String): Boolean = z == "BL" || z == "BR"

    private fun isInFrontCone(p: UnitPiece, o: UnitPiece): Boolean {
        val ang = angleTo(p.x, p.y, o.x, o.y)
        return abs(normAngle(ang - p.angle)) < FH
    }

    private fun normAngle(a: Float): Float {
        var angle = a
        while (angle > PI) angle -= (PI * 2).toFloat()
        while (angle < -PI) angle += (PI * 2).toFloat()
        return angle
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    private fun angleTo(fromX: Float, fromY: Float, toX: Float, toY: Float): Float {
        return atan2(toY - fromY, toX - fromX)
    }

    private fun buildGrid(list: List<UnitPiece>, cellSize: Float): Map<Long, List<Int>> {
        val grid = mutableMapOf<Long, MutableList<Int>>()
        for (i in list.indices) {
            val p = list[i]
            val key = ((p.x / cellSize).toInt().toLong() * 100000L) + (p.y / cellSize).toInt().toLong()
            grid.getOrPut(key) { mutableListOf() }.add(i)
        }
        return grid
    }

    private fun queryGrid(
        grid: Map<Long, List<Int>>,
        cellSize: Float,
        x: Float,
        y: Float,
        radius: Float,
        out: MutableList<Int>
    ) {
        out.clear()
        val cr = max(1, ceil(radius / cellSize).toInt())
        val ccx = (x / cellSize).toInt().toLong()
        val ccy = (y / cellSize).toInt().toLong()

        for (dx in -cr..cr) {
            val bx = (ccx + dx) * 100000L
            for (dy in -cr..cr) {
                val list = grid[bx + (ccy + dy)]
                if (list != null) {
                    out.addAll(list)
                }
            }
        }
    }

    private fun initTerritories() {
        territories.clear()
        territories.addAll(TerritoryCoordinates.createTerritories())
    }
}
