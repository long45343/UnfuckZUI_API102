package xyz.long4543.unfuckzzui.feature

import android.content.res.Resources
import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import xyz.long4543.unfuckzzui.core.ConfigManager
import xyz.long4543.unfuckzzui.core.FeatureHandler
import xyz.long4543.unfuckzzui.core.findClassOrNull
import xyz.long4543.unfuckzzui.core.findMethodOrNull
import xyz.long4543.unfuckzzui.core.safeHook

object ChangeTaskTimeout : FeatureHandler {
    override val key = "change_task_timeout"
    override val targets = setOf("system")
    override val isDynamic = false

    override fun onSystemServerStarting(module: XposedModule, param: SystemServerStartingParam) {
        val cl = param.classLoader
        cl.findClassOrNull("com.android.server.wm.RecentTasks")?.let { cls ->
            cls.findMethodOrNull("loadParametersFromResources", Resources::class.java)?.let { m ->
                safeHook("RecentTasks.loadParametersFromResources") {
                    module.hook(m).intercept(Hooker { chain: Chain ->
                        val result = chain.proceed()
                        if (ConfigManager.isEnabled(key)) {
                            try {
                                val that = chain.thisObject
                                val fMax = cls.getDeclaredField("mMaxNumVisibleTasks").apply { isAccessible = true }
                                val fDuration = cls.getDeclaredField("mActiveTasksSessionDurationMs").apply { isAccessible = true }
                                fMax.setInt(that, -1)
                                fDuration.setLong(that, 7L * 24 * 60 * 60 * 1000L)
                            } catch (_: Throwable) {}
                        }
                        result
                    })
                }
            }
        }
    }
}
