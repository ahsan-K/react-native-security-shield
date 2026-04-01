package com.securityshield

import android.content.Context

object SecurityEvaluator {

    fun evaluate(context: Context): SecurityResult {
        val configured = SecurityConfig.isConfigured
        val signatureValid = if (configured) isSignatureValid(context) else false

        val emulatorDetected = if (SecurityConfig.enableEmulatorDetection) {
            EmulatorDetection.isRunningOnEmulator() || NativeIntegrity.isEmulatorDetectedNative()
        } else {
            false
        }

        val debuggingDetected = if (SecurityConfig.enableDebuggerDetection) {
            EmulatorDetection.isDebugging() || NativeIntegrity.isDebuggerDetectedNative()
        } else {
            false
        }

        val fridaDetected = if (SecurityConfig.enableFridaDetection) {
            SecurityDetection.isFridaDetected() || NativeIntegrity.isFridaDetectedNative()
        } else {
            false
        }

        val xposedDetected = if (SecurityConfig.enableFridaDetection) {
            SecurityDetection.isXposedDetected()
        } else {
            false
        }

        val rootedDetected = if (SecurityConfig.enableRootDetection) {
            SecurityDetection.isRooted()
        } else {
            false
        }

        val safe = configured &&
            signatureValid &&
            !emulatorDetected &&
            !debuggingDetected &&
            !fridaDetected &&
            !xposedDetected &&
            !rootedDetected

        return SecurityResult(
            configured = configured,
            signatureValid = signatureValid,
            emulatorDetected = emulatorDetected,
            debuggingDetected = debuggingDetected,
            fridaDetected = fridaDetected,
            xposedDetected = xposedDetected,
            rootedDetected = rootedDetected,
            safe = safe
        )
    }

    private fun isSignatureValid(context: Context): Boolean {
        val currentSig = SignatureUtil.getAppSignatureSha256Hex(context).trim().lowercase()

        val allowed = listOfNotNull(
            SecurityConfig.releaseSha256?.trim()?.lowercase(),
            SecurityConfig.playSigningSha256?.trim()?.lowercase(),
            SecurityConfig.internalSharingSha256?.trim()?.lowercase()
        ).filter { it.isNotBlank() }

        if (allowed.isEmpty()) return false

        return allowed.any { it == currentSig }
    }
}