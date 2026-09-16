package xyz.long4543.unfuckzzui.core

import xyz.long4543.unfuckzzui.feature.AllowDisableDolbyAtmos
import xyz.long4543.unfuckzzui.feature.AllowGetPackages
import xyz.long4543.unfuckzzui.feature.ChangeTaskTimeout
import xyz.long4543.unfuckzzui.feature.DefaultPortrait
import xyz.long4543.unfuckzzui.feature.DisableForceStop
import xyz.long4543.unfuckzzui.feature.DisableGameHelperPopup
import xyz.long4543.unfuckzzui.feature.EnableAutorunByDefault
import xyz.long4543.unfuckzzui.feature.FixAutoGuest
import xyz.long4543.unfuckzzui.feature.PackageInstallerHook
import xyz.long4543.unfuckzzui.feature.PermissionControllerHook
import xyz.long4543.unfuckzzui.feature.SafeCenterHook
import xyz.long4543.unfuckzzui.feature.UnfuckNotificationIcon
import xyz.long4543.unfuckzzui.feature.UnfuckNotificationIconZui17
import xyz.long4543.unfuckzzui.feature.UnlockCnGms

object FeatureRegistry {
    val features: List<FeatureHandler> = listOf(
        AllowDisableDolbyAtmos,
        DisableForceStop,
        PackageInstallerHook,
        PermissionControllerHook,
        SafeCenterHook,
        UnfuckNotificationIcon,
        UnfuckNotificationIconZui17,
        FixAutoGuest,
        DefaultPortrait,
        AllowGetPackages,
        EnableAutorunByDefault,
        UnlockCnGms,
        DisableGameHelperPopup,
        ChangeTaskTimeout
    )
}
