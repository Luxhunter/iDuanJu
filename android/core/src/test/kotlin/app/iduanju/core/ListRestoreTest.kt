package app.iduanju.core

import app.iduanju.core.restore.CategorySnapshot
import app.iduanju.core.restore.CategorySnapshotCodec
import app.iduanju.core.restore.FeedRules
import app.iduanju.core.restore.StringMapCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ListRestoreTest {
    @Test fun stringMapRoundTrip() {
        val raw = StringMapCodec.encode(mapOf("红果" to "2", "plugin:a" to "0"))
        assertEquals(mapOf("红果" to "2", "plugin:a" to "0"), StringMapCodec.decode(raw))
        assertEquals(emptyMap<String, String>(), StringMapCodec.decode(null))
        assertEquals(emptyMap<String, String>(), StringMapCodec.decode("{"))
    }

    @Test fun categorySnapshotRoundTrip() {
        val s = CategorySnapshot(
            filterMode = true,
            genre = "comic",
            sub = "new",
            selected = mapOf("genre" to "12", "year" to "2024"),
            names = mapOf("genre" to "漫剧"),
            scrollIndex = 14,
            scrollOffset = 36,
        )
        assertEquals(s, CategorySnapshotCodec.decode(CategorySnapshotCodec.encode(s)))
        assertNull(CategorySnapshotCodec.decode("not-json"))
    }

    @Test fun shorterDefaultChipListDoesNotReplaceDiscoveredCategories() {
        val current = listOf("" to "推荐", "a" to "动作", "b" to "喜剧")
        val adopted = FeedRules.adopt(current, listOf("" to "推荐"), sel = 2)
        assertFalse(adopted.changed)
        assertEquals(current, adopted.cats)
        assertEquals(2, adopted.sel)
    }

    @Test fun longerCategoryListIsAdoptedWithoutResettingSelection() {
        val adopted = FeedRules.adopt(listOf("" to "最新"), listOf("" to "最新", "3" to "电影", "4" to "剧集"), sel = 0)
        assertEquals(listOf("" to "最新", "3" to "电影", "4" to "剧集"), adopted.cats)
        assertEquals(0, adopted.sel)
    }

    @Test fun sameSizeListWithDifferentIdsReplacesAndClampsSelection() {
        val adopted = FeedRules.adopt(
            listOf("" to "最新", "1" to "A", "2" to "B"),
            listOf("" to "最新", "9" to "其他", "8" to "纪录"),
            sel = 2,
        )
        assertEquals(2, adopted.sel)
        assertEquals("8", adopted.cats[2].first)
    }
}
