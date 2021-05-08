#import "BixolonPrinterPlugin.h"
#if __has_include(<bixolon_printer_plugin/bixolon_printer_plugin-Swift.h>)
#import <bixolon_printer_plugin/bixolon_printer_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "bixolon_printer_plugin-Swift.h"
#endif

@implementation BixolonPrinterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBixolonPrinterPlugin registerWithRegistrar:registrar];
}
@end
