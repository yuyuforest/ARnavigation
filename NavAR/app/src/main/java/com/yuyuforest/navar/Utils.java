package com.yuyuforest.navar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.yuyuforest.navar.panorama.PanoDemoActivity;
import com.yuyuforest.navar.panorama.PanoDemoMain;
import com.yuyuforest.navar.walk.BNaviMainActivity;

import static com.yuyuforest.navar.SuperApplication.center;

public class Utils {
    public static void showPoiInfo(final Context context, String name, String address, final double lat, final double lng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(name + "\n\n" + (address == null ? "" : address))
                .setPositiveButton("到这里去", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, BNaviMainActivity.class);
                        intent.putExtra("startLat", center.latitude);
                        intent.putExtra("startLng", center.longitude);
                        intent.putExtra("endLat", lat);
                        intent.putExtra("endLng", lng);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("查看附近全景", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, PanoDemoMain.class);
                        intent.putExtra("type", PanoDemoActivity.GEO);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lng", lng);
                        context.startActivity(intent);
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }
}
