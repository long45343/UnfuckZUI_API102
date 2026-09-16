package xyz.long4543.unfuckzui.feature

import android.animation.ArgbEvaluator
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.android.internal.util.ContrastColorUtil
import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzui.core.ConfigManager
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findConstructorOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.replaceMethod
import xyz.long4543.unfuckzui.core.safeHook
import java.lang.invoke.MethodHandles
import java.util.ArrayList

object UnfuckNotificationIcon : FeatureHandler {
    override val key = "honor_notification_smallicon"
    override val targets = setOf("com.android.systemui")
    override val isDynamic = false

    private var systemUiContext: Context? = null
    private var pm: PackageManager? = null
    private val isCtsMode = ThreadLocal<Boolean>()

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        if (Build.VERSION.SDK_INT >= 35) return

        val cl = param.classLoader

        safeHook("SystemUI.Application.onCreate") {
            Application::class.java.findMethodOrNull("onCreate")?.let { m ->
                module.hook(m).intercept(Hooker { chain: Chain ->
                    val result = chain.proceed()
                    val app = chain.thisObject as? Context
                    if (app != null) {
                        systemUiContext = app
                        pm = app.packageManager
                    }
                    result
                })
            }
        }

        cl.findClassOrNull("com.android.systemui.util.XSystemUtil")?.let { cls ->
            cls.findMethodOrNull("isCTSGTSTest")?.let { m ->
                safeHook("XSystemUtil.isCTSGTSTest") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) {
                            val mode = isCtsMode.get()
                            if (mode != null) return@Hooker mode
                        }
                        chain.proceed()
                    })
                }
            }
        }

        cl.findClassOrNull("com.android.systemui.battery.BatteryMeterView")?.let { cls ->
            cls.findMethodOrNull("onDarkChanged", ArrayList::class.java, Float::class.javaPrimitiveType ?: Float::class.java, Int::class.javaPrimitiveType ?: Int::class.java)?.let { m ->
                safeHook("BatteryMeterView.onDarkChanged") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) isCtsMode.set(false)
                        try {
                            chain.proceed()
                        } finally {
                            if (ConfigManager.isEnabled(key)) isCtsMode.set(null)
                        }
                    })
                }
            }
        }

        cl.findClassOrNull("com.android.systemui.statusbar.phone.DarkIconDispatcherImpl")?.let { cls ->
            val factoryCls = cl.findClassOrNull("com.android.systemui.statusbar.phone.LightBarTransitionsController\$Factory")
            val dumpCls = cl.findClassOrNull("com.android.systemui.dump.DumpManager")
            if (factoryCls != null && dumpCls != null) {
                cls.findConstructorOrNull(Context::class.java, factoryCls, dumpCls)?.let { ctor ->
                    safeHook("DarkIconDispatcherImpl.<init>") {
                        module.hook(ctor).intercept(Hooker { chain: Chain ->
                            val result = chain.proceed()
                            if (ConfigManager.isEnabled(key)) {
                                val obj = chain.thisObject
                                try {
                                    cls.getDeclaredField("mDarkModeIconColorSingleTone").apply { isAccessible = true }.setInt(obj, -0x21000000)
                                    cls.getDeclaredField("mDarkModeIconColorSingleToneCts").apply { isAccessible = true }.setInt(obj, -0x21000000)
                                    cls.getDeclaredField("mLightModeIconColorSingleTone").apply { isAccessible = true }.setInt(obj, -0x1)
                                } catch (_: Throwable) {}
                            }
                            result
                        })
                    }
                }
            }
        }

        cl.findClassOrNull("com.android.systemui.statusbar.StatusBarIconView")?.let { cls ->
            cls.findMethodOrNull("setStaticDrawableColor", Int::class.javaPrimitiveType ?: Int::class.java)?.let { m ->
                safeHook("StatusBarIconView.setStaticDrawableColor") {
                    val lookup = MethodHandles.lookup()
                    val setDrawableColor = lookup.unreflectSetter(cls.getDeclaredField("mDrawableColor").apply { isAccessible = true })
                    val setIconColor = lookup.unreflectSetter(cls.getDeclaredField("mIconColor").apply { isAccessible = true })
                    val mhSetColorInternal = lookup.unreflect(cls.getDeclaredMethod("setColorInternal", Int::class.javaPrimitiveType ?: Int::class.java).apply { isAccessible = true })
                    val mhUpdateContrastedStaticColor = lookup.unreflect(cls.getDeclaredMethod("updateContrastedStaticColor").apply { isAccessible = true })
                    val fieldDozer = cls.getDeclaredField("mDozer").apply { isAccessible = true }
                    val getDozer = lookup.unreflectGetter(fieldDozer)
                    val mhDozerSetColor = lookup.unreflect(fieldDozer.type.getDeclaredMethod("setColor", Int::class.javaPrimitiveType ?: Int::class.java).apply { isAccessible = true })

                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) {
                            val color = chain.getArg(0) as Int
                            val thiz = chain.thisObject
                            setDrawableColor.invoke(thiz, color)
                            mhSetColorInternal.invoke(thiz, color)
                            mhUpdateContrastedStaticColor.invoke(thiz)
                            setIconColor.invoke(thiz, color)
                            val dozer = getDozer.invoke(thiz)
                            if (dozer != null) {
                                mhDozerSetColor.invoke(dozer, color)
                            }
                            return@Hooker null
                        }
                        chain.proceed()
                    })
                }
            }
        }

        cl.findClassOrNull("com.android.systemui.statusbar.NotificationListener")?.let { cls ->
            cls.findMethodOrNull("replaceTheSmallIcon", StatusBarNotification::class.java)?.let { m ->
                module.replaceMethod(m, key, null)
            }
        }

        cl.findClassOrNull("com.android.systemui.statusbar.phone.NotificationIconAreaController")?.let { cls ->
            cls.findMethodOrNull("generateIconLayoutParams")?.let { m ->
                safeHook("NotificationIconAreaController.generateIconLayoutParams") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        val result = chain.proceed()
                        if (ConfigManager.isEnabled(key) && result is FrameLayout.LayoutParams) {
                            val ctx = systemUiContext
                            if (ctx != null) {
                                val dm = ctx.resources.displayMetrics
                                val h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, dm)
                                val p = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, dm)
                                result.height = h.toInt()
                                result.width += (p * 2).toInt()
                            }
                        }
                        result
                    })
                }
            }
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            cl.findClassOrNull("com.android.systemui.statusbar.phone.CentralSurfacesImpl")?.let { cls ->
                cls.findMethodOrNull("clearStatusBarIcon")?.let { m ->
                    module.replaceMethod(m, key, null)
                }
            }
        } else if (Build.VERSION.SDK_INT == 34) {
            cl.findClassOrNull("com.android.systemui.statusbar.phone.ZuiCoreImpl")?.let { cls ->
                cls.findMethodOrNull("clearStatusBarIcon")?.let { m ->
                    module.replaceMethod(m, key, null)
                }
            }
        }

        hookNotificationRow(module, cl)
    }

    private fun hookNotificationRow(module: XposedModule, cl: ClassLoader) {
        val wrapperCls = cl.findClassOrNull("com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper") ?: return
        val rowCls = cl.findClassOrNull("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow") ?: return
        val onContentUpdated = wrapperCls.findMethodOrNull("onContentUpdated", rowCls) ?: return

        safeHook("NotificationHeaderViewWrapper.onContentUpdated") {
            val mIconField = wrapperCls.getDeclaredField("mIcon").apply { isAccessible = true }
            val getEntry = rowCls.getDeclaredMethod("getEntry").apply { isAccessible = true }
            val getSbn = getEntry.returnType.getDeclaredMethod("getSbn").apply { isAccessible = true }
            val cachingIconViewCls = Class.forName("com.android.internal.widget.CachingIconView")
            val setOriginalIconColor = cachingIconViewCls.getDeclaredMethod("setOriginalIconColor", Int::class.javaPrimitiveType ?: Int::class.java).apply { isAccessible = true }
            val setBackgroundColor = cachingIconViewCls.getDeclaredMethod("setBackgroundColor", Int::class.javaPrimitiveType ?: Int::class.java).apply { isAccessible = true }

            module.hook(onContentUpdated).intercept(Hooker { chain: Chain ->
                val result = chain.proceed()
                if (!ConfigManager.isEnabled(key)) return@Hooker result

                try {
                    val row = chain.getArg(0)
                    val entry = getEntry.invoke(row)
                    val sbn = getSbn.invoke(entry) as? StatusBarNotification ?: return@Hooker result
                    val iconview = mIconField.get(chain.thisObject) as? ImageView ?: return@Hooker result

                    val keyBackground = 1145141919
                    val scale = 24.0f / 34.0f
                    if (iconview.getTag(keyBackground) != true) {
                        val d = ShapeDrawable(OvalShape())
                        d.paint.color = -0xcccccd
                        iconview.background = d
                        val lp = iconview.layoutParams
                        if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                            lp.width = Math.round(lp.width * scale)
                            lp.height = Math.round(lp.height * scale)
                            if (lp is ViewGroup.MarginLayoutParams) {
                                lp.marginStart = lp.marginStart + Math.round(lp.width * ((1.0f - scale) * 0.75f))
                            }
                        }
                        iconview.requestLayout()
                        iconview.setTag(keyBackground, true)
                    }

                    val ctx = systemUiContext ?: return@Hooker result
                    val isDark = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    val origColor = sbn.notification.color
                    val bgColor = if (origColor == 0 || origColor == 1) {
                        ctx.getColor(android.R.color.system_accent1_500)
                    } else {
                        origColor
                    }
                    val fgColor = ContrastColorUtil.resolveContrastColor(ctx, 0, bgColor, !isDark)
                    setBackgroundColor.invoke(iconview, fgColor)
                    setOriginalIconColor.invoke(iconview, bgColor)
                } catch (_: Throwable) {}
                result
            })
        }
    }
}
