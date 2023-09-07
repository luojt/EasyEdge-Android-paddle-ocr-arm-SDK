package com.baidu.ai.edge.demo.snpetest;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.ai.edge.core.util.FileUtil;
import com.baidu.ai.edge.demo.R;
import com.baidu.ai.edge.demo.base.BaseActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

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
                        if (dir.equals("snpe")) {
                            /* 高通骁龙DSP */

                            String configJson = FileUtil.readAssetFileUtf8String(getAssets(),
                                    "demo/config.json");
                            JSONObject jsonObject = new JSONObject(configJson);
                            int modelType = jsonObject.getInt("modelType");
                            Log.i(TAG, "Model type is " + modelType);

                            AsyncTask<Void, CharSequence, CharSequence> at = null;
                            switch (modelType) {
                                case 1:
                                    at = new TestSnpeClassifyTask(app, tv, SERIAL_NUM);
                                    break;
                                case 401:
                                case 2:
                                    at = new TestSnpeDetectionTask(app, tv, SERIAL_NUM);
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "timestamp:" + System.currentTimeMillis() + " " + format.format(new Date()));
    }
}
