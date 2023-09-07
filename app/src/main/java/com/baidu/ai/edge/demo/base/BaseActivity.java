package com.baidu.ai.edge.demo.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.baidu.ai.edge.core.base.Consts;
import com.baidu.ai.edge.demo.R;

/**
 * Created by linyiran on 6/16/22.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;

    protected boolean allPermissionsGranted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        requestPermission();
        onActivityCreated(savedInstanceState);
    }

    protected abstract void setLayout();

    protected abstract void onActivityCreated(Bundle savedInstanceState);

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA
            };
            allPermissionsGranted = true;
            for (String perm : permissions) {
                if ((PermissionChecker.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                ActivityCompat.requestPermissions(BaseActivity.this, permissions, REQUEST_PERMISSION);
            } else {
                requestAllFilesAccess();
            }
        } else {
            allPermissionsGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            allPermissionsGranted = true;
            for (int grantRes : grantResults) {
                if (grantRes != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                requestAllFilesAccess();
            }
        }
    }

    /**
     * Android 11 跳转到设置获取SD卡根目录写入权限
     */
    private void requestAllFilesAccess() {
        if (!Consts.AUTH_REQUIRE_SDCARD) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            allPermissionsGranted = false;

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(BaseActivity.this,
                    R.style.AppTheme);
            alertBuilder.setMessage("需授权访问SD卡文件");
            alertBuilder.setCancelable(false);
            alertBuilder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertBuilder.show();
        }
    }
}
