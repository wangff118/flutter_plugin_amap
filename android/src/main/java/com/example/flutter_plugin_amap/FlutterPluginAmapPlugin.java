package com.example.flutter_plugin_amap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.HashMap;
import java.util.ArrayList;

import com.amap.api.fence.GeoFenceClient;
import com.amap.api.location.DPoint;
import com.amap.api.maps.model.LatLng;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.fence.GeoFence;

import android.os.Message;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.flutter.plugin.common.PluginRegistry.Registrar;


/** FlutterPluginAmapPlugin */
public class FlutterPluginAmapPlugin implements MethodCallHandler,GeoFenceListener {

 private Registrar registrar;	
	// 地理围栏客户端
 private GeoFenceClient mGeoFenceClient = null;
 private static final String GEOFENCE_BROADCAST_ACTION = "com.example.flutter_plugin_amap";
 
  private Context getApplicationContext(){
        return registrar.activity().getApplicationContext();
   }
 	
  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_plugin_amap");
    channel.setMethodCallHandler(new FlutterPluginAmapPlugin());
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {  
	   String method = call.method;
	   
	if ("onCreate".equals(method)) {
        mGeoFenceClient = new GeoFenceClient(getApplicationContext());
		mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_STAYED | GeoFenceClient.GEOFENCE_OUT)；
		mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
		mGeoFenceClient.setGeoFenceListener(this);
		
	    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);
    } 
	//1.根据关键字创建POI围栏
    else if (call.method.equals("createPOIkeyword")) {
		
		Object arguments = call.arguments;
		HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		String keyword = argsMap.get("keyword");
        String poiType = argsMap.get("poiType");
        String city = argsMap.get("city");
		String s_size = argsMap.get("size");
		int size =  Integer.parseInt(s_size); 
		String customId = argsMap.get("customId");
		
		mGeoFenceClient.addGeoFence(keyword, poiType, city,  size, customId);
		
		//result.success("createPOIkeyword");
	}
	
    //2.根据经纬度进行周边搜索创建POI围栏
    else if (call.method.equals("createPOIPoint")) {
		
		Object arguments = call.arguments;
		HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		String keyword = argsMap.get("keyword");
        String poiType = argsMap.get("poiType");
		
		double longitude =Double.parseDouble(argsMap.get("longitude")) ;
		double latitude  = Double.parseDouble(argsMap.get("latitude"));
		
		//创建一个中心点坐标
		DPoint centerPoint = new DPoint();
		//设置中心点纬度
		centerPoint.setLatitude(longitude);
		//设置中心点经度
		centerPoint.setLongitude(latitude);
		
		float aroundRadius = Float.parseFloat(argsMap.get("aroundRadius"))
		
       
		String s_size = argsMap.get("size");
		int size =  Integer.parseInt(s_size); 
		String customId = argsMap.get("customId");
		
		mGeoFenceClient.addGeoFence(keyword, poiType, centerPoint,  aroundRadius, size, customId);
		//result.success("createPOIPoint");
	}	
	//3.创建行政区划围栏
	else if (call.method.equals("createAdministrativearea")) {
		
		Object arguments = call.arguments;
		HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		String keyword = argsMap.get("keyword");
        String customId = argsMap.get("customId");
		
        mGeoFenceClient.addGeoFence(String keyword, String customId)；	

      //  result.success("createAdministrativearea");		
	}
	//4.创建自定义围栏 --  圆形围栏
	else if (call.method.equals("createRoundFence")) {
		
		Object arguments = call.arguments;
		HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		double longitude =Double.parseDouble(argsMap.get("longitude")) ;
		double latitude  = Double.parseDouble(argsMap.get("latitude"));
		
		//创建一个中心点坐标
		DPoint centerPoint = new DPoint();
		//设置中心点纬度
		centerPoint.setLatitude(longitude);
		//设置中心点经度
		centerPoint.setLongitude(latitude);		
		
		float radius = Float.parseFloat(argsMap.get("radius"))
        String customId = argsMap.get("customId");
		
        mGeoFenceClient.addGeoFence(centerPoint, radius,  customId);	

      //  result.success("createRoundFence");			
	}
	//5.创建自定义围栏 --  多边形围栏
    else if (call.method.equals("createPolygonalFence")) {  
        Object arguments = call.arguments;
	    HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		ArrayList<LatLng> points = (ArrayList<LatLng>) argsMap.get("points");
	 
	    String customId = argsMap.get("customId");
	    mGeoFenceClient.addGeoFence(points, customId);
		
	//	result.success("createPolygonalFence");	
    }//6.创建并设置PendingIntent
    else if (call.method.equals("createPolygonalFence")) {  
        Object arguments = call.arguments;
	    HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		ArrayList<LatLng> points = (ArrayList<LatLng>) argsMap.get("points");
	 
	    String customId = argsMap.get("customId");
	    mGeoFenceClient.addGeoFence(points, customId);
		
	//	result.success("createPolygonalFence");	
    }

	else {
      result.notImplemented();
    }
	
  private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {     
	  // 接收广播
	  if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
		
		Bundle bundle = intent.getExtras();
		//获取对应的围栏的语音url地址
		String customId = bundle
			.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
		String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
		GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
		//status标识的是当前的围栏状态，不是围栏行为
		int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
		
		StringBuffer sb = new StringBuffer();
		switch (status) {
		  case GeoFence.STATUS_LOCFAIL:
			sb.append("定位失败");
			break;
		  case GeoFence.STATUS_IN:
			sb.append("进入围栏 ");
		   // openMediaPlay(customId);
			break;
		  case GeoFence.STATUS_OUT:
			sb.append("离开围栏 ");
		 //   mediaPlayer.pause();
			break;
		  case GeoFence.STATUS_STAYED:
			sb.append("停留在围栏内 ");
			break;
		  default:
			break;
		}
		if (status != GeoFence.STATUS_LOCFAIL) {
		  if (customId!=null) {
			sb.append(" customId: " + customId);
		  }
		  sb.append(" fenceId: " + fenceId);
		}
		String str = sb.toString();
		Message msg = Message.obtain();
		msg.obj = str;
		msg.what = 2;
		result.success(msg);	
	  
	  }
	}
  };
  
  
}
