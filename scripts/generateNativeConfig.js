#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

function ensureArray(value) {
  return Array.isArray(value) ? value : [];
}

function normalizeString(value) {
  return typeof value === "string" ? value.trim() : "";
}

function escapeKotlin(str) {
  return str.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}

function escapeSwift(str) {
  return str.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}

function buildKotlinFile(config) {
  const android = config.android || {};
  const options = config.options || {};

  const allowedDomains = ensureArray(android.allowedDomains)
    .map((v) => `"${escapeKotlin(normalizeString(v).toLowerCase())}"`)
    .join(", ");

  const pins = ensureArray(android.pins)
    .map((v) => `"${escapeKotlin(normalizeString(v))}"`)
    .join(", ");

  return `package com.securityshield

object GeneratedSecurityConfig {
    const val RELEASE_SHA256: String = "${escapeKotlin(normalizeString(android.releaseSha256).toLowerCase())}"
    const val PLAY_SIGNING_SHA256: String = "${escapeKotlin(normalizeString(android.playSigningSha256).toLowerCase())}"
    const val INTERNAL_SHARING_SHA256: String = "${escapeKotlin(normalizeString(android.internalSharingSha256).toLowerCase())}"

    val ALLOWED_DOMAINS: List<String> = listOf(${allowedDomains})
    val PINS: List<String> = listOf(${pins})

    const val ENABLE_ROOT_DETECTION: Boolean = ${options.enableRootDetection !== false}
    const val ENABLE_FRIDA_DETECTION: Boolean = ${options.enableFridaDetection !== false}
    const val ENABLE_DEBUGGER_DETECTION: Boolean = ${options.enableDebuggerDetection !== false}
    const val ENABLE_EMULATOR_DETECTION: Boolean = ${options.enableEmulatorDetection !== false}
    const val ENABLE_SCREENSHOT_PROTECTION: Boolean = ${options.enableScreenshotProtection !== false}
    const val KILL_ON_THREAT: Boolean = ${options.killOnThreat !== false}
}
`;
}

function buildSwiftFile(config) {
  const ios = config.ios || {};
  const options = config.options || {};

  const allowedDomains = ensureArray(ios.allowedDomains)
    .map((v) => `"${escapeSwift(normalizeString(v).toLowerCase())}"`)
    .join(", ");

  const pins = ensureArray(ios.pins)
    .map((v) => `"${escapeSwift(normalizeString(v))}"`)
    .join(", ");

  return `import Foundation

enum GeneratedSecurityConfig {
  static let bundleId: String = "${escapeSwift(normalizeString(ios.bundleId))}"
  static let teamId: String = "${escapeSwift(normalizeString(ios.teamId))}"
  static let allowedDomains: [String] = [${allowedDomains}]
  static let pins: [String] = [${pins}]

  static let enableRootDetection: Bool = ${options.enableRootDetection !== false}
  static let enableFridaDetection: Bool = ${options.enableFridaDetection !== false}
  static let enableDebuggerDetection: Bool = ${options.enableDebuggerDetection !== false}
  static let enableEmulatorDetection: Bool = ${options.enableEmulatorDetection !== false}
  static let enableScreenshotProtection: Bool = ${options.enableScreenshotProtection !== false}
  static let killOnThreat: Bool = ${options.killOnThreat !== false}
}
`;
}

function main() {
  const hostRoot = process.cwd();
  const packageRoot = path.resolve(__dirname, "..");

  const configPath = path.join(hostRoot, "securityshield.config.json");

  if (!fs.existsSync(configPath)) {
    throw new Error(`Missing config file: ${configPath}`);
  }

  const raw = fs.readFileSync(configPath, "utf8");
  const config = JSON.parse(raw);

  const androidOut = path.join(
    packageRoot,
    "android",
    "src",
    "main",
    "java",
    "com",
    "securityshield",
    "config",
    "GeneratedSecurityConfig.kt"
  );

  const iosOut = path.join(
    packageRoot,
    "ios",
    "config",
    "GeneratedSecurityConfig.swift"
  );

  fs.mkdirSync(path.dirname(androidOut), { recursive: true });
  fs.mkdirSync(path.dirname(iosOut), { recursive: true });

  fs.writeFileSync(androidOut, buildKotlinFile(config), "utf8");
  fs.writeFileSync(iosOut, buildSwiftFile(config), "utf8");

  console.log("Generated native config files:");
  console.log(`- ${androidOut}`);
  console.log(`- ${iosOut}`);
}

try {
  main();
} catch (err) {
  console.error("Error:", err.message);
  process.exit(1);
}