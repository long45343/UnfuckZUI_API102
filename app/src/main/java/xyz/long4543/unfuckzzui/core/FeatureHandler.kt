package xyz.long4543.unfuckzzui.core

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

interface FeatureHandler {
    val key: String
    val targets: Set<String>
    val isDynamic: Boolean get() = true

    fun onSystemServerStarting(module: XposedModule, param: SystemServerStartingParam) {}
    fun onPackageReady(module: XposedModule, param: PackageReadyParam) {}
}
