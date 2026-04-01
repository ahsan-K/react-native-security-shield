export type AndroidSecurityConfig = {
    releaseSha256?: string;
    playSigningSha256?: string;
    internalSharingSha256?: string;
    pins?: string[];
    allowedDomains?: string[];
  };
  
  export type IOSSecurityConfig = {
    bundleId?: string;
    teamId?: string;
    pins?: string[];
    allowedDomains?: string[];
  };
  
  export type SecurityOptions = {
    enableRootDetection?: boolean;
    enableFridaDetection?: boolean;
    enableDebuggerDetection?: boolean;
    enableEmulatorDetection?: boolean;
    enableScreenshotProtection?: boolean;
    killOnThreat?: boolean;
  };
  
  export type SecurityShieldConfig = {
    android?: AndroidSecurityConfig;
    ios?: IOSSecurityConfig;
    options?: SecurityOptions;
  };
  
  export type SecurityCheckResult = {
    configured: boolean;
    signatureValid: boolean;
    emulatorDetected: boolean;
    debuggingDetected: boolean;
    fridaDetected: boolean;
    xposedDetected: boolean;
    rootedDetected: boolean;
    safe: boolean;
  };