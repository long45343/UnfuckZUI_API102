package xyz.long4543.unfuckzui.feature

import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import xyz.long4543.unfuckzui.core.ConfigManager
import xyz.long4543.unfuckzui.core.FeatureHandler
import xyz.long4543.unfuckzui.core.Logger
import xyz.long4543.unfuckzui.core.findClassOrNull
import xyz.long4543.unfuckzui.core.findMethodOrNull
import xyz.long4543.unfuckzui.core.safeHook
import java.lang.reflect.Field

object DisableFaceUnlockTimeout : FeatureHandler {
    override val key = "disable_face_unlock_timeout"
    override val targets = setOf("com.android.systemui")
    override val isDynamic = true

    override fun onPackageReady(module: XposedModule, param: PackageReadyParam) {
        val cl = param.classLoader
        val delegateCls = cl.findClassOrNull("com.android.keyguard.ZuiFaceAuthDelegate") ?: return

        var fSecureTime: Field? = null
        var fWakeSecureTime: Field? = null
        var fAdditionSecureTime: Field? = null
        var fCurrentTimeOff: Field? = null
        var fCurrentTimeOn: Field? = null
        var fLastPassTimestamp: Field? = null
        var fFaceUnlockManager: Field? = null
        var fSecurityTime: Field? = null

        try {
            fSecureTime = delegateCls.getDeclaredField("secureTime").apply { isAccessible = true }
            fWakeSecureTime = delegateCls.getDeclaredField("mWakeSecureTime").apply { isAccessible = true }
            fAdditionSecureTime = delegateCls.getDeclaredField("mAdditionSecureTime").apply { isAccessible = true }
            fCurrentTimeOff = delegateCls.getDeclaredField("currentTimeOff").apply { isAccessible = true }
            fCurrentTimeOn = delegateCls.getDeclaredField("currentTimeOn").apply { isAccessible = true }
            fLastPassTimestamp = delegateCls.getDeclaredField("mLastPassTimestamp").apply { isAccessible = true }
            fFaceUnlockManager = delegateCls.getDeclaredField("mFaceUnlockManager").apply { isAccessible = true }
        } catch (t: Throwable) {
            Logger.e("DisableFaceUnlockTimeout: failed to resolve ZuiFaceAuthDelegate fields", t)
        }

        delegateCls.findMethodOrNull("checkAndStartFaceDetecting", Boolean::class.javaPrimitiveType ?: Boolean::class.java)?.let { method ->
            safeHook("ZuiFaceAuthDelegate.checkAndStartFaceDetecting") {
                module.hook(method).intercept(Hooker { chain: Chain ->
                    if (ConfigManager.isEnabled(key)) {
                        try {
                            val that = chain.thisObject
                            val now = System.currentTimeMillis()
                            fSecureTime?.setLong(that, 0L)
                            fWakeSecureTime?.setLong(that, 0L)
                            fAdditionSecureTime?.setLong(that, 0L)
                            fCurrentTimeOff?.setLong(that, now)
                            fCurrentTimeOn?.setLong(that, now)
                            fLastPassTimestamp?.setLong(that, now)

                            val faceManager = fFaceUnlockManager?.get(that)
                            if (faceManager != null) {
                                if (fSecurityTime == null) {
                                    fSecurityTime = faceManager.javaClass.getDeclaredField("mSecurityTime").apply { isAccessible = true }
                                }
                                fSecurityTime?.setBoolean(faceManager, false)
                            }
                        } catch (t: Throwable) {
                            Logger.e("DisableFaceUnlockTimeout: failed to reset timeout timestamps", t)
                        }
                    }

                    val result = chain.proceed()

                    if (ConfigManager.isEnabled(key)) {
                        try {
                            val that = chain.thisObject
                            val faceManager = fFaceUnlockManager?.get(that)
                            if (faceManager != null) {
                                fSecurityTime?.setBoolean(faceManager, false)
                            }
                        } catch (_: Throwable) {}
                    }
                    result
                })
            }
        }
    }
}
