import { Platform } from "react-native";
import type { SecurityShieldConfig } from "./types";

function isNonEmptyString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function isStringArray(value: unknown): value is string[] {
  return Array.isArray(value) && value.every((item) => typeof item === "string");
}

export function validateSecurityConfig(config: SecurityShieldConfig): void {
  if (!config || typeof config !== "object") {
    throw new Error("SecurityShield config must be an object.");
  }

  if (Platform.OS === "android") {
    const android = config.android;
    if (!android || typeof android !== "object") {
      throw new Error("Android config is required on Android.");
    }

    const hasAnySignature =
      isNonEmptyString(android.releaseSha256) ||
      isNonEmptyString(android.playSigningSha256) ||
      isNonEmptyString(android.internalSharingSha256);

    if (!hasAnySignature) {
      throw new Error(
        "At least one Android signature SHA-256 is required: releaseSha256, playSigningSha256, or internalSharingSha256."
      );
    }

    if (android.allowedDomains && !isStringArray(android.allowedDomains)) {
      throw new Error("android.allowedDomains must be an array of strings.");
    }

    if (android.pins && !isStringArray(android.pins)) {
      throw new Error("android.pins must be an array of strings.");
    }
  }

  if (Platform.OS === "ios") {
    const ios = config.ios;
    if (!ios || typeof ios !== "object") {
      throw new Error("iOS config is required on iOS.");
    }

    if (!isNonEmptyString(ios.bundleId)) {
      throw new Error("ios.bundleId is required on iOS.");
    }

    if (!isNonEmptyString(ios.teamId)) {
      throw new Error("ios.teamId is required on iOS.");
    }

    if (ios.allowedDomains && !isStringArray(ios.allowedDomains)) {
      throw new Error("ios.allowedDomains must be an array of strings.");
    }

    if (ios.pins && !isStringArray(ios.pins)) {
      throw new Error("ios.pins must be an array of strings.");
    }
  }
}

export function normalizeConfig(config: SecurityShieldConfig): SecurityShieldConfig {
  return {
    android: config.android
      ? {
          releaseSha256: config.android.releaseSha256?.trim()?.toLowerCase(),
          playSigningSha256: config.android.playSigningSha256?.trim()?.toLowerCase(),
          internalSharingSha256: config.android.internalSharingSha256?.trim()?.toLowerCase(),
          pins: (config.android.pins || []).map((item) => item.trim()).filter(Boolean),
          allowedDomains: (config.android.allowedDomains || [])
            .map((item) => item.trim().toLowerCase())
            .filter(Boolean),
        }
      : undefined,
    ios: config.ios
      ? {
          bundleId: config.ios.bundleId?.trim(),
          teamId: config.ios.teamId?.trim(),
          pins: (config.ios.pins || []).map((item) => item.trim()).filter(Boolean),
          allowedDomains: (config.ios.allowedDomains || [])
            .map((item) => item.trim().toLowerCase())
            .filter(Boolean),
        }
      : undefined,
    options: {
      enableRootDetection: config.options?.enableRootDetection ?? true,
      enableFridaDetection: config.options?.enableFridaDetection ?? true,
      enableDebuggerDetection: config.options?.enableDebuggerDetection ?? true,
      enableEmulatorDetection: config.options?.enableEmulatorDetection ?? true,
      enableScreenshotProtection: config.options?.enableScreenshotProtection ?? true,
      killOnThreat: config.options?.killOnThreat ?? true,
    },
  };
}