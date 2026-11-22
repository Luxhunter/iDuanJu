package app.iduanju.core

import app.iduanju.core.player.PlaybackFileGuard
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackFileGuardTest {
    @Test fun skipsTheFileJustWrittenAndAnythingStillPlaying() {
        val guard = PlaybackFileGuard()
        guard.retain("/cache/playing.mp4")
        val victims = guard.pickVictims(
            filesOldestFirst = listOf(
                "/cache/old.mp4" to 400L,
                "/cache/playing.mp4" to 500L,
                "/cache/mid.mp4" to 400L,
            ),
            keep = "/cache/next.mp4",
            keepBytes = 400L,
            budget = 600L,
        )
        assertEquals(listOf("/cache/old.mp4", "/cache/mid.mp4"), victims)
    }

    @Test fun stopsOnceTheBudgetIsMet() {
        val victims = PlaybackFileGuard().pickVictims(
            filesOldestFirst = listOf("/cache/a" to 100L, "/cache/b" to 100L),
            keep = "/cache/keep",
            keepBytes = 100L,
            budget = 250L,
        )
        assertEquals(listOf("/cache/a"), victims)
    }

    @Test fun releaseAllowsALaterTrimToDeleteTheFile() {
        val guard = PlaybackFileGuard()
        guard.retain("/cache/playing.mp4")
        guard.release("/cache/playing.mp4")
        val victims = guard.pickVictims(
            listOf("/cache/playing.mp4" to 900L),
            keep = "/cache/other.mp4",
            keepBytes = 10L,
            budget = 100L,
        )
        assertEquals(listOf("/cache/playing.mp4"), victims)
    }
}
