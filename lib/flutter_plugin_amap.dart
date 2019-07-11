import 'dart:async';
import 'package:amap_base_navi/amap_base_navi.dart';
import 'package:flutter/services.dart';

class FlutterPluginAmap {
  static const MethodChannel _channel =
      const MethodChannel('flutter_plugin_amap');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 创建地理围栏对象
  static void onCreate() async{
    await _channel.invokeMethod('onCreate');
  }

  /// 根据关键字创建POI围栏
  static void createPOIkeyword(String keyword, String poiType, String city, int size,String customId) async{
    Map argsMap = <String, String>{
      'keyword': '$keyword',
      'poiType': '$poiType',
      'city': '$city',
      'size': '$size',
      'customId': '$customId'
    };
    await  _channel.invokeMethod('createPOIkeyword', argsMap);
  }

  /// 根据经纬度进行周边搜索创建POI围栏
  static void createPOIPoint(String keyword, String poiType, String city, String longitude, String latitude, String aroundRadius, int size,String customId) async{
    Map argsMap = <String, String>{
      'keyword': '$keyword',
      'poiType': '$poiType',
      'city': '$city',
      'longitude': '$longitude',
      'latitude': '$latitude',
      'aroundRadius': '$aroundRadius',
      'size': '$size',
      'customId': '$customId'
    };
    await  _channel.invokeMethod('createPOIPoint', argsMap);
  }

  /// 创建行政区划围栏
  static void createAdministrativearea(String keyword, String customId) async{
    Map argsMap = <String, String>{
      'keyword': '$keyword',
      'customId': '$customId'
    };
    await  _channel.invokeMethod('createAdministrativearea', argsMap);
  }

  /// 创建自定义围栏 --  圆形围栏
  static void createRoundFence( String longitude, String latitude,String radius, String customId) async{
    Map argsMap = <String, String>{
      'longitude': '$longitude',
      'latitude': '$latitude',
      'radius': '$radius',
      'customId': '$customId'
    };
    await  _channel.invokeMethod('createRoundFence', argsMap);
  }

  ///创建自定义围栏 --  多边形围栏
  static void createPolygonalFence(List<LatLng> points, String customId) async{// List<int>> files
    Map argsMap = <String, dynamic>{
      'points':points,
      'customId': '$customId'
    };
    await  _channel.invokeMethod('createPolygonalFence', argsMap);
  }



}
