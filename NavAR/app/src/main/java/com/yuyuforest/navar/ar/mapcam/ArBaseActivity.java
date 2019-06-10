package com.yuyuforest.navar.ar.mapcam;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yuyuforest.navar.R;
import com.yuyuforest.navar.Utils;
import com.yuyuforest.navar.panorama.PanoDemoActivity;
import com.yuyuforest.navar.panorama.PanoDemoMain;

import map.baidu.ar.ArPageListener;
import map.baidu.ar.camera.SimpleSensor;
import map.baidu.ar.camera.find.FindArCamGLView;
import map.baidu.ar.model.ArPoiInfo;
import map.baidu.ar.model.PoiInfoImpl;
import map.baidu.ar.utils.TypeUtils;

/**
 * Ar默认展示 Activity
 */
public class ArBaseActivity extends AppCompatActivity implements ArPageListener {

    private RelativeLayout camRl;
    private FindArCamGLView mCamGLView;
    public static ArrayList<PoiInfoImpl> poiInfos;
    private RelativeLayout mArPoiItemRl;
    private SimpleSensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_find_ar);
        poiInfos = (ArrayList<PoiInfoImpl>) ArMainActivity.poiInfos;
        mArPoiItemRl = (RelativeLayout) findViewById(R.id.ar_poi_item_rl);
        mArPoiItemRl.setVisibility(View.VISIBLE);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        camRl = (RelativeLayout) findViewById(R.id.cam_rl);
        mCamGLView = (FindArCamGLView) LayoutInflater.from(this).inflate(R.layout.layout_find_cam_view, null);
        mCamGLView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom == 0 || oldBottom != 0 || mCamGLView == null) {
                    return;
                }
                RelativeLayout.LayoutParams params = TypeUtils.safeCast(
                        mCamGLView.getLayoutParams(), RelativeLayout.LayoutParams.class);
                if (params == null) {
                    return;
                }
                params.height = bottom - top;
                mCamGLView.requestLayout();
            }
        });
        camRl.addView(mCamGLView);
        initSensor();
        // 保持屏幕不锁屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initSensor() {
        if (mSensor == null) {
            mSensor = new SimpleSensor(this, new HoldPositionListenerImp());
        }
        mSensor.startSensor();
    }

    private class HoldPositionListenerImp implements SimpleSensor.OnHoldPositionListener {
        @Override
        public void onOrientationWithRemap(float[] remapValue) {
            if (mCamGLView != null && mArPoiItemRl != null) {
                if (poiInfos.size() <= 0) {
                    mArPoiItemRl.setVisibility(View.GONE);
                    Toast.makeText(ArBaseActivity.this, "附近没有可识别的类别", Toast.LENGTH_SHORT).show();
                } else {
                    mCamGLView.setFindArSensorState(remapValue, getLayoutInflater(),
                            mArPoiItemRl, ArBaseActivity.this, poiInfos, ArBaseActivity.this);
                    mArPoiItemRl.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void finishCamInternal() {
        if (mCamGLView != null) {
            mCamGLView.stopCam();
            camRl.removeAllViews();
            mCamGLView = null;

        }
        if (mArPoiItemRl != null) {
            mArPoiItemRl.removeAllViews();
        }
        if (mSensor != null) {
            mSensor.stopSensor();
        }
        // 恢复屏幕自动锁屏
        ArBaseActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(ArBaseActivity.this).cloneInContext(ArBaseActivity.this);
    }

    @Override
    public void noPoiInScreen(boolean isNoPoiInScreen) {
    }

    @Override
    public void selectItem(Object iMapPoiItem) {
        if (iMapPoiItem instanceof PoiInfoImpl) {
            ArPoiInfo info = ((PoiInfoImpl) iMapPoiItem).getPoiInfo();
            //Toast.makeText(this, "点击poi: " + info.name, Toast.LENGTH_SHORT).show();
            Utils.showPoiInfo(ArBaseActivity.this, info.name, info.address, info.location.latitude, info.location.longitude);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishCamInternal();
    }
}
