package com.securityshield

object NativeIntegrity {

    init {
        System.loadLibrary("securityshield")
    }

    @JvmStatic
    external fun verifyPublicKeyHash(serverHash: String): Boolean

    @JvmStatic
    external fun isDebuggerDetectedNative(): Boolean

    @JvmStatic
    external fun isFridaDetectedNative(): Boolean

    @JvmStatic
    external fun isEmulatorDetectedNative(): Boolean
}