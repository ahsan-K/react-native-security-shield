import Foundation

enum SecurityEvaluator {
  static func evaluate() -> [String: Any] {
    let config = NativeSecurityConfig.shared
    let configured = config.isConfigured

    let signatureValid = configured ? isIdentityValid() : false

    let emulatorDetected = config.enableEmulatorDetection
      ? EmulatorDetection.isEmulatorOrJailbroken()
      : false

    let debuggingDetected = config.enableDebuggerDetection
      ? EmulatorDetection.isDebugging()
      : false

    let fridaDetected = config.enableFridaDetection
      ? SecurityDetection.isFridaDetected()
      : false

    let xposedDetected = false
    let rootedDetected = false

    let safe = configured &&
      signatureValid &&
      !emulatorDetected &&
      !debuggingDetected &&
      !fridaDetected &&
      !xposedDetected &&
      !rootedDetected

    return [
      "configured": configured,
      "signatureValid": signatureValid,
      "emulatorDetected": emulatorDetected,
      "debuggingDetected": debuggingDetected,
      "fridaDetected": fridaDetected,
      "xposedDetected": xposedDetected,
      "rootedDetected": rootedDetected,
      "safe": safe
    ]
  }

  private static func isIdentityValid() -> Bool {
    let config = NativeSecurityConfig.shared

    guard let expectedBundleId = config.bundleId,
          let actualBundleId = Bundle.main.bundleIdentifier,
          actualBundleId == expectedBundleId else {
      return false
    }

    guard let expectedTeamId = config.teamId else {
      return false
    }

    return currentTeamIdentifier() == expectedTeamId
  }

  private static func currentTeamIdentifier() -> String? {
    guard let path = Bundle.main.path(forResource: "embedded", ofType: "mobileprovision"),
          let data = try? Data(contentsOf: URL(fileURLWithPath: path)),
          let content = String(data: data, encoding: .isoLatin1) else {
      return nil
    }

    guard let startRange = content.range(of: "<?xml"),
          let endRange = content.range(of: "</plist>") else {
      return nil
    }

    let plistText = String(content[startRange.lowerBound..<endRange.upperBound])
    guard let plistData = plistText.data(using: .utf8),
          let plist = try? PropertyListSerialization.propertyList(from: plistData, options: [], format: nil) as? [String: Any],
          let entitlements = plist["Entitlements"] as? [String: Any],
          let appIdentifier = entitlements["application-identifier"] as? String else {
      return nil
    }

    return appIdentifier.components(separatedBy: ".").first
  }
}