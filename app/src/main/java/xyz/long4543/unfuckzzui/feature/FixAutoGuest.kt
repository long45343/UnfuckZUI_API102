package xyz.long4543.unfuckzzui.feature

import android.content.Context
import android.provider.Settings
import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzzui.core.ConfigManager
import xyz.long4543.unfuckzzui.core.FeatureHandler
import xyz.long4543.unfuckzzui.core.findClassOrNull
import xyz.long4543.unfuckzzui.core.findMethodOrNull
import xyz.long4543.unfuckzzui.core.safeHook

object FixAutoGuest : FeatureHandler {
    override val key = "fix_auto_guest"
    override val targets = setOf("com.android.systemui")

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader
        cl.findClassOrNull("com.android.systemui.user.domain.interactor.GuestUserInteractor")?.let { cls ->
            cls.findMethodOrNull("isDeviceAllowedToAddGuest")?.let { m ->
                safeHook("GuestUserInteractor.isDeviceAllowedToAddGuest") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) {
                            try {
                                val field = cls.getDeclaredField("applicationContext").apply { isAccessible = true }
                                val context = field.get(chain.thisObject) as? Context
                                if (context != null && Settings.Global.getInt(context.contentResolver, "user_switcher_enabled", 0) == 0) {
                                    return@Hooker false
                                }
                            } catch (_: Throwable) {}
                        }
                        chain.proceed()
                    })
                }
            }
        }
    }
}
