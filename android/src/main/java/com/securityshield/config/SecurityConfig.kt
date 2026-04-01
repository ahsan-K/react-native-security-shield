package com.securityshield

object SecurityConfig {
    @Volatile var isConfigured: Boolean = false

    @Volatile var releaseSha256: String? = null
    @Volatile var playSigningSha256: String? = null
    @Volatile var internalSharingSha256: String? = null

    @Volatile var allowedDomains: List<String> = emptyList()
    @Volatile var pins: List<String> = emptyList()

    @Volatile var enableRootDetection: Boolean = true
    @Volatile var enableFridaDetection: Boolean = true
    @Volatile var enableDebuggerDetection: Boolean = true
    @Volatile var enableEmulatorDetection: Boolean = true
    @Volatile var enableScreenshotProtection: Boolean = true
    @Volatile var killOnThreat: Boolean = true

    fun reset() {
        isConfigured = false
        releaseSha256 = null
        playSigningSha256 = null
        internalSharingSha256 = null
        allowedDomains = emptyList()
        pins = emptyList()
        enableRootDetection = true
        enableFridaDetection = true
        enableDebuggerDetection = true
        enableEmulatorDetection = true
        enableScreenshotProtection = true
        killOnThreat = true
    }
}