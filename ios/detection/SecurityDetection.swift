import Foundation
import MachO.dyld

enum SecurityDetection {
  static func isFridaDetected() -> Bool {
    let suspiciousLibs = [
      "frida",
      "frida-gadget",
      "frida-agent",
      "libfrida",
      "re.frida.server",
      "gum-js-loop",
      "linjector"
    ]

    for i in 0..<_dyld_image_count() {
      guard let name = _dyld_get_image_name(i) else { continue }
      let lib = String(cString: name).lowercased()

      if suspiciousLibs.contains(where: { lib.contains($0) }) {
        return true
      }
    }

    if let value = getenv("DYLD_INSERT_LIBRARIES") {
      let injected = String(cString: value).lowercased()
      if !injected.isEmpty {
        return true
      }
    }

    return false
  }
}