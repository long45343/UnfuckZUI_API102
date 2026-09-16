package xyz.long4543.unfuckzui.feature

import android.content.Context
import android.os.Build
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.replaceMethod
import java.util.ArrayList

object DisableForceStop : FeatureHandler {
    override val key = "disable_force_stop"
    override val targets = setOf("com.zui.launcher")

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader
        if (Build.VERSION.SDK_INT <= 35) {
            cl.findClassOrNull("com.android.systemui.shared.system.ActivityManagerWrapper")?.let { cls ->
                cls.findMethodOrNull("removeAllRunningAppProcesses", Context::class.java, ArrayList::class.java)?.let { m ->
                    module.replaceMethod(m, key, null)
                }
                cls.findMethodOrNull("removeAppProcess", Context::class.java, Int::class.javaPrimitiveType ?: Int::class.java, String::class.java, Int::class.javaPrimitiveType ?: Int::class.java)?.let { m ->
                    module.replaceMethod(m, key, null)
                }
            }
        } else {
            cl.findClassOrNull("com.zui.launcher.util.OverviewUtilities")?.let { cls ->
                cls.findMethodOrNull("removeAllRunningAppProcesses", Context::class.java, ArrayList::class.java, Boolean::class.javaPrimitiveType ?: Boolean::class.java)?.let { m ->
                    module.replaceMethod(m, key, null)
                }
                cls.findMethodOrNull("removeAppProcess", Context::class.java, Int::class.javaPrimitiveType ?: Int::class.java, String::class.java, Int::class.javaPrimitiveType ?: Int::class.java)?.let { m ->
                    module.replaceMethod(m, key, null)
                }
            }
        }
    }
}
