# 🔐 React Native Security Shield

Enterprise-grade security for React Native apps.

- SSL Pinning (native-oriented)
- Android app signature validation
- Debugger detection
- Frida detection
- Root / jailbreak style checks
- Reusable package setup across projects
- Root & Emulator Detection
- Screenshot Blocking

Author: **Ahsan Ali**

---

## Installation

```bash
npm install react-native-security-shield
```

iOS:

```bash
cd ios && pod install && cd ..
```

---

## What this package does

This package provides a reusable security layer for React Native apps. It supports:

- Android native security checks
- iOS native security checks
- runtime `configure()` support
- generated native config support
- optional XOR-obfuscated SSL pin storage in native C++

---

# Host Project Setup

This section includes **all host project changes** required to integrate the package correctly.

---

## 1) Create security config file in host project root

Create this file in your app root:

### `securityshield.config.json`

```json
{
  "android": {
    "releaseSha256": "YOUR_RELEASE_SHA256_HEX",
    "playSigningSha256": "YOUR_PLAY_SIGNING_SHA256_HEX",
    "internalSharingSha256": "YOUR_INTERNAL_SIGNING_SHA256_HEX",
    "pins": [
      "YOUR_BASE64_PIN_1",
      "YOUR_BASE64_PIN_2"
    ],
    "allowedDomains": [
      "api.example.com",
      "uat.example.com"
    ]
  },
  "ios": {
    "bundleId": "com.example.app",
    "teamId": "ABCDE12345",
    "pins": [
      "YOUR_BASE64_PIN_1",
      "YOUR_BASE64_PIN_2"
    ],
    "allowedDomains": [
      "api.example.com",
      "uat.example.com"
    ]
  },
  "options": {
    "enableRootDetection": true,
    "enableFridaDetection": true,
    "enableDebuggerDetection": true,
    "enableEmulatorDetection": true,
    "enableScreenshotProtection": true,
    "killOnThreat": true
  }
}
```

---

## 2) Host project Android native changes

### A. `android/app/build.gradle`

Add the config generation task so native config files are generated before build. Please put this block after android {} before dependencies {}

```gradle
task generateSecurityConfig(type: Exec) {

    def appRoot = rootProject.projectDir.parentFile

    workingDir appRoot

    commandLine "node",

            new File(appRoot, "node_modules/react-native-security-shield/scripts/generateNativeConfig.js").absolutePath

}
 
task encodePins(type: Exec) {

    def appRoot = rootProject.projectDir.parentFile

    workingDir appRoot

    commandLine "node",

            new File(appRoot, "node_modules/react-native-security-shield/scripts/encodePins.js").absolutePath

    standardInput = System.in

}
 
encodePins.mustRunAfter generateSecurityConfig
 
tasks.whenTaskAdded { task ->

    if (task.name == "assembleRelease" || task.name == "bundleRelease" || task.name == "installRelease") {

        task.dependsOn generateSecurityConfig

        task.dependsOn encodePins

    }

}
 
```

Put this block **after** the `android { ... }` block and **before** `dependencies { ... }`.

### Optional note about `encodePins.js`

If your current `encodePins.js` is interactive, do **not** attach it to `preBuild`.

Run it manually when pins change:

```bash
node node_modules/react-native-security-shield/scripts/encodePins.js
```

---

### B. `android/app/src/main/AndroidManifest.xml`

Your main manifest should include:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">

    <application
      android:name=".MainApplication"
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher"
      android:roundIcon="@mipmap/ic_launcher_round"
      android:allowBackup="false"
      android:theme="@style/AppTheme"
      tools:replace="android:allowBackup,android:usesCleartextTraffic,android:networkSecurityConfig"
      android:hardwareAccelerated="true"
      android:supportsRtl="true"
      android:usesCleartextTraffic="false"
      android:networkSecurityConfig="@xml/network_security_config">

    </application>
</manifest>
```

If the package manifest already sets `android:networkSecurityConfig`, then keep `tools:replace="android:networkSecurityConfig"` in host app manifest when overriding values.

---

### C. `android/app/src/debug/AndroidManifest.xml`

If you want debug-specific network config, use this file:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:networkSecurityConfig="@xml/network_security_config_debug"
        tools:replace="android:networkSecurityConfig"
        tools:targetApi="28"
        tools:ignore="GoogleAppIndexingWarning" />
</manifest>
```

