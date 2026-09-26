package com.example

import com.example.engine.GameEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.concurrent.thread

class GameEngineTest {

    @Test
    fun testEngineInitAndUpdate() {
        val engine = GameEngine()
        assertNotNull(engine.playerGeneral)
        assertEquals(50, engine.units.count { it.team == "p" })

        // Run 60 frames of update
        for (i in 0 until 60) {
            engine.update(0.016f)
        }

        // Test resetGame
        engine.resetGame("Jenderal Test", 1)
        assertNotNull(engine.playerGeneral)
        assertEquals(50, engine.units.count { it.team == "p" })

        for (i in 0 until 60) {
            engine.update(0.016f)
        }
    }

    @Test
    fun testConcurrentUpdateAndReset() {
        val engine = GameEngine()
        var running = true
        var exceptionOccurred: Throwable? = null

        val updater = thread(start = true) {
            try {
                while (running) {
                    engine.update(0.016f)
                    Thread.sleep(2)
                }
            } catch (t: Throwable) {
                exceptionOccurred = t
            }
        }

        val readerAndResetter = thread(start = true) {
            try {
                for (i in 0 until 50) {
                    val alive = engine.getAliveUnitsSnapshot()
                    val all = engine.getAllUnitsSnapshot()
                    val terr = engine.getTerritoriesSnapshot()
                    val parts = engine.getParticlesSnapshot()
                    assertTrue(alive.size <= all.size)
                    assertTrue(terr.isNotEmpty())

                    engine.selectInBounds(1000f, 1000f, 3000f, 3000f)
                    engine.orderMove(2000f, 2000f)

                    if (i % 10 == 0) {
                        engine.resetGame("Jenderal Solo", 1)
                    }
                    Thread.sleep(5)
                }
            } catch (t: Throwable) {
                exceptionOccurred = t
            }
        }

        readerAndResetter.join(10000)
        running = false
        updater.join(5000)

        assertNull("No exception should occur during concurrent game operations", exceptionOccurred)
    }
}
