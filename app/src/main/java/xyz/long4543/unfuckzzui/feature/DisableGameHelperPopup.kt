package xyz.long4543.unfuckzzui.feature

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzzui.core.FeatureHandler
import xyz.long4543.unfuckzzui.core.findClassOrNull
import xyz.long4543.unfuckzzui.core.findMethodOrNull
import xyz.long4543.unfuckzzui.core.replaceMethod

object DisableGameHelperPopup : FeatureHandler {
    override val key = "disable_game_helper_popup"
    override val targets = setOf("com.zui.game.service")

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader
        cl.findClassOrNull("com.zui.game.service.ui.FloatingGameNoticController")?.let { cls ->
            cls.findMethodOrNull("show")?.let { m ->
                module.replaceMethod(m, key, null)
            }
        }
    }
}
