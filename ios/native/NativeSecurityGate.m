#import <Foundation/Foundation.h>

// Intentionally left passive for reusable package mode.
// Do not auto-kill app at library load time.
// Runtime checks are triggered explicitly from JS via:
// SecurityShield.configure(...)
// SecurityShield.start()