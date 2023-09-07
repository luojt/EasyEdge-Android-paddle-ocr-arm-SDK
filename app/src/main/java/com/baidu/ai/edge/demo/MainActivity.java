package com.baidu.ai.edge.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ai.edge.core.base.CallException;
import com.baidu.ai.edge.core.base.Consts;
import com.baidu.ai.edge.core.infer.InferManager;
import com.baidu.ai.edge.core.util.FileUtil;
import com.baidu.ai.edge.demo.base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends BaseActivity {
    private static final String TAG = "demo.MainActivity";

    private Button startUIActivityBtn;
    private String modelName = "";
    private String version = "";
    private String soc;
    private ArrayList<String> socList = new ArrayList<>();
    private int modelType;

    // 请替换为您的序列号
    private static final String SERIAL_NUM = "XXXX-XXXX-XXXX-XXXX";

    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState) {
        final AlertDialog.Builder agreementDialog = new AlertDialog.Builder(this)
                .setTitle("允许“百度EasyDL”使用数据？")
                .setMessage("可能同时包含无线局域网和蜂窝移动数据")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sp = getSharedPreferences("demo_auth_info",
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("isAgree", true);
                        editor.commit();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                startUICameraActivity();
                            }
                        }).start();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("不允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        initConfig();

        TextView modelNameText = findViewById(R.id.model_text);
        modelNameText.setText(modelName);

        startUIActivityBtn = findViewById(R.id.start_ui_activity);
        startUIActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("demo_auth_info", Context.MODE_PRIVATE);
                boolean hasAgree = sp.getBoolean("isAgree", false);
                boolean checkChip = checkChip();
                if (hasAgree) {
                    Log.i(this.getClass().getSimpleName(), "socList: " + socList.toString()
                            + ", Build.HARDWARE is: " + Build.HARDWARE + "soc: " + soc);
                    if (checkChip) {
                        startUICameraActivity();
                    } else {
                        Toast.makeText(getApplicationContext(), "soc not supported, socList: " + socList.toString()
                                + ", Build.HARDWARE is: " + Build.HARDWARE, Toast.LENGTH_LONG).show();
                    }
                } else {
                    agreementDialog.show();
                }
            }
        });
    }

    private boolean checkChip() {
        if (socList.contains(Consts.SOC_DSP) && Build.HARDWARE.equalsIgnoreCase("qcom")) {
            soc = Consts.SOC_DSP;
            return true;
        }
        if (socList.contains(Consts.SOC_ADRENO_GPU) && Build.HARDWARE.equalsIgnoreCase("qcom")) {
            soc = Consts.SOC_ADRENO_GPU;
            return true;
        }
        if (socList.contains(Consts.SOC_NPU) && Build.HARDWARE.contains("kirin980")) {
            soc = "npu200";
            return true;
        }
        if (socList.contains(Consts.SOC_NPU_VINCI)
                && (Build.HARDWARE.contains("kirin810") || Build.HARDWARE.contains("kirin820")
                    || Build.HARDWARE.contains("kirin990") || Build.HARDWARE.contains("kirin985"))) {
            soc = Consts.SOC_NPU_VINCI;
            return true;
        }
        if (socList.contains(Consts.SOC_ARM_GPU)) {
            try {
                if (InferManager.isSupportOpencl()) {
                    soc = Consts.SOC_ARM_GPU;
                    return true;
                }
            } catch (CallException e) {
                Toast.makeText(getApplicationContext(), e.getErrorCode() + ", " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (socList.contains(Consts.SOC_ARM)) {
            soc = Consts.SOC_ARM;
            return true;
        }
        return false;
    }

    private void startUICameraActivity() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra("name", modelName);
        intent.putExtra("model_type", modelType);
        intent.putExtra("serial_num", SERIAL_NUM);

        intent.putExtra("soc", soc);
        startActivityForResult(intent, 1);
    }

    /**
     * demo文件夹非必需，如果没有默认使用通用arm的配置
     */
    private void initConfig() {
        if (initConfigFromDemoConfig()) {
            Log.i(TAG, "Initialized by demo/config.json");
            return;
        }
        if (initConfigFromDemoConf()) {
            Log.i(TAG, "Initialized by demo/conf.json");
            return;
        }

        /* 从infer/读配置 */
        String confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(),
                Consts.ASSETS_DIR_ARM + "/conf.json");
        if (!TextUtils.isEmpty(confJson)) {
            try {
                JSONObject confObj = new JSONObject(confJson);
                modelName = confObj.optString("modelName", "");

                String str = confObj.optString("soc", Consts.SOC_ARM);
                String[] socs = str.split(",");
                socList.addAll(Arrays.asList(socs));

                modelType = confObj.getInt("modelType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(),
                    Consts.ASSETS_DIR_ARM + "/infer_cfg.json");
            try {
                JSONObject confObj = new JSONObject(confJson);
                socList.add(Consts.SOC_ARM);
                modelType = confObj.getJSONObject("model_info").getInt("model_kind");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Initialized by arm#*.json");
    }

    /**
     * 原有的
     */
    private boolean initConfigFromDemoConfig() {
        String confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(), "demo/config.json");
        if (TextUtils.isEmpty(confJson)) {
            return false;
        }
        try {
            JSONObject confObj = new JSONObject(confJson);
            modelName = confObj.optString("modelName", "");

            String str = confObj.optString("soc", Consts.SOC_ARM);
            String[] socs = str.split(",");
            socList.addAll(Arrays.asList(socs));

            modelType = confObj.getInt("modelType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 开放模型
     */
    private boolean initConfigFromDemoConf() {
        String confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(), "demo/conf.json");
        if (TextUtils.isEmpty(confJson)) {
            return false;
        }
        try {
            JSONObject confObj = new JSONObject(confJson);
            modelName = confObj.optString("modelName", "");
            socList.add(Consts.SOC_ARM);

            String inferCfgJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(),
                    Consts.ASSETS_DIR_ARM + "/infer_cfg.json");
            if (TextUtils.isEmpty(inferCfgJson)) {
                return false;
            }
            JSONObject inferCfgObj = new JSONObject(inferCfgJson);
            modelType = inferCfgObj.getJSONObject("model_info").getInt("model_kind");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
}
