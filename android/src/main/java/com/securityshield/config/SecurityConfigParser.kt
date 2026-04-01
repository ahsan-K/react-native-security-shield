package com.securityshield

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

object SecurityConfigParser {

    fun apply(config: ReadableMap) {
        SecurityConfig.reset()

        applyGeneratedDefaults()
        applyRuntimeConfig(config)

        SecurityConfig.isConfigured = hasMinimumConfiguration()
    }

    private fun applyGeneratedDefaults() {
        SecurityConfig.releaseSha256 =
            GeneratedSecurityConfig.RELEASE_SHA256.trim().lowercase().ifBlank { null }

        SecurityConfig.playSigningSha256 =
            GeneratedSecurityConfig.PLAY_SIGNING_SHA256.trim().lowercase().ifBlank { null }

        SecurityConfig.internalSharingSha256 =
            GeneratedSecurityConfig.INTERNAL_SHARING_SHA256.trim().lowercase().ifBlank { null }

        SecurityConfig.allowedDomains =
            GeneratedSecurityConfig.ALLOWED_DOMAINS
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }

        SecurityConfig.pins =
            GeneratedSecurityConfig.PINS
                .map { it.trim() }
                .filter { it.isNotBlank() }

        SecurityConfig.enableRootDetection = GeneratedSecurityConfig.ENABLE_ROOT_DETECTION
        SecurityConfig.enableFridaDetection = GeneratedSecurityConfig.ENABLE_FRIDA_DETECTION
        SecurityConfig.enableDebuggerDetection = GeneratedSecurityConfig.ENABLE_DEBUGGER_DETECTION
        SecurityConfig.enableEmulatorDetection = GeneratedSecurityConfig.ENABLE_EMULATOR_DETECTION
        SecurityConfig.enableScreenshotProtection = GeneratedSecurityConfig.ENABLE_SCREENSHOT_PROTECTION
        SecurityConfig.killOnThreat = GeneratedSecurityConfig.KILL_ON_THREAT
    }

    private fun applyRuntimeConfig(config: ReadableMap) {
        if (config.hasKey("android") && !config.isNull("android")) {
            val android = config.getMap("android")
            if (android != null) {
                val releaseSha256 = android.getString("releaseSha256")?.trim()?.lowercase()
                if (!releaseSha256.isNullOrBlank()) {
                    SecurityConfig.releaseSha256 = releaseSha256
                }

                val playSigningSha256 = android.getString("playSigningSha256")?.trim()?.lowercase()
                if (!playSigningSha256.isNullOrBlank()) {
                    SecurityConfig.playSigningSha256 = playSigningSha256
                }

                val internalSharingSha256 = android.getString("internalSharingSha256")?.trim()?.lowercase()
                if (!internalSharingSha256.isNullOrBlank()) {
                    SecurityConfig.internalSharingSha256 = internalSharingSha256
                }

                val allowedDomains = toStringList(android.getArray("allowedDomains"))
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }

                if (allowedDomains.isNotEmpty()) {
                    SecurityConfig.allowedDomains = allowedDomains
                }

                val pins = toStringList(android.getArray("pins"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (pins.isNotEmpty()) {
                    SecurityConfig.pins = pins
                }
            }
        }

        if (config.hasKey("options") && !config.isNull("options")) {
            val options = config.getMap("options")
            if (options != null) {
                if (options.hasKey("enableRootDetection") && !options.isNull("enableRootDetection")) {
                    SecurityConfig.enableRootDetection = options.getBoolean("enableRootDetection")
                }
                if (options.hasKey("enableFridaDetection") && !options.isNull("enableFridaDetection")) {
                    SecurityConfig.enableFridaDetection = options.getBoolean("enableFridaDetection")
                }
                if (options.hasKey("enableDebuggerDetection") && !options.isNull("enableDebuggerDetection")) {
                    SecurityConfig.enableDebuggerDetection = options.getBoolean("enableDebuggerDetection")
                }
                if (options.hasKey("enableEmulatorDetection") && !options.isNull("enableEmulatorDetection")) {
                    SecurityConfig.enableEmulatorDetection = options.getBoolean("enableEmulatorDetection")
                }
                if (options.hasKey("enableScreenshotProtection") && !options.isNull("enableScreenshotProtection")) {
                    SecurityConfig.enableScreenshotProtection = options.getBoolean("enableScreenshotProtection")
                }
                if (options.hasKey("killOnThreat") && !options.isNull("killOnThreat")) {
                    SecurityConfig.killOnThreat = options.getBoolean("killOnThreat")
                }
            }
        }
    }

    private fun hasMinimumConfiguration(): Boolean {
        val hasAnySignature = listOfNotNull(
            SecurityConfig.releaseSha256,
            SecurityConfig.playSigningSha256,
            SecurityConfig.internalSharingSha256
        ).any { it.isNotBlank() }

        return hasAnySignature
    }

    private fun toStringList(array: ReadableArray?): List<String> {
        if (array == null) return emptyList()

        val result = mutableListOf<String>()
        for (i in 0 until array.size()) {
            array.getString(i)?.let { result.add(it) }
        }
        return result
    }
}