package com.example.flutter_plugin_amap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amap.api.fence.GeoFenceClient;
import com.amap.api.location.DPoint;
import com.amap.api.maps.model.LatLng;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.fence.GeoFence;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;

//import android.R;
//import com.joe.ditudemo.R
import android.support.v7.app.AppCompatActivity;
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
import android.graphics.Color;


import io.flutter.plugin.common.PluginRegistry.Registrar;




/** FlutterPluginAmapPlugin */
public class FlutterPluginAmapPlugin extends AppCompatActivity implements MethodCallHandler, GeoFenceListener{

  private Registrar registrar;	
	// 地理围栏客户端
  private GeoFenceClient mGeoFenceClient = null;
  private static final String GEOFENCE_BROADCAST_ACTION = "com.example.flutter_plugin_amap";
  private AMap mAMap;
  //private MapView mMapView;
    // 记录已经添加成功的围栏
  private volatile ConcurrentMap<String, GeoFence> fenceMap = new ConcurrentHashMap<String, GeoFence>();  
  private ConcurrentMap mCustomEntitys;
  private Context mContext;
  
  
  private Context getApplicationContext(){
	    mContext = registrar.activity().getApplicationContext();
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
		setContentView(R.layout.activity_main);
		//mMapView = (MapView)findViewById(R.id.map);
		setUpMapIfNeeded();
		mCustomEntitys = new ConcurrentHashMap<String, Object>();
        mGeoFenceClient = new GeoFenceClient(getApplicationContext());
		mGeoFenceClient.setGeoFenceListener(this);
		mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_STAYED | GeoFenceClient.GEOFENCE_OUT);
		mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
		
	    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(GEOFENCE_BROADCAST_ACTION);
        mContext.registerReceiver(mGeoFenceReceiver, filter);
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
		
		float aroundRadius = Float.parseFloat(argsMap.get("aroundRadius"));
		
       
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
		
        mGeoFenceClient.addGeoFence(keyword, customId);	

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
		
		float radius = Float.parseFloat(argsMap.get("radius"));
        String customId = argsMap.get("customId");
		
        mGeoFenceClient.addGeoFence(centerPoint, radius,  customId);	

      //  result.success("createRoundFence");			
	}
	//5.创建自定义围栏 --  多边形围栏
	/**
    else if (call.method.equals("createPolygonalFence")) {  
        Object arguments = call.arguments;
	    HashMap<String, String> argsMap = (HashMap<String, String>) arguments;
		
		ArrayList<LatLng> points = (ArrayList<LatLng>) argsMap.get("points");
	 
	    String customId = argsMap.get("customId");
	    mGeoFenceClient.addGeoFence(points, customId);
		
	//	result.success("createPolygonalFence");	
    }*/
	

	else {
      result.notImplemented();
    }
  
}

 // GeoFenceListener fenceListenter = new GeoFenceListener() {
    //创建回调监听
   @Override
   public void onGeoFenceCreateFinished(List<GeoFence> geoFenceList,int errorCode,String s) {
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
            for (GeoFence fence : geoFenceList) {
                //Log.e(TAG, "fenid:" + fence.getFenceId() + " customID:" + s + " " + fenceMap.containsKey(fence.getFenceId()));
                fenceMap.putIfAbsent(fence.getFenceId(), fence);
            }
            //Log.e(TAG, "回调添加成功个数:" + geoFenceList.size());
            //Log.e(TAG, "回调添加围栏个数:" + fenceMap.size());
            
			//result.success(geoFenceList);
			drawFenceToMap();
			
           // Log.e(TAG, "添加围栏成功！！");
        } else {
           
			//result.success(errorCode);
        }
    }
//};	

//mGeoFenceClient.setGeoFenceListener(fenceListenter);//设置回调监听
	
 BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
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
		//Message msg = Message.obtain();
		//msg.obj = str;
		//msg.what = 2;
	//	result.success(str);	
	  
	  }
	}
  };  
  

  public void drawFenceToMap() {
        Iterator iter = fenceMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            GeoFence val = (GeoFence) entry.getValue();
            if (!mCustomEntitys.containsKey(key)) {
               // Log.d("LG", "添加围栏:" + key);
                drawFence(val);
            }
        }
    } 

 private void drawFence(GeoFence fence) {
        drawCircle(fence);
 }	
 
  private void drawCircle(GeoFence fence) {
        CircleOptions option = new CircleOptions();
        option.fillColor(mContext.getResources().getColor(Color.argb(163, 118, 212, 243)));
        option.strokeColor(mContext.getResources().getColor(Color.argb(180, 63, 145, 252)));
        option.strokeWidth(4);
        option.radius(fence.getRadius());
        DPoint dPoint = fence.getCenter();
        option.center(new LatLng(dPoint.getLatitude(), dPoint.getLongitude()));
        Circle circle = mAMap.addCircle(option);
        mCustomEntitys.put(fence.getFenceId(), circle);
    }
	
	

  private void setUpMapIfNeeded() {
        if (mAMap == null) {
            mAMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            UiSettings uiSettings = mAMap.getUiSettings();
            if (uiSettings != null) {
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setMyLocationButtonEnabled(true); // 设置默认定位按钮是否显示
            }
           // mAMap.setLocationSource(this);// 设置定位监听
            mAMap.setMyLocationStyle(
                    new MyLocationStyle().radiusFillColor(Color.argb(0, 0, 0, 0))
                            .strokeColor(Color.argb(0, 0, 0, 0)).myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked)));
            mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(13));
        }
    }

}
