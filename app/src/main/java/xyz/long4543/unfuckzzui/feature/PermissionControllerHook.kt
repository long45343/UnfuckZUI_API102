package xyz.long4543.unfuckzzui.feature

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzzui.core.ConfigManager
import xyz.long4543.unfuckzzui.core.FeatureHandler
import xyz.long4543.unfuckzzui.core.findClassOrNull
import xyz.long4543.unfuckzzui.core.findMethodOrNull
import xyz.long4543.unfuckzzui.core.replaceMethod
import xyz.long4543.unfuckzzui.core.safeHook
import java.lang.invoke.MethodHandles

object PermissionControllerHook : FeatureHandler {
    override val key = "permission_controller_style"
    override val targets = setOf("com.android.permissioncontroller", "com.android.settings", "com.zui.safecenter")

    private val isRowVersionTls = ThreadLocal<Boolean>()

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader
        when (param.packageName) {
            "com.android.permissioncontroller" -> handlePermissionController(module, cl)
            "com.android.settings" -> handleSettings(module, cl)
            "com.zui.safecenter" -> handleSafeCenter(module, cl)
        }
    }

    private fun handlePermissionController(module: XposedModule, cl: ClassLoader) {
        val zuiUtilsCls = cl.findClassOrNull("com.android.permissioncontroller.extra.ZuiUtils")
            ?: cl.findClassOrNull("com.android.permissioncontroller.permission.utils.ZuiUtils")

        zuiUtilsCls?.findMethodOrNull("isCTSandGTS", String::class.java)?.let { m ->
            module.replaceMethod(m, key, true)
        }

        if (Build.VERSION.SDK_INT <= 34) {
            cl.findClassOrNull("com.android.permissioncontroller.permission.ui.GrantPermissionsActivity")?.let { cls ->
                cls.findMethodOrNull("onCreate", Bundle::class.java)?.let { m ->
                    safeHook("GrantPermissionsActivity.onCreate") {
                        module.hook(m).intercept(Hooker { chain: Chain ->
                            if (ConfigManager.isEnabled(key)) {
                                val activity = chain.thisObject as? Activity
                                activity?.apply {
                                    setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                                    window?.decorView?.apply {
                                        filterTouchesWhenObscured = true
                                        setPadding(0, 0, 0, 0)
                                    }
                                }
                            }
                            chain.proceed()
                        })
                    }
                }
            }
        }
    }

    private fun handleSettings(module: XposedModule, cl: ClassLoader) {
        cl.findClassOrNull("com.lenovo.common.utils.LenovoUtils")?.let { cls ->
            cls.findMethodOrNull("isRowVersion")?.let { m ->
                safeHook("LenovoUtils.isRowVersion") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) {
                            val value = isRowVersionTls.get()
                            if (value != null) return@Hooker value
                        }
                        chain.proceed()
                    })
                }
            }
        }

        fun hookWithTls(clsName: String, methodName: String, vararg paramTypes: Class<*>) {
            cl.findClassOrNull(clsName)?.let { cls ->
                cls.findMethodOrNull(methodName, *paramTypes)?.let { m ->
                    safeHook("$clsName.$methodName") {
                        module.hook(m).intercept(Hooker { chain: Chain ->
                            if (ConfigManager.isEnabled(key)) {
                                isRowVersionTls.set(true)
                            }
                            try {
                                chain.proceed()
                            } finally {
                                if (ConfigManager.isEnabled(key)) {
                                    isRowVersionTls.remove()
                                }
                            }
                        })
                    }
                }
            }
        }

        hookWithTls("com.android.settings.applications.appinfo.AppPermissionPreferenceController", "startManagePermissionsActivity")

        val prefCls = cl.findClassOrNull("androidx.preference.Preference")
        if (prefCls != null) {
            hookWithTls("com.lenovo.settings.privacy.PrivacyManagerPreferenceController", "handlePreferenceTreeClick", prefCls)
        }

        if (Build.VERSION.SDK_INT >= 36) {
            hookWithTls("com.lenovo.settings.applications.LenovoAppHeaderPreferenceController", "handlePermissionClick")
        } else {
            val viewCls = cl.findClassOrNull("android.view.View")
            if (viewCls != null) {
                hookWithTls("com.lenovo.settings.applications.LenovoAppHeaderPreferenceController", "lambda\$initAppEntryList\$0\$com-lenovo-settings-applications-LenovoAppHeaderPreferenceController", viewCls)
            }
        }
    }

    private fun handleSafeCenter(module: XposedModule, cl: ClassLoader) {
        val cls = cl.findClassOrNull("com.lenovo.xuipermissionmanager.XuiPermissionManager") ?: return
        val supercls = cls.superclass ?: return
        val onCreate = cls.findMethodOrNull("onCreate", Bundle::class.java) ?: return
        val superOnCreate = supercls.findMethodOrNull("onCreate", Bundle::class.java) ?: return

        safeHook("XuiPermissionManager.onCreate") {
            val superInvoker = MethodHandles.lookup().unreflectSpecial(superOnCreate, cls)
            module.hook(onCreate).intercept(Hooker { chain: Chain ->
                if (ConfigManager.isEnabled(key)) {
                    val arg0 = chain.getArg(0)
                    superInvoker.invoke(chain.thisObject, arg0)
                    val activity = chain.thisObject as Activity
                    activity.startActivity(Intent("android.intent.action.MANAGE_PERMISSIONS"))
                    activity.finish()
                    return@Hooker null
                }
                chain.proceed()
            })
        }
    }
}
