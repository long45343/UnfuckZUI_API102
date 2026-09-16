package xyz.long4543.unfuckzui.feature

import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
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
import java.lang.invoke.MethodHandles

object UnfuckNotificationIconZui17 : FeatureHandler {
    override val key = "honor_notification_smallicon"
    override val targets = setOf("com.android.systemui")
    override val isDynamic = false

    private val isCtsMode = ThreadLocal<Boolean>()

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        if (Build.VERSION.SDK_INT < 35) return

        val cl = param.classLoader

        cl.findClassOrNull("com.android.systemui.util.XSystemUtil")?.let { cls ->
            cls.findMethodOrNull("isCTSGTSTest")?.let { m ->
                safeHook("XSystemUtil.isCTSGTSTest(Zui17)") {
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

        cl.findClassOrNull("com.android.systemui.statusbar.NotificationShelf")?.let { cls ->
            cls.findMethodOrNull("updateResources\$5")?.let { m ->
                safeHook("NotificationShelf.updateResources$5") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        if (ConfigManager.isEnabled(key)) isCtsMode.set(true)
                        try {
                            chain.proceed()
                        } finally {
                            if (ConfigManager.isEnabled(key)) isCtsMode.remove()
                        }
                    })
                }
            }
        }

        if (Build.VERSION.SDK_INT == 35) {
            cl.findClassOrNull("com.android.systemui.statusbar.NotificationListener")?.let { cls ->
                cls.findMethodOrNull("replaceTheSmallIcon", StatusBarNotification::class.java)?.let { m ->
                    module.replaceMethod(m, key, null)
                }
            }
        } else if (Build.VERSION.SDK_INT >= 36) {
            cl.findClassOrNull("com.android.systemui.util.QSUtil")?.let { cls ->
                cls.findMethodOrNull("replaceTheSmallIcon", Context::class.java, StatusBarNotification::class.java)?.let { m ->
                    module.replaceMethod(m, key, null)
                }
            }
        }

        val wrapperCls = cl.findClassOrNull("com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper")
        val rowCls = cl.findClassOrNull("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow")
        if (wrapperCls != null && rowCls != null) {
            wrapperCls.findMethodOrNull("onContentUpdated", rowCls)?.let { onContentUpdated ->
                safeHook("NotificationHeaderViewWrapper.onContentUpdated(Zui17)") {
                    val mIconField = wrapperCls.getDeclaredField("mIcon").apply { isAccessible = true }
                    val getIcon = MethodHandles.lookup().unreflectGetter(mIconField)

                    module.hook(onContentUpdated).intercept(Hooker { chain: Chain ->
                        val result = chain.proceed()
                        if (!ConfigManager.isEnabled(key)) return@Hooker result

                        try {
                            val iconview = getIcon.invoke(chain.thisObject) as? ImageView ?: return@Hooker result
                            val keySizeUnfucked = 1145141919
                            if (iconview.getTag(keySizeUnfucked) == true) return@Hooker result

                            val lp = iconview.layoutParams
                            if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                                val dm = iconview.context.resources.displayMetrics
                                val diameter = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, dm)
                                val dInt = Math.round(diameter)
                                lp.width = dInt
                                lp.height = dInt
                                if (lp is ViewGroup.MarginLayoutParams) {
                                    lp.marginStart = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, dm))
                                }
                                iconview.requestLayout()
                            }
                            iconview.setTag(keySizeUnfucked, true)
                        } catch (_: Throwable) {}
                        result
                    })
                }
            }
        }

        if (Build.VERSION.SDK_INT == 35) {
            cl.findClassOrNull("com.android.systemui.notificationlist.view.NotificationHeaderView")?.let { cls ->
                cls.findMethodOrNull("shouldShowIconBackground")?.let { m ->
                    module.replaceMethod(m, key, true)
                }
            }
            cl.findClassOrNull("android.app.Notification\$Builder")?.let { cls ->
                cls.findMethodOrNull("isCtsGtsTest")?.let { m ->
                    module.replaceMethod(m, key, true)
                }
            }
        }
    }
}
