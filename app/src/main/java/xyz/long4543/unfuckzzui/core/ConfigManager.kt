package xyz.long4543.unfuckzzui.core

import android.content.SharedPreferences
import io.github.libxposed.api.XposedModule
import io.github.libxposed.service.XposedService
import java.util.concurrent.ConcurrentHashMap

object ConfigManager {
    const val PREF_GROUP = "feature_config"

    private var hookPrefs: SharedPreferences? = null
    private val memoryFlags = ConcurrentHashMap<String, Boolean>()
    // 保持对 listener 的强引用，防止被 GC 回收
    private var prefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun initInHookProcess(module: XposedModule) {
        try {
            val prefs = module.getRemotePreferences(PREF_GROUP)
            hookPrefs = prefs
            prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                if (key != null) {
                    val newVal = sp.getBoolean(key, true)
                    memoryFlags[key] = newVal
                    Logger.d("Config updated in memory: $key -> $newVal")
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
        } catch (t: Throwable) {
            Logger.e("Failed to initialize RemotePreferences in hook process", t)
        }
    }

    fun isEnabled(key: String, defaultValue: Boolean = true): Boolean {
        return memoryFlags.computeIfAbsent(key) {
            hookPrefs?.getBoolean(key, defaultValue) ?: defaultValue
        }
    }

    fun writePreference(service: XposedService, key: String, value: Boolean) {
        try {
            service.getRemotePreferences(PREF_GROUP).edit().putBoolean(key, value).apply()
        } catch (t: Throwable) {
            Logger.e("Failed to write RemotePreference for $key", t)
        }
    }

    fun readPreference(service: XposedService, key: String, defaultValue: Boolean = true): Boolean {
        return try {
            service.getRemotePreferences(PREF_GROUP).getBoolean(key, defaultValue)
        } catch (t: Throwable) {
            Logger.e("Failed to read RemotePreference for $key", t)
            defaultValue
        }
    }
}
