package xyz.long4543.unfuckzzui.core

import io.github.libxposed.api.XposedInterface.Chain
import io.github.libxposed.api.XposedInterface.HookHandle
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

inline fun <T> safeHook(tag: String, block: () -> T): T? {
    return try {
        block()
    } catch (e: NoSuchMethodException) {
        Logger.w("[$tag] Hook skipped: Target method not found: ${e.message}")
        null
    } catch (e: ClassNotFoundException) {
        Logger.w("[$tag] Hook skipped: Target class not found: ${e.message}")
        null
    } catch (e: NoSuchFieldException) {
        Logger.w("[$tag] Hook skipped: Target field not found: ${e.message}")
        null
    } catch (t: Throwable) {
        Logger.e("[$tag] Unexpected failure during hook setup", t)
        null
    }
}

fun XposedModule.replaceMethod(
    executable: Executable,
    featureKey: String,
    returnValue: Any? = null
): HookHandle? {
    return safeHook(featureKey) {
        hook(executable).intercept(Hooker { chain: Chain ->
            if (!ConfigManager.isEnabled(featureKey)) {
                return@Hooker chain.proceed()
            }
            returnValue
        })
    }
}

fun ClassLoader.findClassOrNull(className: String): Class<*>? {
    return try {
        Class.forName(className, false, this)
    } catch (_: ClassNotFoundException) {
        null
    }
}

fun Class<*>.findMethodOrNull(name: String, vararg paramTypes: Class<*>): Method? {
    return try {
        getDeclaredMethod(name, *paramTypes).apply { isAccessible = true }
    } catch (_: NoSuchMethodException) {
        null
    }
}

fun Class<*>.findConstructorOrNull(vararg paramTypes: Class<*>): Constructor<*>? {
    return try {
        getDeclaredConstructor(*paramTypes).apply { isAccessible = true }
    } catch (_: NoSuchMethodException) {
        null
    }
}
