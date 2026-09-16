package xyz.long4543.unfuckzui.feature

import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import xyz.long4543.unfuckzui.core.ConfigManager
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.safeHook

object AllowGetPackages : FeatureHandler {
    override val key = "allow_get_packages"
    override val targets = setOf("system")

    private const val OP_GET_INSTALLED_APP = 214

    override fun onSystemServerStarting(module: XposedModule, param: SystemServerStartingParam) {
        val cl = param.classLoader

        cl.findClassOrNull("android.app.AppOpsManager")?.let { cls ->
            cls.findMethodOrNull("opToDefaultMode", Int::class.javaPrimitiveType ?: Int::class.java)?.let { m ->
                safeHook("AppOpsManager.opToDefaultMode") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) {
                            val op = chain.getArg(0) as? Int
                            if (op == OP_GET_INSTALLED_APP) {
                                return@Hooker 0 // AppOpsManager.MODE_ALLOWED
                            }
                        }
                        chain.proceed()
                    })
                }
            }
        }

        cl.findClassOrNull("com.android.server.appop.AppOpsService")?.let { cls ->
            cls.findMethodOrNull("checkOperationRawZui", Int::class.javaPrimitiveType ?: Int::class.java, Int::class.javaPrimitiveType ?: Int::class.java, String::class.java)?.let { m ->
                safeHook("AppOpsService.checkOperationRawZui") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) {
                            val op = chain.getArg(0) as? Int
                            if (op == OP_GET_INSTALLED_APP) {
                                return@Hooker 0 // AppOpsManager.MODE_ALLOWED
                            }
                        }
                        chain.proceed()
                    })
                }
            }
        }
    }
}
