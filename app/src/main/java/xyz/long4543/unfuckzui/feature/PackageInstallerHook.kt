package xyz.long4543.unfuckzui.feature

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Window
import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzui.core.ConfigManager
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.replaceMethod
import xyz.long4543.unfuckzui.core.safeHook

object PackageInstallerHook : FeatureHandler {
    override val key = "package_installer_style"
    override val targets = setOf("com.android.packageinstaller")
    override val isDynamic = false

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader

        cl.findClassOrNull("com.android.packageinstaller.extra.Utils")?.let { utilsCls ->
            utilsCls.findMethodOrNull("isCTSandGTS", String::class.java)?.let { m ->
                module.replaceMethod(m, key, true)
            }
            utilsCls.findMethodOrNull("isCTSandGTS", String::class.java, Intent::class.java)?.let { m ->
                module.replaceMethod(m, key, true)
            }
        }

        val rStyleCls = cl.findClassOrNull("com.android.packageinstaller.R\$style") ?: return
        val themeField = try {
            rStyleCls.getField("Theme_AlertDialogActivity")
        } catch (_: NoSuchFieldException) {
            return
        }
        val themeId = themeField.getInt(null)

        safeHook("PackageInstaller.Activity.onCreate") {
            val onCreate = Activity::class.java.findMethodOrNull("onCreate", Bundle::class.java) ?: return@safeHook
            module.hook(onCreate).intercept(Hooker { chain: Chain ->
                if (ConfigManager.isEnabled(key)) {
                    val activity = chain.thisObject as? Activity
                    activity?.apply {
                        setTheme(themeId)
                        try {
                            val setTranslucent = Activity::class.java.getDeclaredMethod("setTranslucent", Boolean::class.javaPrimitiveType)
                            setTranslucent.isAccessible = true
                            setTranslucent.invoke(this, true)
                        } catch (_: Throwable) {}
                        requestWindowFeature(Window.FEATURE_NO_TITLE)
                        window?.setWindowAnimations(0)
                    }
                }
                chain.proceed()
            })
        }
    }
}