This avoids manifest merger conflicts between:

- package manifest config
- host app main manifest config
- host app debug manifest config

---

### D. `android/app/src/main/res/xml/network_security_config.xml`

Create this file in the host app:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

### E. `android/app/src/debug/res/xml/network_security_config_debug.xml`

Create this debug file if you want a separate debug config:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>

    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

### F. `android/app/proguard-rules.pro`

Add:

```pro
-keep class com.securityshield.** { *; }
-keepclassmembers class com.securityshield.** { *; }

-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keepclassmembers class * {
    native <methods>;
}
```

---

## 3) Host project iOS native changes

### A. Run pods

```bash
cd ios
pod install
cd ..
```

### B. `Info.plist` optional entry

If you use the Cydia URL scheme check for jailbreak-related detection:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
    <string>cydia</string>
</array>
```

---

## 4) Host project package.json scripts

A good host-project setup is:

```json
{
  "scripts": {
    "security:prepare": "node node_modules/react-native-security-shield/scripts/generateNativeConfig.js",
    "android": "npm run security:prepare && react-native run-android",
    "ios": "npm run security:prepare && react-native run-ios"
  }
}
```

If later you make `encodePins.js` non-interactive, you can include it here too.

---

# Native generated config

The package uses generated native config files.

These are produced by:

```bash
node node_modules/react-native-security-shield/scripts/generateNativeConfig.js
```

This script reads `securityshield.config.json` from the host project root and writes generated config into package native files.

---

# encodePins.js usage

Use this when you want native XOR-obfuscated pin storage.

Run manually:

```bash
node node_modules/react-native-security-shield/scripts/encodePins.js
```

Use it when:

- pins change
- certificate rotates
- environment changes
- you want to update native-obfuscated pin values

If your current script asks for Base64 input interactively, that is expected.

---

# Usage in React Native app

## Basic startup usage

Use this in your app startup, such as `App.tsx` or a bootstrap file.

```tsx
import React, { useEffect } from "react";
import SecurityShield from "react-native-security-shield";
import securityConfig from "./securityshield.config.json";

export default function App() {
  useEffect(() => {
    const initSecurity = async () => {
      try {
        await SecurityShield.configure(securityConfig);

        const result = await SecurityShield.runSecurityCheck();
        console.log("Security result:", result);

        if (!result.safe) {
          console.log("Unsafe environment detected.");
        }

        await SecurityShield.start();
      } catch (error) {
        console.log("Security init error:", error);
      }
    };

    initSecurity();
  }, []);

  return null;
}
```

---

## Important logic note

Do **not** do this:

```js
if (!result.safe) {
  return;
}
await SecurityShield.start();
```

Why? Because:

- `runSecurityCheck()` only returns the status
- `start()` performs enforcement

If you `return` before calling `start()`, the app will not apply kill/exit logic.

---

## Debug-friendly startup usage

For development, you may want logs without enforcing hard close:

```tsx
import React, { useEffect } from "react";
import SecurityShield from "react-native-security-shield";
import securityConfig from "./securityshield.config.json";

export default function App() {
  useEffect(() => {
    const initSecurity = async () => {
      try {
        await SecurityShield.configure(securityConfig);

        const result = await SecurityShield.runSecurityCheck();
        console.log("Security result:", result);

        if (!result.safe && __DEV__) {
          console.log("Unsafe environment detected, but allowed in debug.");
          return;
        }

        await SecurityShield.start();
      } catch (error) {
        console.log("Security init error:", error);
      }
    };

    initSecurity();
  }, []);

  return null;
}
```

---

## Release behavior

In release mode, if:

- `emulatorDetected` is `true`
- or signature is invalid
- or another enabled threat is detected

then:

- `safe` becomes `false`
- `start()` should enforce protection
- app should close if `killOnThreat` is enabled

---

# Available API

```js
await SecurityShield.configure(config);
await SecurityShield.start();
const result = await SecurityShield.runSecurityCheck();

