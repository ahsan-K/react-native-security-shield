package com.securityshield

import android.os.Build
import java.io.File

object EmulatorDetection {

    @JvmStatic
    fun isRunningOnEmulator(): Boolean {
        val model = Build.MODEL ?: ""
        val manufacturer = Build.MANUFACTURER ?: ""
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""
        val hardware = Build.HARDWARE ?: ""
        val product = Build.PRODUCT ?: ""
        val fingerprint = Build.FINGERPRINT ?: ""

        if (fingerprint.startsWith("generic", ignoreCase = true) ||
            fingerprint.contains("virtual", ignoreCase = true)) {
            return true
        }

        if (model.contains("Emulator", ignoreCase = true) ||
            model.contains("Android SDK built for x86", ignoreCase = true) ||
            model.contains("sdk_gphone", ignoreCase = true)) {
            return true
        }

        if (manufacturer.contains("Genymotion", ignoreCase = true)) {
            return true
        }

        if (manufacturer.equals("Google", ignoreCase = true) &&
            (brand.startsWith("generic", ignoreCase = true) ||
             device.startsWith("generic", ignoreCase = true))) {
            return true
        }

        if (hardware.equals("goldfish", ignoreCase = true) ||
            hardware.equals("ranchu", ignoreCase = true) ||
            hardware.equals("qemu", ignoreCase = true) ||
            hardware.equals("vbox86", ignoreCase = true)) {
            return true
        }

        if (product.contains("sdk", ignoreCase = true) ||
            product.contains("emulator", ignoreCase = true) ||
            product.contains("simulator", ignoreCase = true)) {
            return true
        }

        val serialSuspicious = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                false
            } else {
                @Suppress("DEPRECATION")
                val serial = Build.SERIAL
                serial.isNullOrBlank() || serial.equals("unknown", ignoreCase = true)
            }
        } catch (_: Throwable) {
            false
        }

        if (serialSuspicious) return true

        val emulatorFiles = arrayOf(
            "/dev/qemu_pipe",
            "/dev/socket/qemud",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
        )

        if (emulatorFiles.any { File(it).exists() }) return true

        return false
    }

    @JvmStatic
    fun isDebugging(): Boolean {
        return android.os.Debug.isDebuggerConnected() ||
            android.os.Debug.waitingForDebugger()
    }
}