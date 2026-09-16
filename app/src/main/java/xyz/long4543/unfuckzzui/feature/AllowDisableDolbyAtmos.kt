package xyz.long4543.unfuckzzui.feature

import android.content.Context
import android.os.Build
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

object AllowDisableDolbyAtmos : FeatureHandler {
    override val key = "allow_disable_dax"
    override val targets = setOf("com.android.settings", "com.android.systemui", "com.zui.game.service")

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader
        when (param.packageName) {
            "com.android.settings" -> handleSettings(module, cl)
            "com.android.systemui" -> handleSystemUi(module, cl)
            "com.zui.game.service" -> handleGameService(module, cl)
        }
    }

    private fun handleSettings(module: XposedModule, cl: ClassLoader) {
        when {
            Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> {
                cl.findClassOrNull("com.android.settings.dolby.DolbyAtmosPreferenceFragment")?.let { cls ->
                    cls.findMethodOrNull("getheadsetStatus")?.let { m ->
                        module.replaceMethod(m, key, 1)
                    }
                }
            }
            Build.VERSION.SDK_INT == 34 -> {
                cl.findClassOrNull("com.lenovo.settings.sound.dolby.DolbyAtmosFragment")?.let { cls ->
                    cls.findMethodOrNull("isHeadsetConnected")?.let { m ->
                        module.replaceMethod(m, key, true)
                    }
                    cls.findMethodOrNull("initView")?.let { m ->
                        safeHook("DolbyAtmosFragment.initView") {
                            module.hook(m).intercept(Hooker { chain: Chain ->
                                val result = chain.proceed()
                                if (ConfigManager.isEnabled(key)) {
                                    try {
                                        val field = cls.getDeclaredField("mDolbySwitchPreference").apply { isAccessible = true }
                                        val pref = field.get(chain.thisObject)
                                        pref?.javaClass?.getMethod("setSummary", CharSequence::class.java)?.invoke(pref, null)
                                    } catch (_: Throwable) {}
                                }
                                result
                            })
                        }
                    }
                }
            }
            Build.VERSION.SDK_INT >= 35 -> {
                cl.findClassOrNull("com.lenovo.settings.sound.dolby.DolbyAtmosUtils")?.let { cls ->
                    cls.findMethodOrNull("isHeadsetConnected", Context::class.java)?.let { m ->
                        module.replaceMethod(m, key, true)
                    }
                }
                cl.findClassOrNull("com.lenovo.settings.sound.dolby.DolbySwitchPreferenceController")?.let { cls ->
                    val prefCls = cl.findClassOrNull("androidx.preference.Preference") ?: return
                    cls.findMethodOrNull("updateState", prefCls)?.let { m ->
                        safeHook("DolbySwitchPreferenceController.updateState") {
                            module.hook(m).intercept(Hooker { chain: Chain ->
                                if (ConfigManager.isEnabled(key)) {
                                    try {
                                        val pref = chain.getArg(0)
                                        pref?.javaClass?.getMethod("setSummary", CharSequence::class.java)?.invoke(pref, null)
                                    } catch (_: Throwable) {}
                                }
                                chain.proceed()
                            })
                        }
                    }
                }
            }
        }
    }

    private fun handleSystemUi(module: XposedModule, cl: ClassLoader) {
        cl.findClassOrNull("com.android.systemui.qs.tiles.QDolbyAtmosTile")?.let { cls ->
            val methodName = if (Build.VERSION.SDK_INT <= 34) "isHeadSetConnect" else "isHeadSetConnect\$2"
            cls.findMethodOrNull(methodName)?.let { m ->
                module.replaceMethod(m, key, true)
            }
        }
        cl.findClassOrNull("com.android.systemui.qs.tiles.QDolbyAtmosDetailView")?.let { cls ->
            cls.findMethodOrNull("isHeadSetConnect")?.let { m ->
                module.replaceMethod(m, key, true)
            }
        }
    }

    private fun handleGameService(module: XposedModule, cl: ClassLoader) {
        cl.findClassOrNull("com.zui.game.service.util.DolbyUtils")?.let { cls ->
            cls.findMethodOrNull("handleDolbyGameSound", Context::class.java, Int::class.javaPrimitiveType ?: Int::class.java)?.let { m ->
                module.replaceMethod(m, key, null)
            }
        }
        cl.findClassOrNull("com.zui.util.SettingsValueUtilKt")?.let { cls ->
            cls.findMethodOrNull("isHeadsetConnected", Context::class.java)?.let { m ->
                module.replaceMethod(m, key, true)
            }
        }
    }
}
