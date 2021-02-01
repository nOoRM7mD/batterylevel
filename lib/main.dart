import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const String _channel = 'samples.flutter.dev/battery';
  String _batteryLevel;

  String _result = 'Unknown battery level.';

  //static const platform = const MethodChannel('samples.flutter.dev/battery');
  static const BasicMessageChannel<String> platform =
      BasicMessageChannel<String>(_channel, StringCodec());

  @override
  void initState() {
    super.initState();
    platform.setMessageHandler(_getBatteryLevel);
  }

  /// this function helps to manage the message received under MessageChannel.
  // ignore: missing_return
  Future<String> _getBatteryLevel(String message) async {
    String batteryLevel;

    try {
      // final int result = await platform.invokeMethod('getBatteryLevel');
      batteryLevel = 'Battery level at $message % .';
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }
    setState(() {
      _batteryLevel = batteryLevel;
    });
  }

  void onGetBatteryBtnClicked() {
    setState(() {
      _result = _batteryLevel;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
              child: Text('Get Battery Level'),
              onPressed: onGetBatteryBtnClicked,
            ),
            Text(_result),
          ],
        ),
      ),
    );
  }
}
