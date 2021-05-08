
import 'dart:async';

import 'package:flutter/services.dart';

class BixolonPrinterPlugin {
  static const MethodChannel _channel =
      const MethodChannel('bixolon_printer_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> get showPrinterActivity async {
    final String res = await _channel.invokeMethod('printerActivity');
    return res;
  }

  static Future<String> printIt(String text,String qrCode) async {
    final String res = await _channel.invokeMethod('printIt',<String, dynamic>{
      'text': text,
      'qrCode': qrCode,
    });
    return res;
  }

}
