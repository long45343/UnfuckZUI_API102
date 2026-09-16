package xyz.long4543.unfuckzui.service

import android.content.Context
import android.widget.Toast

object ShizukuProcessReloader {

    fun killPackagesWithSu(packages: List<String>): Boolean {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", "command -v su"))
            if (proc.waitFor() != 0) return false
            val su = Runtime.getRuntime().exec("su")
            su.outputStream.bufferedWriter().use { writer ->
                for (pkg in packages) {
                    if (pkg != "system" && pkg != "android") {
                        val safePkg = pkg.replace("'", "'\\''")
                        writer.write("killall '$safePkg'\n")
                        writer.write("am force-stop '$safePkg'\n")
                    }
                }
                writer.write("exit 0\n")
            }
            su.waitFor() == 0
        } catch (_: Throwable) {
            false
        }
    }

    fun restartSystemUi(context: Context) {
        val success = killPackagesWithSu(listOf("com.android.systemui"))
        if (!success) {
            Toast.makeText(context, "无法获取 Root 权限，请手动重启应用或设备", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "已成功重启 SystemUI", Toast.LENGTH_SHORT).show()
        }
    }
}
