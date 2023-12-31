import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bixolon_printer_plugin/bixolon_printer_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              ElevatedButton(child: Text("Open Printer Activity"),
              onPressed: ()=>BixolonPrinterPlugin.showPrinterActivity ,),
              ElevatedButton(child: Text("Test Direct Print"),
                onPressed: ()=>BixolonPrinterPlugin.printIt("رهرو آن نیست که گهی تند وگهی خسته رود\nرهرو آنست که آهسته و پیوسته رود", "http://PartSilicon.com") ,),
              ElevatedButton(child: Text("platformVersion"),
                onPressed: ()=>BixolonPrinterPlugin.platformVersion ,)
            ],
          ),

        ),
      ),
    );
  }
}
