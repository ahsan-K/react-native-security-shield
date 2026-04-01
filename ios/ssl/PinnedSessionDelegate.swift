import Foundation
import CryptoKit

final class PinnedSessionDelegate: NSObject, URLSessionDelegate {

  func urlSession(_ session: URLSession,
                  didReceive challenge: URLAuthenticationChallenge,
                  completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {

    guard challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust,
          let serverTrust = challenge.protectionSpace.serverTrust,
          let serverCert = SecTrustGetCertificateAtIndex(serverTrust, 0) else {
      completionHandler(.performDefaultHandling, nil)
      return
    }

    let host = challenge.protectionSpace.host.lowercased()
    let config = NativeSecurityConfig.shared

    guard shouldPin(host: host, allowedDomains: config.allowedDomains) else {
      completionHandler(.useCredential, URLCredential(trust: serverTrust))
      return
    }

    guard let key = SecCertificateCopyKey(serverCert),
          let publicKeyData = SecKeyCopyExternalRepresentation(key, nil) as Data? else {
      completionHandler(.cancelAuthenticationChallenge, nil)
      return
    }

    let hash = Data(SHA256.hash(data: publicKeyData)).base64EncodedString()

    if config.pins.contains(hash) {
      completionHandler(.useCredential, URLCredential(trust: serverTrust))
    } else {
      completionHandler(.cancelAuthenticationChallenge, nil)
    }
  }

  private func shouldPin(host: String, allowedDomains: [String]) -> Bool {
    return allowedDomains.contains { allowed in
      host == allowed || host.hasSuffix(".\(allowed)")
    }
  }
}