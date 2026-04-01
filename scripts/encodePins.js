#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const readline = require("readline");

const PIN_KEY = 0x5a;

function xorEncode(str, key) {
  const bytes = Buffer.from(str, "utf8");
  return Array.from(bytes).map((b) => b ^ key);
}

function toCppArray(name, arr) {
  if (!arr.length) {
    return `static const uint8_t ${name}[] = {};`;
  }

  const formatted = arr
    .map((b) => `0x${b.toString(16).padStart(2, "0")}`)
    .join(", ");

  return `static const uint8_t ${name}[] = { ${formatted} };`;
}

function replaceSection(content, name, replacementLine) {
  const regex = new RegExp(
    `static const uint8_t ${name}\\[\\] = \\{[^]*?\\};`,
    "m"
  );

  if (!regex.test(content)) {
    throw new Error(`Could not find placeholder for ${name} in native_integrity.cpp`);
  }

  return content.replace(regex, replacementLine);
}

function askQuestion(rl, question) {
  return new Promise((resolve) => rl.question(question, resolve));
}

async function main() {
  const packageRoot = path.resolve(__dirname, "..");
  const targetFile = path.join(
    packageRoot,
    "android",
    "src",
    "main",
    "cpp",
    "native_integrity.cpp"
  );

  if (!fs.existsSync(targetFile)) {
    throw new Error(`File not found: ${targetFile}`);
  }

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  try {
    console.log("\nReact Native Security Shield — XOR Pin Encoder\n");

    const pin1 = (await askQuestion(rl, "Enter Pin 1 (Base64 SHA-256), or leave blank: ")).trim();
    const pin2 = (await askQuestion(rl, "Enter Pin 2 (Base64 SHA-256), or leave blank: ")).trim();

    const pin1Encoded = pin1 ? xorEncode(pin1, PIN_KEY) : [];
    const pin2Encoded = pin2 ? xorEncode(pin2, PIN_KEY) : [];

    let fileContent = fs.readFileSync(targetFile, "utf8");

    fileContent = replaceSection(fileContent, "PIN1_XOR", toCppArray("PIN1_XOR", pin1Encoded));
    fileContent = replaceSection(fileContent, "PIN2_XOR", toCppArray("PIN2_XOR", pin2Encoded));

    const keyRegex = /static const uint8_t PIN_KEY = 0x[0-9a-fA-F]+;/;
    if (!keyRegex.test(fileContent)) {
      throw new Error("Could not find PIN_KEY in native_integrity.cpp");
    }

    fileContent = fileContent.replace(
      keyRegex,
      `static const uint8_t PIN_KEY = 0x${PIN_KEY.toString(16)};`
    );

    fs.writeFileSync(targetFile, fileContent, "utf8");

    console.log("\nDone.");
    console.log(`Updated: ${targetFile}`);
    console.log("Native XOR pins have been generated successfully.\n");
  } finally {
    rl.close();
  }
}

main().catch((err) => {
  console.error("\nError:", err.message);
  process.exit(1);
});