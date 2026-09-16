package xyz.long4543.unfuckzzui.feature

import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import xyz.long4543.unfuckzzui.core.ConfigManager
import xyz.long4543.unfuckzzui.core.FeatureHandler
import xyz.long4543.unfuckzzui.core.Logger
import xyz.long4543.unfuckzzui.core.findClassOrNull
import xyz.long4543.unfuckzzui.core.safeHook

object UnlockCnGms : FeatureHandler {
    override val key = "unlock_cn_gms"
    override val targets = setOf("system")
    override val isDynamic = false

    override fun onSystemServerStarting(module: XposedModule, param: SystemServerStartingParam) {
        val cl = param.classLoader
        val pmsClass = cl.findClassOrNull("com.android.server.pm.PackageManagerService") ?: return
        val pmsMain = pmsClass.declaredMethods.firstOrNull { it.name == "main" } ?: return

        safeHook("PackageManagerService.main") {
            module.hook(pmsMain).intercept(Hooker { chain: Chain ->
                val result = chain.proceed()
                if (ConfigManager.isEnabled(key)) {
                    try {
                        val cfgCls = cl.findClassOrNull("com.android.server.SystemConfig")
                        val cfgGetInstance = cfgCls?.getDeclaredMethod("getInstance")
                        val cfgRemoveFeature = cfgCls?.getDeclaredMethod("removeFeature", String::class.java)
                        val cfg = cfgGetInstance?.invoke(null)
                        if (cfg != null && cfgRemoveFeature != null) {
                            cfgRemoveFeature.invoke(cfg, "cn.google.services")
                            cfgRemoveFeature.invoke(cfg, "com.google.android.feature.services_updater")
                            Logger.i("Unlocked CN GMS restriction packages in SystemConfig")
                        }
                    } catch (t: Throwable) {
                        Logger.e("Failed to remove CN GMS restrictions", t)
                    }
                }
                result
            })
        }
    }
}
