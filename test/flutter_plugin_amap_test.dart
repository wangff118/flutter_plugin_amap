import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_plugin_amap/flutter_plugin_amap.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_plugin_amap');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterPluginAmap.platformVersion, '42');
  });
}