await SecurityShield.isConfigured();
await SecurityShield.isEmulator();
await SecurityShield.isDebugging();
await SecurityShield.isFridaDetected();
await SecurityShield.isXposedDetected();
await SecurityShield.isRooted();

SecurityShield.setSecureFlag(true);
SecurityShield.exitApp();
```

---

# Meaning of result object

Example:

```js
{
  configured: true,
  signatureValid: false,
  emulatorDetected: true,
  debuggingDetected: false,
  fridaDetected: false,
  xposedDetected: false,
  rootedDetected: false,
  safe: false
}
```

### Field meanings

- `configured`: config loaded correctly
- `signatureValid`: Android signature matches allowed SHA256
- `emulatorDetected`: emulator found
- `debuggingDetected`: debugger found
- `fridaDetected`: Frida found
- `xposedDetected`: Xposed found
- `rootedDetected`: root found
- `safe`: final overall result

---

# Security modes

## Runtime config mode

```js
await SecurityShield.configure(securityConfig);
await SecurityShield.start();
```

This is easier but weaker, because values are available in runtime/JS.

## Native generated config mode

```js
await SecurityShield.configure({});
await SecurityShield.start();
```

This is stronger when generated files and native pin encoding are already prepared.

---

# Android release signing note

For proper signature validation on Android, provide one or more of:

- `releaseSha256`
- `playSigningSha256`
- `internalSharingSha256`

If these do not match the installed APK signature, `signatureValid` becomes `false`.

This is especially useful against repackaging / resigning attacks.

---

# SSL pinning note

Important:

- this package provides pinning infrastructure
- default JS `fetch()` / `axios()` do not automatically use the package's native pinned client
- native pinning is strongest when requests are routed through a native pinned networking layer

So current setup gives:

- detection
- validation
- protection
- native pin support

But automatic JS-network pinning requires additional integration if needed.

---

# Common mistakes

## 1. Calling `runSecurityCheck()` but not `start()`

Wrong:

```js
const result = await SecurityShield.runSecurityCheck();
if (!result.safe) return;
await SecurityShield.start();
```

Right:

```js
const result = await SecurityShield.runSecurityCheck();
if (!result.safe) {
  console.log("Unsafe environment detected.");
}
await SecurityShield.start();
```

---

## 2. Manifest conflicts

If you get `networkSecurityConfig` merge errors:

- add `tools:replace="android:networkSecurityConfig"` where required
- keep debug override in `src/debug/AndroidManifest.xml`

---

## 3. Wrong script path in Gradle

Use:

```gradle
def appRoot = rootProject.projectDir.parentFile
workingDir appRoot
```

This ensures host project root is used, not `android/` directory.

---

## 4. Interactive encodePins in automated build

If `encodePins.js` prompts for input, do not attach it to automatic `preBuild`.

Use it manually until you make it non-interactive.

---

# Recommended workflow

## Development

- use debug build
- run generated native config script
- optionally skip hard-close in debug
- inspect `runSecurityCheck()` logs

## Production

- use generated native config
- use encoded pins
- keep `killOnThreat: true`
- call `start()` during startup
- test release build on real device and emulator

---

# Example host project file checklist

## Required / recommended host project files

- `securityshield.config.json`
- `android/app/build.gradle`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/debug/AndroidManifest.xml`
- `android/app/src/main/res/xml/network_security_config.xml`
- `android/app/src/debug/res/xml/network_security_config_debug.xml`
- `android/app/proguard-rules.pro`
- `ios/Info.plist` (optional `cydia` scheme)
- startup integration in `App.tsx`

---

# Build commands

## Android debug

```bash
yarn android
```

## Android release

```bash
cd android
./gradlew assembleRelease
```

## Generate native config manually

```bash
node node_modules/react-native-security-shield/scripts/generateNativeConfig.js
```

## Encode pins manually

```bash
node node_modules/react-native-security-shield/scripts/encodePins.js
```

---

# Notes on attack resistance

This package improves resistance against:

- repackaging
- emulator usage
- Frida-based inspection
- debugger attachment
- screenshot capture
- simple MITM attempts when pinning is properly integrated

But mobile client security is not absolute. Advanced attackers can still patch client code. Always combine app-side checks with backend-side validation.

---

# Author

**Ahsan Ali**

---

# License

MIT
