package app.iduanju.core.player

/**
 * Chooses cache files that may be deleted. [keep] is the file just written for playback.
 * [retained] are paths a player is still using (including the previous episode while the next one is precached).
 * Files are oldest-first. Deletion continues while the running total is at or above [budget], matching the
 * previous cache trim, but never removes a retained or just-written file.
 */
class PlaybackFileGuard {
    private val retained = HashSet<String>()

    fun retain(path: String) {
        if (path.isNotEmpty()) synchronized(this) { retained.add(path) }
    }

    fun release(path: String) {
        synchronized(this) { retained.remove(path) }
    }

    fun pickVictims(
        filesOldestFirst: List<Pair<String, Long>>,
        keep: String,
        keepBytes: Long,
        budget: Long,
    ): List<String> = synchronized(this) {
        var total = filesOldestFirst.sumOf { it.second } + keepBytes
        val victims = ArrayList<String>()
        for ((path, size) in filesOldestFirst) {
            if (total < budget) break
            if (path == keep || path in retained) continue
            victims += path
            total -= size
        }
        victims
    }
}
