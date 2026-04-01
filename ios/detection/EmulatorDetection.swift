import Foundation
import UIKit

enum EmulatorDetection {
  static func isEmulatorOrJailbroken() -> Bool {
    #if targetEnvironment(simulator)
    return true
    #else
    return isJailbroken()
    #endif
  }

  static func isDebugging() -> Bool {
    var name: [Int32] = [CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid()]
    var info = kinfo_proc()
    var size = MemoryLayout<kinfo_proc>.stride

    let result = sysctl(&name, UInt32(name.count), &info, &size, nil, 0)
    return result == 0 && (info.kp_proc.p_flag & P_TRACED) != 0
  }

  static func isJailbroken() -> Bool {
    if hasSuspiciousFiles() { return true }
    if canOpenCydia() { return true }
    if canWriteOutsideSandbox() { return true }
    if hasInjectedLibraries() { return true }
    return false
  }

  private static func hasSuspiciousFiles() -> Bool {
    let paths = [
      "/Applications/Cydia.app",
      "/Library/MobileSubstrate/MobileSubstrate.dylib",
      "/bin/bash",
      "/usr/sbin/sshd",
      "/etc/apt",
      "/private/var/lib/apt/",
      "/var/lib/cydia",
      "/usr/bin/ssh"
    ]

    return paths.contains { FileManager.default.fileExists(atPath: $0) }
  }

  private static func canOpenCydia() -> Bool {
    guard let url = URL(string: "cydia://package/com.example") else { return false }
    return UIApplication.shared.canOpenURL(url)
  }

  private static func canWriteOutsideSandbox() -> Bool {
    let testPath = "/private/securityshield_jb_test.txt"

    do {
      try "test".write(toFile: testPath, atomically: true, encoding: .utf8)
      try? FileManager.default.removeItem(atPath: testPath)
      return true
    } catch {
      return false
    }
  }

  private static func hasInjectedLibraries() -> Bool {
    guard let value = getenv("DYLD_INSERT_LIBRARIES") else { return false }
    return String(cString: value).isEmpty == false
  }
}