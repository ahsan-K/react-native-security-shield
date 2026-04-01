package com.securityshield

import android.app.Activity
import android.os.Process
import android.view.WindowManager
import com.facebook.react.bridge.*

class SecurityShieldModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "SecurityShield"

    @ReactMethod
    fun configure(config: ReadableMap, promise: Promise) {
        try {
            SecurityConfigParser.apply(config)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("ERR_CONFIGURE", e.message, e)
        }
    }

    @ReactMethod
    fun start(promise: Promise) {
        try {
            val result = SecurityEvaluator.evaluate(reactContext)
            applyRuntimeProtection(result)
            promise.resolve(result.safe)
        } catch (e: Exception) {
            promise.reject("ERR_START", e.message, e)
        }
    }

    @ReactMethod
    fun runSecurityCheck(promise: Promise) {
        try {
            val result = SecurityEvaluator.evaluate(reactContext)
            promise.resolve(result.toWritableMap())
        } catch (e: Exception) {
            promise.reject("ERR_RUN_SECURITY_CHECK", e.message, e)
        }
    }

    @ReactMethod
    fun isConfigured(promise: Promise) {
        promise.resolve(SecurityConfig.isConfigured)
    }

    @ReactMethod
    fun isEmulator(promise: Promise) {
        try {
            promise.resolve(EmulatorDetection.isRunningOnEmulator())
        } catch (e: Exception) {
            promise.reject("ERR_EMULATOR", e.message, e)
        }
    }

    @ReactMethod
    fun isDebugging(promise: Promise) {
        try {
            promise.resolve(EmulatorDetection.isDebugging())
        } catch (e: Exception) {
            promise.reject("ERR_DEBUGGING", e.message, e)
        }
    }

    @ReactMethod
    fun isFridaDetected(promise: Promise) {
        try {
            promise.resolve(SecurityDetection.isFridaDetected())
        } catch (e: Exception) {
            promise.reject("ERR_FRIDA", e.message, e)
        }
    }

    @ReactMethod
    fun isXposedDetected(promise: Promise) {
        try {
            promise.resolve(SecurityDetection.isXposedDetected())
        } catch (e: Exception) {
            promise.reject("ERR_XPOSED", e.message, e)
        }
    }

    @ReactMethod
    fun isRooted(promise: Promise) {
        try {
            promise.resolve(SecurityDetection.isRooted())
        } catch (e: Exception) {
            promise.reject("ERR_ROOT", e.message, e)
        }
    }

    @ReactMethod
    fun setSecureFlag(enable: Boolean) {
        val activity: Activity? = currentActivity
        activity?.runOnUiThread {
            if (enable) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    @ReactMethod
    fun exitApp() {
        val activity: Activity? = currentActivity
        activity?.runOnUiThread {
            try {
                activity.finishAffinity()
                activity.finish()
            } catch (_: Exception) {
            }

            try {
                Process.killProcess(Process.myPid())
            } catch (_: Exception) {
            }

            try {
                System.exit(0)
            } catch (_: Exception) {
            }
        }
    }

    private fun applyRuntimeProtection(result: SecurityResult) {
        if (!result.safe && SecurityConfig.killOnThreat) {
            exitApp()
            return
        }

        if (result.safe && SecurityConfig.enableScreenshotProtection) {
            setSecureFlag(true)
        }
    }
}