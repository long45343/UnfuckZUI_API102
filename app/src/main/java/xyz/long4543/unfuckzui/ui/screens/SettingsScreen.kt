package xyz.long4543.unfuckzui.ui.screens

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.SystemProperties
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.long4543.unfuckzui.BuildConfig
import xyz.long4543.unfuckzui.R
import xyz.long4543.unfuckzui.ZuiApp
import xyz.long4543.unfuckzui.core.ConfigManager
import xyz.long4543.unfuckzui.service.ShizukuProcessReloader
import xyz.long4543.unfuckzui.ui.MainActivity

data class SettingItem(
    val key: String,
    val titleRes: Int,
    val summaryRes: Int? = null,
    val isDynamic: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val service by ZuiApp.serviceFlow.collectAsState()

    // 内存开关状态字典
    val featureStates = remember(service) {
        mutableStateMapOf<String, Boolean>().apply {
            val allKeys = listOf(
                "honor_notification_smallicon",
                "allow_disable_dax",
                "disable_force_stop",
                "fix_auto_guest",
                "default_portrait",
                "disable_game_helper_popup",
                "change_task_timeout",
                "disable_face_unlock_timeout",
                "package_installer_style",
                "permission_controller_style",
                "disable_virus_scan",
                "allow_get_packages",
                "default_enable_autorun",
                "unlock_cn_gms"
            )
            for (k in allKeys) {
                this[k] = if (service != null) {
                    ConfigManager.readPreference(service!!, k, true)
                } else {
                    true
                }
            }
        }
    }

    var showLauncherIcon by remember {
        val pm = context.packageManager
        val comp = ComponentName(context, MainActivity::class.java)
        mutableStateOf(pm.getComponentEnabledSetting(comp) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, R.string.action_reload_settings, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatusCard(service != null)
            }

            // 外观
            item {
                CategoryHeader(stringResource(R.string.category_appearance))
            }
            item {
                SettingSwitchCard(
                    item = SettingItem("honor_notification_smallicon", R.string.honor_notification_smallicon_title, R.string.honor_notification_smallicon_summary, isDynamic = false),
                    checked = featureStates["honor_notification_smallicon"] ?: true,
                    onCheckedChange = { checked ->
                        featureStates["honor_notification_smallicon"] = checked
                        service?.let { ConfigManager.writePreference(it, "honor_notification_smallicon", checked) }
                    }
                )
            }

            // 行为
            item {
                CategoryHeader(stringResource(R.string.category_behavior))
            }
            val behaviorItems = listOf(
                SettingItem("allow_disable_dax", R.string.allow_disable_dax_title),
                SettingItem("disable_force_stop", R.string.disable_force_stop_title, R.string.disable_force_stop_summary),
                SettingItem("fix_auto_guest", R.string.fix_guest_user_title, R.string.fix_guest_user_summary),
                SettingItem("default_portrait", R.string.default_portrait_title, isDynamic = false),
                SettingItem("disable_game_helper_popup", R.string.disable_game_helper_popup_title),
                SettingItem("change_task_timeout", R.string.change_task_timeout_title, R.string.change_task_timeout_summary, isDynamic = false),
                SettingItem("disable_face_unlock_timeout", R.string.disable_face_unlock_timeout_title, R.string.disable_face_unlock_timeout_summary, isDynamic = true)
            )
            items(behaviorItems.size) { idx ->
                val item = behaviorItems[idx]
                SettingSwitchCard(
                    item = item,
                    checked = featureStates[item.key] ?: true,
                    onCheckedChange = { checked ->
                        featureStates[item.key] = checked
                        service?.let { ConfigManager.writePreference(it, item.key, checked) }
                    }
                )
            }

            // 中国版特定
            item {
                CategoryHeader(stringResource(R.string.category_cn))
            }
            val cnItems = listOf(
                SettingItem("package_installer_style", R.string.package_installer_style_title, isDynamic = false),
                SettingItem("permission_controller_style", R.string.permission_controller_style_title),
                SettingItem("disable_virus_scan", R.string.disable_virus_scan_title),
                SettingItem("allow_get_packages", R.string.allow_get_packages_title, R.string.allow_get_packages_summary),
                SettingItem("default_enable_autorun", R.string.default_enable_autorun_title, R.string.default_enable_autorun_summary),
                SettingItem("unlock_cn_gms", R.string.unlock_cn_gms_title, R.string.unlock_cn_gms_summary, isDynamic = false)
            )
            items(cnItems.size) { idx ->
                val item = cnItems[idx]
                SettingSwitchCard(
                    item = item,
                    checked = featureStates[item.key] ?: true,
                    onCheckedChange = { checked ->
                        featureStates[item.key] = checked
                        service?.let { ConfigManager.writePreference(it, item.key, checked) }
                    }
                )
            }

            // 辅助与关于
            item {
                CategoryHeader(stringResource(R.string.category_about))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.show_launcher_icon), style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = showLauncherIcon,
                                onCheckedChange = { checked ->
                                    showLauncherIcon = checked
                                    val pm = context.packageManager
                                    val comp = ComponentName(context, MainActivity::class.java)
                                    pm.setComponentEnabledSetting(
                                        comp,
                                        if (checked) PackageManager.COMPONENT_ENABLED_STATE_DEFAULT else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                        PackageManager.DONT_KILL_APP
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { ShizukuProcessReloader.restartSystemUi(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("重启 SystemUI (Root)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val romRegion = SystemProperties.get("ro.config.zui.region", "N/A")
                        Text("${stringResource(R.string.version_title)}: ${BuildConfig.VERSION_NAME} (API 102)", style = MaterialTheme.typography.bodyMedium)
                        Text("${stringResource(R.string.rom_region)}: $romRegion", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatusCard(isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = if (isActive) stringResource(R.string.status_active) else stringResource(R.string.status_inactive),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!isActive) {
                    Text(
                        text = stringResource(R.string.message_module_not_enabled),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingSwitchCard(
    item: SettingItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium
                )
                if (item.summaryRes != null) {
                    Text(
                        text = stringResource(item.summaryRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!item.isDynamic) {
                    Text(
                        text = "需重启生效",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
