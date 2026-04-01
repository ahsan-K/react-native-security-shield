#include <jni.h>
#include <cstring>
#include <string>
#include <vector>

// ------------------------------------------------------------
// Constant-time compare
// ------------------------------------------------------------
static bool constantTimeEquals(const char* a, const char* b) {
    if (!a || !b) return false;

    size_t lenA = strlen(a);
    size_t lenB = strlen(b);

    if (lenA != lenB) return false;

    volatile unsigned char diff = 0;
    for (size_t i = 0; i < lenA; ++i) {
        diff |= static_cast<unsigned char>(a[i] ^ b[i]);
    }

    return diff == 0;
}

// ------------------------------------------------------------
// XOR decode helper for future secure mode
// ------------------------------------------------------------
static std::string xorDecode(const uint8_t* data, size_t len, uint8_t key) {
    std::string out;
    out.resize(len);

    for (size_t i = 0; i < len; ++i) {
        out[i] = static_cast<char>(data[i] ^ key);
    }

    return out;
}

// ------------------------------------------------------------
// Secure-mode placeholders
// Later, CLI generated pins will be placed here.
// For now, empty arrays mean native fallback is disabled.
// ------------------------------------------------------------
static const uint8_t PIN_KEY = 0x5A;

static const uint8_t PIN1_XOR[] = {};
static const uint8_t PIN2_XOR[] = {};

static bool hasNativePins() {
    return sizeof(PIN1_XOR) > 0 || sizeof(PIN2_XOR) > 0;
}

static bool isPinnedHashAllowed(const char* serverHashBase64) {
    if (!serverHashBase64) return false;
    if (!hasNativePins()) return false;

    if (sizeof(PIN1_XOR) > 0) {
        std::string pin1 = xorDecode(PIN1_XOR, sizeof(PIN1_XOR), PIN_KEY);
        if (constantTimeEquals(serverHashBase64, pin1.c_str())) {
            return true;
        }
    }

    if (sizeof(PIN2_XOR) > 0) {
        std::string pin2 = xorDecode(PIN2_XOR, sizeof(PIN2_XOR), PIN_KEY);
        if (constantTimeEquals(serverHashBase64, pin2.c_str())) {
            return true;
        }
    }

    return false;
}

// ------------------------------------------------------------
// JNI export
// ------------------------------------------------------------
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_securityshield_NativeIntegrity_verifyPublicKeyHash(
    JNIEnv* env,
    jclass,
    jstring serverHash_) {

    if (serverHash_ == nullptr) {
        return JNI_FALSE;
    }

    const char* serverHash = env->GetStringUTFChars(serverHash_, nullptr);
    bool ok = isPinnedHashAllowed(serverHash);
    env->ReleaseStringUTFChars(serverHash_, serverHash);

    return ok ? JNI_TRUE : JNI_FALSE;
}