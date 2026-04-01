import { NativeModules, Platform } from "react-native";
import type { SecurityCheckResult, SecurityShieldConfig } from "./types";
import { normalizeConfig, validateSecurityConfig } from "./utils";

const LINKING_ERROR =
  `The package 'react-native-security-shield' doesn't seem to be linked properly.\n\n` +
  Platform.select({
    ios: "Run 'pod install' inside the ios directory.\n",
    default: "",
  }) +
  "Rebuild the app after installing the package.";

const NativeSecurityShield = NativeModules.SecurityShield
  ? NativeModules.SecurityShield
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

class SecurityShieldManager {
  private configured = false;

  async configure(config: SecurityShieldConfig): Promise<boolean> {
    validateSecurityConfig(config);
    const normalized = normalizeConfig(config);
    const result = await NativeSecurityShield.configure(normalized);
    this.configured = true;
    return !!result;
  }

  async start(): Promise<boolean> {
    this.ensureConfigured();
    const result = await NativeSecurityShield.start();
    return !!result;
  }

  async runSecurityCheck(): Promise<SecurityCheckResult> {
    this.ensureConfigured();
    return await NativeSecurityShield.runSecurityCheck();
  }

  async isConfigured(): Promise<boolean> {
    return await NativeSecurityShield.isConfigured();
  }

  async isEmulator(): Promise<boolean> {
    return await NativeSecurityShield.isEmulator();
  }

  async isDebugging(): Promise<boolean> {
    return await NativeSecurityShield.isDebugging();
  }

  async isFridaDetected(): Promise<boolean> {
    return await NativeSecurityShield.isFridaDetected();
  }

  async isXposedDetected(): Promise<boolean> {
    if (Platform.OS !== "android") return false;
    return await NativeSecurityShield.isXposedDetected();
  }

  async isRooted(): Promise<boolean> {
    if (Platform.OS !== "android") return false;
    return await NativeSecurityShield.isRooted();
  }

  setSecureFlag(enable = true): void {
    NativeSecurityShield.setSecureFlag(enable);
  }

  exitApp(): void {
    NativeSecurityShield.exitApp();
  }

  private ensureConfigured() {
    if (!this.configured) {
      throw new Error(
        "SecurityShield is not configured. Call SecurityShield.configure(...) before start() or runSecurityCheck()."
      );
    }
  }
}

const SecurityShield = new SecurityShieldManager();

export default SecurityShield;
export { SecurityShield };