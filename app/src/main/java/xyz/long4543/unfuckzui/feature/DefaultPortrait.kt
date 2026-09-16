package xyz.long4543.unfuckzui.feature

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.replaceMethod

object DefaultPortrait : FeatureHandler {
    override val key = "default_portrait"
    override val targets = setOf("system")
    override val isDynamic = false

    override fun onSystemServerStarting(module: XposedModule, param: SystemServerStartingParam) {
        val cl = param.classLoader
        cl.findClassOrNull("com.zui.server.wm.ZuiDisplayRotation")?.let { cls ->
            cls.findMethodOrNull("getDefaultDisplayRotation")?.let { m ->
                module.replaceMethod(m, key, 0)
            }
        }
    }
}
