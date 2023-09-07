package com.baidu.ai.edge.demo.infertest;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.ai.edge.core.base.Consts;
import com.baidu.ai.edge.core.util.FileUtil;
import com.baidu.ai.edge.demo.R;
import com.baidu.ai.edge.demo.base.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    // 请替换为你自己的序列号
    private static final String SERIAL_NUM = "XXXX-XXXX-XXXX-XXXX";

    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_main_test);
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState) {
        Button b = findViewById(R.id.button2);
        final Application app = getApplication();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, app.getApplicationInfo().nativeLibraryDir);
                TextView tv = findViewById(R.id.sample_text);
                try {
                    String[] dir_arr = app.getAssets().list("");
                    for (String dir : dir_arr) {
                        if (dir.equals("infer")) {
                            /* 通用ARM */
                            int modelType = getModelType();
                            Log.i(TAG, "Model type is " + modelType);

                            AsyncTask<Void, CharSequence, CharSequence> at = null;
                            switch (modelType) {
                                case 1:
                                    at = new TestInferClassifyTask(app, tv, SERIAL_NUM);
                                    break;
                                case 401:
                                case 2:
                                    at = new TestInferDetectionTask(app, tv, SERIAL_NUM);
                                    break;
                                case 100:
                                    at = new TestInferOcrTask(app, tv, SERIAL_NUM);
                                    break;
                                case 6:
                                    at = new TestInferSegmentTask(app, tv, SERIAL_NUM);
                                    break;
                                case 402:
                                    at = new TestInferPoseTask(app, tv, SERIAL_NUM);
                                    break;
                            }
                            if (at != null) {
                                at.execute();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private int getModelType() {
        int modelType = -1;
        String confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(), "demo/config.json");
        if (TextUtils.isEmpty(confJson)) {
            confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(),
                    Consts.ASSETS_DIR_ARM + "/conf.json");
        }
        if (!TextUtils.isEmpty(confJson)) {
            try {
                JSONObject confObj = new JSONObject(confJson);
                modelType = confObj.getInt("modelType");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            confJson = FileUtil.readAssetsFileUTF8StringIfExists(getAssets(),
                    Consts.ASSETS_DIR_ARM + "/infer_cfg.json");
            try {
                JSONObject confObj = new JSONObject(confJson);
                modelType = confObj.getJSONObject("model_info").getInt("model_kind");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return modelType == -1 ? Consts.MODEL_TYPE_CLASSIFY : modelType;
    }
}
