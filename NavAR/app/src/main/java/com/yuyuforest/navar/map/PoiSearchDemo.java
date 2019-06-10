package com.yuyuforest.navar.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.yuyuforest.navar.R;
import com.yuyuforest.navar.SuperApplication;
import com.yuyuforest.navar.Utils;
import com.yuyuforest.navar.ar.mapcam.ArBaseActivity;
import com.yuyuforest.navar.panorama.PanoDemoActivity;
import com.yuyuforest.navar.panorama.PanoDemoMain;
import com.yuyuforest.navar.service.SuperLocationListener;
import com.yuyuforest.navar.walk.BNaviMainActivity;

import java.util.ArrayList;
import java.util.List;

import map.baidu.ar.model.PoiInfoImpl;

import static com.yuyuforest.navar.SuperApplication.center;

/**
 * 演示poi搜索功能
 */
public class PoiSearchDemo extends AppCompatActivity implements
        OnGetPoiSearchResultListener {

    private PoiSearch mPoiSearch = null;
    private BaiduMap mBaiduMap = null;
    private EditText keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int loadIndex = 0;

    //private SuggestionSearch mSuggestionSearch = null;
    //private EditText editCity = null;
    //private LatLng center = new LatLng(39.92235, 116.380338);

    private int radius = 5000;   // 100米

    private boolean drawed = false;

    /*****
     * 定位结果回调，重写onReceiveLocation方法
     */

    private class MyLocationListener extends SuperLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            setMapLocation();
        }
    }
    private BDAbstractLocationListener mListener = new MyLocationListener();

    private int searchType = 0;  // 搜索的类型，在显示时区分

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisearch);
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        //mSuggestionSearch = SuggestionSearch.newInstance();
        //mSuggestionSearch.setOnGetSuggestionResultListener(this);

        //editCity = findViewById(R.id.city);
        mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map))).getBaiduMap();
        mBaiduMap.setMyLocationEnabled(true);
        setMapLocation();

        keyWorldsView = findViewById(R.id.searchkey);
        /*
        sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        keyWorldsView.setThreshold(1);
        // 当输入关键字变化时，动态更新建议列表
        keyWorldsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                // 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                    .keyword(cs.toString())
                    .city(editCity.getText().toString()));
            }
        });
        */

    }

    @Override
    protected void onStart() {
        super.onStart();

        SuperApplication.locationService.registerListener(mListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SuperApplication.locationService.unregisterListener(mListener);
    }

    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        //mSuggestionSearch.destroy();
        super.onDestroy();
    }

    private void setMapLocation() {
        BDLocation location = SuperApplication.location;
        if(location == null) return;

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.getDirection()).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);

        if(!drawed) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f); //设置缩放中心点；缩放比例；

            //给地图设置状态
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            //Toast.makeText(PoiSearchDemo.this, "drawed " + center.latitude + " " + center.longitude, Toast.LENGTH_SHORT).show();
            drawed = true;
        }
    }

    /*/**
     * 响应城市内搜索按钮点击事件
     *
     * @param v    检索Button

    public void searchButtonProcess(View v) {
        searchType = 1;

        //String citystr = editCity.getText().toString();
        String keystr = keyWorldsView.getText().toString();

        mPoiSearch.searchInCity((new PoiCitySearchOption())
            .city(citystr)
            .keyword(keystr)
            .pageNum(loadIndex)
            .scope(1));
    }

    public void goToNextPage(View v) {
        loadIndex++;
        searchButtonProcess(null);
    }*/

    /**
     * 响应周边搜索按钮点击事件
     *
     * @param v    检索Button
     */
    public void  searchNearbyProcess(View v) {
        if(center == null) {
            Toast.makeText(PoiSearchDemo.this, "定位失败，无法搜索", Toast.LENGTH_SHORT).show();
            return;
        }
        searchType = 2;
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption()
            .keyword(keyWorldsView.getText().toString())
            .sortType(PoiSortType.distance_from_near_to_far)
            .location(center)
            .radius(radius)
            .pageNum(loadIndex)
            .scope(1);

        mPoiSearch.searchNearby(nearbySearchOption);
    }

    /**
     * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
     *
     * @param result    Poi检索结果，包括城市检索，周边检索，区域检索
     */
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(PoiSearchDemo.this, "未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();

            switch(searchType) {
                case 2:
                    showNearbyArea(center, radius);
                    break;
                default:
                    break;
            }

            return;
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";

            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }

            strInfo += "找到结果";
            Toast.makeText(PoiSearchDemo.this, strInfo, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
     * V5.2.0版本之后，还方法废弃，使用{@link #onGetPoiDetailResult(PoiDetailSearchResult)}代替
     * @param result    POI详情检索结果
     */
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(PoiSearchDemo.this,result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT).show();

            Utils.showPoiInfo(PoiSearchDemo.this, result.name, result.address, result.location.latitude, result.location.longitude);
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        if (poiDetailSearchResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(PoiSearchDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            List<PoiDetailInfo> poiDetailInfoList = poiDetailSearchResult.getPoiDetailInfoList();
            if (null == poiDetailInfoList || poiDetailInfoList.isEmpty()) {
                Toast.makeText(PoiSearchDemo.this, "抱歉，检索结果为空", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < poiDetailInfoList.size(); i++) {
                PoiDetailInfo poiDetailInfo = poiDetailInfoList.get(i);
                if (null != poiDetailInfo) {
                    Toast.makeText(PoiSearchDemo.this,
                        poiDetailInfo.getName() + "!!!: " + poiDetailInfo.getAddress(),
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /*/**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     *
     * @param res    Sug检索结果

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }

        List<String> suggest = new ArrayList<>();
        suggest.add(keyWorldsView.getText().toString());
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }

        sugAdapter = new ArrayAdapter<>(PoiSearchDemo.this, android.R.layout.simple_dropdown_item_1line,
            suggest);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }*/

    private class MyPoiOverlay extends PoiOverlay {
        MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            // if (poi.hasCaterDetails) {
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
            // }
            return true;
        }
    }

    /**
     * 对周边检索的范围进行绘制
     *
     * @param center    周边检索中心点坐标
     * @param radius    周边检索半径，单位米
     */
    public void showNearbyArea(LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor( 0x00CCCC00 )
            .center(center)
            .stroke(new Stroke(5, 0xFFFF00FF ))
            .radius(radius);

        mBaiduMap.addOverlay(ooCircle);
    }
}
