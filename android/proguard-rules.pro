-keep class com.securityshield.** { *; }
-keepclassmembers class com.securityshield.** { *; }

-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keepclassmembers class * {
    native <methods>;
}