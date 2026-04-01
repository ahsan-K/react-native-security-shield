import Foundation
import UIKit
import React

@objc(SecurityShield)
class SecurityShield: NSObject {

  @objc
  func configure(_ config: NSDictionary,
                 resolver resolve: RCTPromiseResolveBlock,
                 rejecter reject: RCTPromiseRejectBlock) {
    do {
      try NativeSecurityConfig.shared.apply(config: config)
      resolve(true)
    } catch {
      reject("ERR_CONFIGURE", error.localizedDescription, error)
    }
  }

  @objc
  func start(_ resolve: RCTPromiseResolveBlock,
             rejecter reject: RCTPromiseRejectBlock) {
    do {
      let result = SecurityEvaluator.evaluate()
      applyRuntimeProtection(result: result)
      resolve(result["safe"] as? Bool ?? false)
    } catch {
      reject("ERR_START", error.localizedDescription, error)
    }
  }

  @objc
  func runSecurityCheck(_ resolve: RCTPromiseResolveBlock,
                        rejecter reject: RCTPromiseRejectBlock) {
    let result = SecurityEvaluator.evaluate()
    resolve(result)
  }

  @objc
  func isConfigured(_ resolve: RCTPromiseResolveBlock,
                    rejecter reject: RCTPromiseRejectBlock) {
    resolve(NativeSecurityConfig.shared.isConfigured)
  }

  @objc
  func isEmulator(_ resolve: RCTPromiseResolveBlock,
                  rejecter reject: RCTPromiseRejectBlock) {
    resolve(EmulatorDetection.isEmulatorOrJailbroken())
  }

  @objc
  func isDebugging(_ resolve: RCTPromiseResolveBlock,
                   rejecter reject: RCTPromiseRejectBlock) {
    resolve(EmulatorDetection.isDebugging())
  }

  @objc
  func isFridaDetected(_ resolve: RCTPromiseResolveBlock,
                       rejecter reject: RCTPromiseRejectBlock) {
    resolve(SecurityDetection.isFridaDetected())
  }

  @objc
  func isXposedDetected(_ resolve: RCTPromiseResolveBlock,
                        rejecter reject: RCTPromiseRejectBlock) {
    resolve(false)
  }

  @objc
  func isRooted(_ resolve: RCTPromiseResolveBlock,
                rejecter reject: RCTPromiseRejectBlock) {
    resolve(false)
  }

  @objc
  func setSecureFlag(_ enable: Bool) {
    ActivityStarter().setSecureFlag(enable)
  }

  @objc
  func exitApp() {
    AppExit().exitApp()
  }

  @objc
  static func requiresMainQueueSetup() -> Bool {
    return true
  }

  private func applyRuntimeProtection(result: [String: Any]) {
    let safe = result["safe"] as? Bool ?? false
    let killOnThreat = NativeSecurityConfig.shared.killOnThreat
    let screenshotProtection = NativeSecurityConfig.shared.enableScreenshotProtection

    if !safe && killOnThreat {
      AppExit().exitApp()
      return
    }

    if safe && screenshotProtection {
      ActivityStarter().setSecureFlag(true)
    }
  }
}