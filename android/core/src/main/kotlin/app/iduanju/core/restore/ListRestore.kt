package app.iduanju.core.restore

import org.json.JSONObject

/** String map stored in a navigation SavedStateHandle. Values are JSON strings. */
object StringMapCodec {
    fun encode(map: Map<String, String>): String = JSONObject(map).toString()

    fun decode(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val o = JSONObject(raw)
            o.keys().asSequence().associateWith { o.optString(it) }
        }.getOrDefault(emptyMap())
    }
}

/**
 * Category / rank screen fields that must survive process death.
 * The in-memory row list stays on the ViewModel; this snapshot is the part SavedStateHandle can keep.
 */
data class CategorySnapshot(
    val filterMode: Boolean,
    val genre: String,
    val sub: String,
    val selected: Map<String, String>,
    val names: Map<String, String>,
    val scrollIndex: Int,
    val scrollOffset: Int,
)

object CategorySnapshotCodec {
    fun encode(s: CategorySnapshot): String = JSONObject()
        .put("filter", s.filterMode)
        .put("genre", s.genre)
        .put("sub", s.sub)
        .put("selected", JSONObject(s.selected))
        .put("names", JSONObject(s.names))
        .put("index", s.scrollIndex.coerceAtLeast(0))
        .put("offset", s.scrollOffset.coerceAtLeast(0))
        .toString()

    fun decode(raw: String?): CategorySnapshot? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val o = JSONObject(raw)
            CategorySnapshot(
                filterMode = o.optBoolean("filter"),
                genre = o.optString("genre"),
                sub = o.optString("sub"),
                selected = StringMapCodec.decode(o.optJSONObject("selected")?.toString()),
                names = StringMapCodec.decode(o.optJSONObject("names")?.toString()),
                scrollIndex = o.optInt("index").coerceAtLeast(0),
                scrollOffset = o.optInt("offset").coerceAtLeast(0),
            )
        }.getOrNull()
    }
}

data class AdoptedCats(val cats: List<Pair<String, String>>, val sel: Int, val changed: Boolean)

/**
 * Home / TVBox category chips. A recomposed screen often passes only the default chip
 * ("最新" / "推荐"); a longer list discovered earlier must win, and the selection must not jump back to 0.
 */
object FeedRules {
    fun adopt(current: List<Pair<String, String>>, incoming: List<Pair<String, String>>, sel: Int): AdoptedCats {
        if (incoming.isEmpty()) return AdoptedCats(current, sel, false)
        val replace = current.isEmpty() || incoming.size > current.size ||
            (incoming.size == current.size && incoming.map { it.first } != current.map { it.first })
        if (!replace) return AdoptedCats(current, sel.coerceIn(0, (current.size - 1).coerceAtLeast(0)), false)
        val nextSel = sel.coerceIn(0, incoming.lastIndex)
        return AdoptedCats(incoming, nextSel, true)
    }
}
