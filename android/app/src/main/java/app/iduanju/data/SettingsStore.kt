package app.iduanju.data

import android.content.Context

/** Public hygiene: optional player device override "did,iid" — never hard-coded. */
class SettingsStore(ctx: Context) {
    private val sp = ctx.getSharedPreferences("idj", Context.MODE_PRIVATE)
    private val secret = ctx.getSharedPreferences("idj-sensitive", Context.MODE_PRIVATE)

    fun playerDeviceOverride(): Pair<String, String>? {
        migrateDevice()
        val s = secret.getString(DEVICE_KEY, null) ?: return null
        val p = s.split(",", limit = 2).map { it.trim() }
        return if (p.size == 2 && p[0].isNotEmpty() && p[1].isNotEmpty()) p[0] to p[1] else null
    }

    fun setPlayerDeviceOverride(did: String?, iid: String?) {
        migrateDevice()
        if (did.isNullOrBlank() || iid.isNullOrBlank()) secret.edit().remove(DEVICE_KEY).apply()
        else secret.edit().putString(DEVICE_KEY, "$did,$iid").apply()
        sp.edit().remove(DEVICE_KEY).apply()
    }

    private fun migrateDevice() {
        val old = sp.getString(DEVICE_KEY, null) ?: return
        if (secret.getString(DEVICE_KEY, null) == null) secret.edit().putString(DEVICE_KEY, old).apply()
        sp.edit().remove(DEVICE_KEY).apply()
    }

    private companion object { const val DEVICE_KEY = "hg.purePlayerDevice" }

    /** Developer option: let the player/media download follow the system proxy (default off = iOS parity). */
    var playerUsesSystemProxy: Boolean
        get() = sp.getBoolean("dev.playerSystemProxy", false)
        set(v) { sp.edit().putBoolean("dev.playerSystemProxy", v).apply(); app.iduanju.core.hongguo.HGNet.playerUsesSystemProxy = v }

    var adStripEnabled: Boolean
        get() = sp.getBoolean("stv.hls.adstrip", sp.getBoolean("adStrip", true))
        set(v) { sp.edit().putBoolean("stv.hls.adstrip", v).apply() }

    var prefetchEnabled: Boolean
        get() = sp.getBoolean("prefetchEnabled", true)
        set(v) { sp.edit().putBoolean("prefetchEnabled", v).apply() }

    var playbackCacheMB: Int
        get() = sp.getInt("playbackCacheMB", 600).coerceIn(100, 2000)
        set(v) { sp.edit().putInt("playbackCacheMB", v.coerceIn(100, 2000)).apply() }

    var danmakuArea: Float
        get() = sp.getFloat("danmakuArea", 1f / 3f)
        set(v) { sp.edit().putFloat("danmakuArea", v).apply() }
}
