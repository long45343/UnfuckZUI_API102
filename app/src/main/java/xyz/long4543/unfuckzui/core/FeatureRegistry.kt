package xyz.long4543.unfuckzui.core

import xyz.long4543.unfuckzui.feature.AllowDisableDolbyAtmos
import xyz.long4543.unfuckzui.feature.AllowGetPackages
import xyz.long4543.unfuckzui.feature.ChangeTaskTimeout
import xyz.long4543.unfuckzui.feature.DefaultPortrait
import xyz.long4543.unfuckzui.feature.DisableForceStop
import xyz.long4543.unfuckzui.feature.DisableGameHelperPopup
import xyz.long4543.unfuckzui.feature.EnableAutorunByDefault
import xyz.long4543.unfuckzui.feature.FixAutoGuest
import xyz.long4543.unfuckzui.feature.PackageInstallerHook
import xyz.long4543.unfuckzui.feature.PermissionControllerHook
import xyz.long4543.unfuckzui.feature.SafeCenterHook
import xyz.long4543.unfuckzui.feature.UnfuckNotificationIcon
import xyz.long4543.unfuckzui.feature.UnfuckNotificationIconZui17
import xyz.long4543.unfuckzui.feature.UnlockCnGms

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
