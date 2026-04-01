#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <string>
#include <unistd.h>
#include <sys/system_properties.h>

// ------------------------------------------------------------
// Helpers
// ------------------------------------------------------------
static bool fileExists(const char* path) {
    return access(path, F_OK) == 0;
}

static std::string getSystemProp(const char* key) {
    char value[PROP_VALUE_MAX] = {0};
    __system_property_get(key, value);
    return std::string(value);
}

static bool contains(const std::string& text, const char* keyword) {
    return text.find(keyword) != std::string::npos;
}

// ------------------------------------------------------------
// Frida / instrumentation detection
// ------------------------------------------------------------
static bool detectFridaMaps() {
    FILE* f = fopen("/proc/self/maps", "r");
    if (!f) return false;

    char line[512];
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, "frida") ||
            strstr(line, "re.frida.server") ||
            strstr(line, "frida-agent") ||
            strstr(line, "libfrida-gadget.so") ||
            strstr(line, "gum-js-loop") ||
            strstr(line, "gadget") ||
            strstr(line, "libfrida") ||
            strstr(line, "linjector") ||
            strstr(line, "xposed") ||
            strstr(line, "substrate")) {
            fclose(f);
            return true;
        }
    }

    fclose(f);
    return false;
}

static bool detectFridaPorts() {
    FILE* f = fopen("/proc/net/tcp", "r");
    if (!f) return false;

    char line[512];
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, ":6992") || strstr(line, ":6993")) {
            fclose(f);
            return true;
        }
    }

    fclose(f);
    return false;
}

static bool detectFridaCmdline() {
    const char* keywords[] = {
        "frida",
        "frida-server",
        "re.frida.server",
        "re.frida",
        "frida-agent",
        "libfrida-gadget.so",
        "gum-js-loop",
        "linjector"
    };

    FILE* p = popen("ps -A", "r");
    if (!p) return false;

    char line[256];
    while (fgets(line, sizeof(line), p)) {
        for (const char* k : keywords) {
            if (strstr(line, k)) {
                pclose(p);
                return true;
            }
        }
    }

    pclose(p);
    return false;
}

// ------------------------------------------------------------
// Debugger detection
// ------------------------------------------------------------
static bool detectDebuggerTracerPid() {
    FILE* f = fopen("/proc/self/status", "r");
    if (!f) return false;

    char line[256];
    while (fgets(line, sizeof(line), f)) {
        if (strncmp(line, "TracerPid:", 10) == 0) {
            int tracer = atoi(line + 10);
            fclose(f);
            return tracer != 0;
        }
    }

    fclose(f);
    return false;
}

// ------------------------------------------------------------
// Emulator detection
// ------------------------------------------------------------
static bool detectEmulatorNative() {
    if (fileExists("/dev/qemu_pipe")) return true;
    if (fileExists("/dev/qemu_trace")) return true;
    if (fileExists("/system/lib/libc_malloc_debug_qemu.so")) return true;
    if (fileExists("/sys/qemu_trace")) return true;

    std::string roKernelQemu = getSystemProp("ro.kernel.qemu");
    if (roKernelQemu == "1") return true;

    std::string roHardware = getSystemProp("ro.hardware");
    if (contains(roHardware, "goldfish") ||
        contains(roHardware, "ranchu") ||
        contains(roHardware, "vbox86") ||
        contains(roHardware, "qemu")) {
        return true;
    }

    std::string roProduct = getSystemProp("ro.product.device");
    if (contains(roProduct, "generic") ||
        contains(roProduct, "emulator") ||
        contains(roProduct, "sdk")) {
        return true;
    }

    std::string roModel = getSystemProp("ro.product.model");
    if (contains(roModel, "Android SDK built for") ||
        contains(roModel, "Emulator") ||
        contains(roModel, "sdk_gphone")) {
        return true;
    }

    std::string roManufacturer = getSystemProp("ro.product.manufacturer");
    if (contains(roManufacturer, "Genymotion") || contains(roManufacturer, "unknown")) {
        return true;
    }

    std::string roBrand = getSystemProp("ro.product.brand");
    std::string roName = getSystemProp("ro.product.name");
    if (contains(roBrand, "generic") || contains(roName, "generic")) {
        return true;
    }

    return false;
}

// ------------------------------------------------------------
// JNI exports
// ------------------------------------------------------------
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_securityshield_NativeIntegrity_isFridaDetectedNative(
    JNIEnv*,
    jclass) {
    bool detected =
        detectFridaMaps() ||
        detectFridaPorts() ||
        detectFridaCmdline();

    return detected ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_securityshield_NativeIntegrity_isDebuggerDetectedNative(
    JNIEnv*,
    jclass) {
    return detectDebuggerTracerPid() ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_securityshield_NativeIntegrity_isEmulatorDetectedNative(
    JNIEnv*,
    jclass) {
    return detectEmulatorNative() ? JNI_TRUE : JNI_FALSE;
}