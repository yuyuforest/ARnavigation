package com.yuyuforest.navar.ar.mapcam;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.yuyuforest.navar.R;
import com.yuyuforest.navar.SuperApplication;
import com.yuyuforest.navar.service.LocationService;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import map.baidu.ar.model.ArLatLng;
import map.baidu.ar.model.ArPoiInfo;
import map.baidu.ar.model.PoiInfoImpl;

import static com.yuyuforest.navar.SuperApplication.center;

/**
 * ArSdk主页面 Activity
 */
public class ArMainActivity extends AppCompatActivity implements View.OnClickListener, /*OnGetDataResultListener,*/
        OnGetPoiSearchResultListener {

    private EditText mEtCategory;
    private Button mArFind;
    private PoiSearch mPoiSearch = null;
    public static List<PoiInfoImpl> poiInfos; // 探索
    int radius = 10000; // 10km半径
    private int loadIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_arsdk);

        mEtCategory = findViewById(R.id.category);
        mArFind = findViewById(R.id.app_find);
        mArFind.setOnClickListener(this);

        // 如果需要检索，初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 探索功能
            case R.id.app_find:
                //locationService.start();// 定位SDK
                if(center != null) searchNearbyProcess();
                else Toast.makeText(ArMainActivity.this, "定位失败，无法搜索", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();

        // -----------location config ------------
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，
        //可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService = ((SuperApplication) getApplication()).locationService;
        locationService.registerListener(mListener);
        /*
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        mArFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationService.start();// 定位SDK
                // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
            }
        });
    }
    */

    /**
     * 响应周边搜索按钮点击事件
     */
    public void searchNearbyProcess() {
        PoiNearbySearchOption nearbySearchOption =
                new PoiNearbySearchOption().keyword(mEtCategory.getText().toString()).sortType(PoiSortType
                        .distance_from_near_to_far).location(center).radius(radius).pageNum(loadIndex);
        mPoiSearch.searchNearby(nearbySearchOption);
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "未找到结果", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            poiInfos = new ArrayList<PoiInfoImpl>();
            for (PoiInfo poi : result.getAllPoi()) {
                ArPoiInfo poiInfo = new ArPoiInfo();
                ArLatLng arLatLng = new ArLatLng(poi.location.latitude, poi.location.longitude);
                poiInfo.name = poi.name;
                poiInfo.location = arLatLng;
                PoiInfoImpl poiImpl = new PoiInfoImpl();
                poiImpl.setPoiInfo(poiInfo);
                poiInfos.add(poiImpl);
            }
            Toast.makeText(this, "查询到" + poiInfos.size() + "个结果", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ArMainActivity.this, ArBaseActivity.class);
            ArMainActivity.this.startActivity(intent);
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += "，";
            }
            strInfo += "找到结果";
            Toast.makeText(this, strInfo, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果:-(", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }
}
