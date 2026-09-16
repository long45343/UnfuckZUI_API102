package xyz.long4543.unfuckzzui.feature

import android.os.Build
import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import xyz.long4543.unfuckzzui.core.ConfigManager
import xyz.long4543.unfuckzzui.core.FeatureHandler
import xyz.long4543.unfuckzzui.core.findClassOrNull
import xyz.long4543.unfuckzzui.core.findMethodOrNull
import xyz.long4543.unfuckzzui.core.replaceMethod
import xyz.long4543.unfuckzzui.core.safeHook

object EnableAutorunByDefault : FeatureHandler {
    override val key = "default_enable_autorun"
    override val targets = setOf("com.zui.safecenter", "system")

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        if (param.packageName != "com.zui.safecenter") return
        val cl = param.classLoader

        cl.findClassOrNull("com.lenovo.performance.autorun.utils.AutoRunWhiteList")?.let { cls ->
            cls.findMethodOrNull("checkType", String::class.java)?.let { m ->
                safeHook("AutoRunWhiteList.checkType") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        val result = chain.proceed()
                        if (ConfigManager.isEnabled(key)) {
                            if (result is Int && result == 0) {
                                return@Hooker 2
                            }
                        }
                        result
                    })
                }
            }
        }
    }

    override fun onSystemServerStarting(module: XposedModule, param: SystemServerStartingParam) {
        if (Build.VERSION.SDK_INT < 36) return
        val cl = param.classLoader

        cl.findClassOrNull("android.app.ZuiSecurityManager")?.let { cls ->
            cls.findMethodOrNull("getRelativeAppStatus", String::class.java, String::class.java)?.let { m ->
                module.replaceMethod(m, key, 1)
            }
        }
    }
}
