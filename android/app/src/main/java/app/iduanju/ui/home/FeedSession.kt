package app.iduanju.ui.home

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import app.iduanju.core.hongguo.HGSeries
import app.iduanju.core.restore.FeedRules
import app.iduanju.core.util.VerificationRequiredException

/**
 * One paged category grid (采集站 / 插件 / 黄果). Scoped to the home back stack entry and keyed by source,
 * so leaving for detail or the player does not reset the chip, page cursor, rows, or scroll.
 */
class FeedSession : ViewModel() {
    var sel by mutableIntStateOf(0)
    var gen by mutableIntStateOf(0)
    val items: SnapshotStateList<HGSeries> = androidx.compose.runtime.mutableStateListOf()
    var page by mutableIntStateOf(1)
    var hasMore by mutableStateOf(true)
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var verify by mutableStateOf<VerificationRequiredException?>(null)
    var cats by mutableStateOf<List<Pair<String, String>>>(emptyList())
    val grid = LazyGridState()

    fun adopt(incoming: List<Pair<String, String>>) {
        val adopted = FeedRules.adopt(cats, incoming, sel)
        if (!adopted.changed && adopted.sel == sel) return
        cats = adopted.cats
        if (adopted.sel != sel) {
            sel = adopted.sel
            gen++
            resetPages()
        }
    }

    fun select(index: Int) {
        if (index == sel) return
        sel = index
        gen++
        resetPages()
    }

    fun resetPages() {
        items.clear()
        page = 1
        hasMore = true
        error = null
        verify = null
        loading = false
    }
}
