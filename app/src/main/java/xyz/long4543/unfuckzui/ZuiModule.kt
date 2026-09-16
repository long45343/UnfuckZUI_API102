package xyz.long4543.unfuckzui

import android.os.SystemProperties
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import xyz.long4543.unfuckzui.core.ConfigManager
import xyz.long4543.unfuckzui.core.FeatureRegistry
import xyz.long4543.unfuckzui.core.Logger

class ZuiModule : XposedModule() {

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        super.onModuleLoaded(param)
        Logger.init(this)

        val zuiVersion = try {
            SystemProperties.get("ro.com.zui.version", "")
        } catch (_: Throwable) {
            ""
        }

        if (zuiVersion.isEmpty()) {
            Logger.w("Not a ZUI ROM environment (ro.com.zui.version is empty), skipping hooks.")
            return
        }

        ConfigManager.initInHookProcess(this)
        Logger.i("UnfuckZUI API 102 module successfully initialized on ZUI: $zuiVersion")
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        super.onSystemServerStarting(param)
        Logger.d("System server starting, dispatching system hooks...")
        FeatureRegistry.features.forEach { feature ->
            if (feature.targets.contains("system")) {
                try {
                    feature.onSystemServerStarting(this, param)
                } catch (t: Throwable) {
                    Logger.e("Error dispatching onSystemServerStarting for ${feature.key}", t)
                }
            }
        }
    }

    override fun onPackageReady(param: PackageReadyParam) {
        super.onPackageReady(param)
        val pkg = param.packageName
        FeatureRegistry.features.forEach { feature ->
            if (feature.targets.contains(pkg)) {
                try {
                    feature.onPackageReady(this, param)
                } catch (t: Throwable) {
                    Logger.e("Error dispatching onPackageReady for ${feature.key} in $pkg", t)
                }
            }
        }
    }
}
