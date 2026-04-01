package com.securityshield

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

data class SecurityResult(
    val configured: Boolean,
    val signatureValid: Boolean,
    val emulatorDetected: Boolean,
    val debuggingDetected: Boolean,
    val fridaDetected: Boolean,
    val xposedDetected: Boolean,
    val rootedDetected: Boolean,
    val safe: Boolean
) {
    fun toWritableMap(): WritableMap {
        return Arguments.createMap().apply {
            putBoolean("configured", configured)
            putBoolean("signatureValid", signatureValid)
            putBoolean("emulatorDetected", emulatorDetected)
            putBoolean("debuggingDetected", debuggingDetected)
            putBoolean("fridaDetected", fridaDetected)
            putBoolean("xposedDetected", xposedDetected)
            putBoolean("rootedDetected", rootedDetected)
            putBoolean("safe", safe)
        }
    }
}