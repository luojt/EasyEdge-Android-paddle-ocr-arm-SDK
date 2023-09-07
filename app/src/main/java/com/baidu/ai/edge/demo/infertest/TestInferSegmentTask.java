package com.baidu.ai.edge.demo.infertest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;

import com.baidu.ai.edge.core.infer.InferConfig;
import com.baidu.ai.edge.core.infer.InferManager;
import com.baidu.ai.edge.core.segment.SegmentationResultModel;
import com.baidu.ai.edge.demo.base.BaseTestTask;

import java.io.InputStream;
import java.util.List;

/**
 * 通用arm 分割
 */
public class TestInferSegmentTask extends BaseTestTask<Void, CharSequence, CharSequence> {
    private static final int NUM_OF_RUNS = 1;
    private static final int NUM_OF_API_CALLS = 1;

    public TestInferSegmentTask(Context context, TextView tv, String serialNum) {
        super(context, tv, serialNum);
    }

    @Override
    protected CharSequence doInBackground(Void... voids) {
        publishProgress("\n\nARM Segmentation\n");
        try {
            for (int i = 0; i < NUM_OF_RUNS; i++) {
                /* 以下逻辑请放在同一个线程里执行，比如使用ThreadHandler */

                publishProgress("\nStart running: " + i + "\n");

                /* 1. 准备配置类，初始化Manager类。可以在onCreate或onResume中触发，请在非UI线程里调用 */
                InferConfig config = new InferConfig(context.getAssets(), "infer");
                InferManager manager = new InferManager(context, config, serialNum);

                /* 2.1 准备图片，作为Bitmap输入 */
                InputStream is = context.getAssets().open("test_segment.jpg");
                Bitmap image = BitmapFactory.decodeStream(is);
                is.close();
                pLog("Image size: " + image.getWidth() + "*" + image.getHeight());

                /* 2.2 推理图片及解析结果 */
                List<SegmentationResultModel> results = null;
                String resStr;
                for (int j = 0; j < NUM_OF_API_CALLS; j++) {
                    // 在模型销毁前可以不断调用。但是不支持多线程。
                    results = manager.segment(image);

                    // 解析结果
                    if (results != null) {
                        resStr = "{size:" + results.size() + ", firstRes:{";
                        if (results.size() > 0) {
                            resStr += "labelName:" + results.get(0).getLabel() + ", "
                                    + "confidence:" + results.get(0).getConfidence() + ", "
                                    + "box:" + results.get(0).getBox();
                        }
                        resStr += "}}";
                    } else {
                        resStr = "{}";
                    }
                    publishProgress("Predict " + j + ": " + resStr + "\n");
                }

                /* 3. 销毁模型。可以在onDestroy或onPause中触发，请在非UI线程里调用 */
                manager.destroy();
                publishProgress("Finish running\n");
            }

            return RESULT_FIN;
        } catch (Exception e) {
            pError(e);
            return genErrStr("ERROR: " + e.getMessage());
        }
    }
}
