package com.yuyuforest.navar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.yuyuforest.navar.ar.mapcam.ArMainActivity;
import com.yuyuforest.navar.ar.mapcam.PermissionsChecker;
import com.yuyuforest.navar.map.PoiSearchDemo;
import com.yuyuforest.navar.panorama.PanoDemoActivity;

public class SuperActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super);

        // 判断权限
        PermissionsChecker permissionsChecker = new PermissionsChecker(this);
        if (permissionsChecker.lacksPermissions()) {
            Toast.makeText(this, "缺少权限，请开启权限！", Toast.LENGTH_SHORT).show();
            openSetting();
        }
    }

    /**
     * 打开设置权限界面
     */
    public void openSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.open_ar:
                intent = new Intent(SuperActivity.this, ArMainActivity.class);
                break;
            case R.id.open_walk:
                intent = new Intent(SuperActivity.this, PoiSearchDemo.class);
                break;
            default:
                break;
        }
        if(intent != null) {
            SuperActivity.this.startActivity(intent);
        }
    }
}
