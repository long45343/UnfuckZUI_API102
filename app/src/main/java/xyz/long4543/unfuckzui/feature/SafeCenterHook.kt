package xyz.long4543.unfuckzui.feature

import android.content.Context
import android.content.Intent
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.replaceMethod

object SafeCenterHook : FeatureHandler {
    override val key = "disable_virus_scan"
    override val targets = setOf("com.zui.safecenter")

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader

        cl.findClassOrNull("tmsdk.fg.creator.ManagerCreatorF")?.let { cls ->
            cls.findMethodOrNull("getManager", Class::class.java)?.let { m ->
                module.replaceMethod(m, key, null)
            }
        }

        cl.findClassOrNull("com.lenovo.safecenter.antivirus.external.AntiVirusInterface")?.let { cls ->
            cls.findMethodOrNull("initTMSApplication", Context::class.java, Boolean::class.javaPrimitiveType ?: Boolean::class.java)?.let { m ->
                module.replaceMethod(m, key, null)
            }
        }

        cl.findClassOrNull("com.lenovo.safecenter.antivirus.tmsdbupdate.UpdateTMSVDBReceiver")?.let { cls ->
            cls.findMethodOrNull("onReceive", Context::class.java, Intent::class.java)?.let { m ->
                module.replaceMethod(m, key, null)
            }
        }
    }
}
