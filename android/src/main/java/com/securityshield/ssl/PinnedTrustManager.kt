package com.securityshield

import android.util.Base64
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class PinnedTrustManager(
    private val defaultTm: X509TrustManager
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        defaultTm.checkClientTrusted(chain, authType)
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        defaultTm.checkServerTrusted(chain, authType)

        if (chain.isEmpty()) {
            throw CertificateException("Empty server certificate chain")
        }

        if (SecurityConfig.allowedDomains.isEmpty() || SecurityConfig.pins.isEmpty()) {
            throw CertificateException("SecurityShield pinning config is missing")
        }

        val leafCert = chain[0]

        if (!shouldPinCertificate(leafCert)) {
            return
        }

        val pubKeyBytes = leafCert.publicKey.encoded
        val digest = MessageDigest.getInstance("SHA-256").digest(pubKeyBytes)
        val base64 = Base64.encodeToString(digest, Base64.NO_WRAP)

        val matched = isPinMatched(base64)

        if (!matched) {
            throw CertificateException("Public key pin mismatch")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return defaultTm.acceptedIssuers
    }

    private fun isPinMatched(serverHashBase64: String): Boolean {
        if (SecurityConfig.pins.any { it == serverHashBase64 }) {
            return true
        }

        return NativeIntegrity.verifyPublicKeyHash(serverHashBase64)
    }

    private fun shouldPinCertificate(cert: X509Certificate): Boolean {
        val sanDomains = mutableListOf<String>()

        try {
            val altNames = cert.subjectAlternativeNames
            if (altNames != null) {
                for (entry in altNames) {
                    val type = entry[0] as? Int ?: continue
                    val value = entry[1] as? String ?: continue

                    if (type == 2) {
                        sanDomains.add(value.lowercase())
                    }
                }
            }
        } catch (_: Throwable) {
        }

        if (sanDomains.isEmpty()) {
            val dn = cert.subjectX500Principal?.name?.lowercase().orEmpty()
            return SecurityConfig.allowedDomains.any { dn.contains("cn=$it") }
        }

        return sanDomains.any { dns ->
            SecurityConfig.allowedDomains.any { allowed ->
                dns.equals(allowed, ignoreCase = true) ||
                    dns.endsWith(".$allowed", ignoreCase = true)
            }
        }
    }
}