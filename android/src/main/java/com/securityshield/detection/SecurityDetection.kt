package com.securityshield

import android.os.Build
import java.io.File

object SecurityDetection {

    @JvmStatic
    fun isFridaDetected(): Boolean {
        val fridaFiles = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/frida",
            "/data/local/tmp/re.frida.server",
            "/data/local/tmp/fridaserver",
            "/system/bin/frida",
            "/system/xbin/frida"
        )

        if (fridaFiles.any { File(it).exists() }) return true
        if (scanSelfMapsForFrida()) return true
        if (isFridaPortOpen()) return true
        if (scanProcCmdlineForFrida()) return true
        if (NativeIntegrity.isFridaDetectedNative()) return true

        return false
    }

    private fun scanSelfMapsForFrida(): Boolean {
        return try {
            val keywords = arrayOf(
                "frida",
                "gum-js-loop",
                "gadget",
                "libfrida",
                "frida-agent",
                "re.frida",
                "linjector",
                "xposed",
                "substrate"
            )

            File("/proc/self/maps").useLines { lines ->
                lines.any { line ->
                    val lower = line.lowercase()
                    keywords.any { keyword -> lower.contains(keyword) }
                }
            }
        } catch (_: Throwable) {
            false
        }
    }

    private fun isFridaPortOpen(): Boolean {
        return try {
            val tcp = File("/proc/net/tcp")
            if (!tcp.exists()) return false

            val text = tcp.readText()
            text.contains(":6992", ignoreCase = true) ||
                text.contains(":6993", ignoreCase = true)
        } catch (_: Throwable) {
            false
        }
    }

    private fun scanProcCmdlineForFrida(): Boolean {
        return try {
            val keywords = arrayOf(
                "frida",
                "frida-server",
                "re.frida.server",
                "gum-js-loop",
                "linjector"
            )

            val procDir = File("/proc")
            val pids = procDir.listFiles()?.asSequence()
                ?.filter { it.isDirectory && it.name.all(Char::isDigit) }
                ?.take(128)
                ?: return false

            for (pidDir in pids) {
                val cmdline = File(pidDir, "cmdline")
                val content = try {
                    cmdline.readBytes().toString(Charsets.UTF_8).lowercase()
                } catch (_: Throwable) {
                    continue
                }

                if (keywords.any { content.contains(it) }) return true
            }

            false
        } catch (_: Throwable) {
            false
        }
    }

    @JvmStatic
    fun isXposedDetected(): Boolean {
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: Throwable) {
            false
        }
    }

    @JvmStatic
    fun isRooted(): Boolean {
        val rootFiles = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        if (rootFiles.any { File(it).exists() }) return true

        val buildTags = Build.TAGS
        if (!buildTags.isNullOrBlank() && buildTags.contains("test-keys")) return true

        return false
    }
}