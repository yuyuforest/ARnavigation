package com.yuyuforest.navar;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.yuyuforest.navar.ar.utils.LocSdkClient;
import com.yuyuforest.navar.service.LocationService;
import com.yuyuforest.navar.service.SuperLocationListener;

import map.baidu.ar.init.ArSdkManager;
import map.baidu.ar.utils.ArBDLocation;

public class SuperApplication extends Application {
    private static SuperApplication mInstance = null;

    public static LocationService locationService;
    public static LatLng center = null;
    public static BDLocation location = null;
    public BMapManager mBMapManager = null;

    private BDAbstractLocationListener locationListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // 定位
        locationService = new LocationService(getApplicationContext());
        locationListener = new SuperLocationListener();
        locationService.start();
        locationService.registerListener(locationListener);
        //locationService.setLocationOption(locationService.getDefaultLocationClientOption());

        // ArSDK模块初始化
        ArSdkManager.getInstance().initApplication(this, new MyGeneralListener());

        // 若用百度定位sdk,需要在此初始化定位SDK
        LocSdkClient.getInstance(this).getLocationStart();

        // 若用探索功能需要再这集成检索模块 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        // 检索模块 自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);

        // 全景
        initEngineManager(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        locationService.unregisterListener(locationListener);
        locationService.stop();
    }

    public static SuperApplication getInstance() {
        return mInstance;
    }

    public static void setLocation(BDLocation l) {
        location = l;
        center = new LatLng(location.getLatitude(), location.getLongitude());
    }

    // 全景
    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(SuperApplication.getInstance().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_SHORT).show();
        }
        Log.d("ljx", "initEngineManager");
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    public static class MyGeneralListener implements map.baidu.ar.init.MKGeneralListener, com.baidu.lbsapi.MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(SuperApplication.getInstance().getApplicationContext(),
                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(SuperApplication.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_SHORT).show();
            }
        }

        // 回调给ArSDK获取坐标（demo调用百度定位sdk）
        @Override
        public ArBDLocation onGetBDLocation() {
            // 3、用于传递给ArSdk经纬度信息
            // a、首先通过百度地图定位SDK获取经纬度信息
            // b、包装经纬度信息到ArSdk的ArBDLocation类中 return即可
            BDLocation location =
                    LocSdkClient.getInstance(ArSdkManager.getInstance().getAppContext()).getLocationStart()
                            .getLastKnownLocation();
            if (location == null) {
                return null;
            }
            ArBDLocation arBDLocation = new ArBDLocation();

            // 设置经纬度信息
            arBDLocation.setLongitude(location.getLongitude());
            arBDLocation.setLatitude(location.getLatitude());
            return arBDLocation;
        }
    }
}
