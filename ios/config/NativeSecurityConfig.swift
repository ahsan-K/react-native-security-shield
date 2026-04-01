import Foundation

final class NativeSecurityConfig {
  static let shared = NativeSecurityConfig()

  private init() {}

  var isConfigured: Bool = false

  var bundleId: String?
  var teamId: String?
  var pins: [String] = []
  var allowedDomains: [String] = []

  var enableRootDetection: Bool = true
  var enableFridaDetection: Bool = true
  var enableDebuggerDetection: Bool = true
  var enableEmulatorDetection: Bool = true
  var enableScreenshotProtection: Bool = true
  var killOnThreat: Bool = true

  func reset() {
    isConfigured = false

    bundleId = GeneratedSecurityConfig.bundleId.trimmingCharacters(in: .whitespacesAndNewlines)
    teamId = GeneratedSecurityConfig.teamId.trimmingCharacters(in: .whitespacesAndNewlines)

    pins = GeneratedSecurityConfig.pins
      .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
      .filter { !$0.isEmpty }

    allowedDomains = GeneratedSecurityConfig.allowedDomains
      .map { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() }
      .filter { !$0.isEmpty }

    enableRootDetection = GeneratedSecurityConfig.enableRootDetection
    enableFridaDetection = GeneratedSecurityConfig.enableFridaDetection
    enableDebuggerDetection = GeneratedSecurityConfig.enableDebuggerDetection
    enableEmulatorDetection = GeneratedSecurityConfig.enableEmulatorDetection
    enableScreenshotProtection = GeneratedSecurityConfig.enableScreenshotProtection
    killOnThreat = GeneratedSecurityConfig.killOnThreat
  }

  func apply(config: NSDictionary) throws {
    reset()

    if let ios = config["ios"] as? NSDictionary {
      if let bundleId = ios["bundleId"] as? String {
        let value = bundleId.trimmingCharacters(in: .whitespacesAndNewlines)
        if !value.isEmpty {
          self.bundleId = value
        }
      }

      if let teamId = ios["teamId"] as? String {
        let value = teamId.trimmingCharacters(in: .whitespacesAndNewlines)
        if !value.isEmpty {
          self.teamId = value
        }
      }

      if let pins = ios["pins"] as? [String] {
        let values = pins
          .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
          .filter { !$0.isEmpty }

        if !values.isEmpty {
          self.pins = values
        }
      }

      if let allowedDomains = ios["allowedDomains"] as? [String] {
        let values = allowedDomains
          .map { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() }
          .filter { !$0.isEmpty }

        if !values.isEmpty {
          self.allowedDomains = values
        }
      }
    }

    if let options = config["options"] as? NSDictionary {
      if let value = options["enableRootDetection"] as? Bool { enableRootDetection = value }
      if let value = options["enableFridaDetection"] as? Bool { enableFridaDetection = value }
      if let value = options["enableDebuggerDetection"] as? Bool { enableDebuggerDetection = value }
      if let value = options["enableEmulatorDetection"] as? Bool { enableEmulatorDetection = value }
      if let value = options["enableScreenshotProtection"] as? Bool { enableScreenshotProtection = value }
      if let value = options["killOnThreat"] as? Bool { killOnThreat = value }
    }

    guard let bundleId, !bundleId.isEmpty else {
      throw NSError(
        domain: "SecurityShield",
        code: 1001,
        userInfo: [NSLocalizedDescriptionKey: "ios.bundleId is required"]
      )
    }

    guard let teamId, !teamId.isEmpty else {
      throw NSError(
        domain: "SecurityShield",
        code: 1002,
        userInfo: [NSLocalizedDescriptionKey: "ios.teamId is required"]
      )
    }

    isConfigured = true
  }
}