Pod::Spec.new do |s|
    s.name         = "react-native-security-shield"
    s.version      = "1.0.0"
    s.summary      = "Secure-first React Native package for mobile app hardening"
    s.description  = "React Native package for SSL pinning, signature validation, emulator/debug/frida detection, and screenshot protection."
    s.homepage     = "https://github.com/yourname/react-native-security-shield"
    s.license      = { :type => "MIT" }
    s.author       = { "Your Name" => "you@example.com" }
    s.platforms    = { :ios => "13.0" }
    s.source       = { :git => "https://github.com/yourname/react-native-security-shield.git", :tag => s.version.to_s }
  
    s.source_files = "ios/**/*.{h,m,mm,swift}"
    s.swift_version = "5.0"
  
    s.dependency "React-Core"
  end