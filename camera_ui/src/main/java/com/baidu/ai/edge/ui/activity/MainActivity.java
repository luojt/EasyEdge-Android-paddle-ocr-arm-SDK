package com.baidu.ai.edge.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ai.edge.ui.R;
import com.baidu.ai.edge.ui.camera.CameraListener;
import com.baidu.ai.edge.ui.layout.ActionBarLayout;
import com.baidu.ai.edge.ui.util.ImageUtil;
import com.baidu.ai.edge.ui.util.StringUtil;
import com.baidu.ai.edge.ui.util.ThreadPoolManager;
import com.baidu.ai.edge.ui.util.UiLog;
import com.baidu.ai.edge.ui.view.PreviewDecoratorView;
import com.baidu.ai.edge.ui.view.PreviewView;
import com.baidu.ai.edge.ui.view.ResultListView;
import com.baidu.ai.edge.ui.view.ResultMaskView;
import com.baidu.ai.edge.ui.view.adapter.ClassifyResultAdapter;
import com.baidu.ai.edge.ui.view.adapter.DetectResultAdapter;
import com.baidu.ai.edge.ui.view.model.BasePolygonResultModel;
import com.baidu.ai.edge.ui.view.model.BaseResultModel;
import com.baidu.ai.edge.ui.view.model.ClassifyResultModel;
import com.baidu.ai.edge.ui.view.model.DetectResultModel;
import com.baidu.ai.edge.ui.view.model.OcrViewResultModel;
import com.baidu.ai.edge.ui.view.model.PoseViewResultModel;
import com.baidu.ai.edge.ui.view.model.SegmentResultModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ruanshimin on 2018/5/3.
 */

public abstract class MainActivity extends AppCompatActivity {

    public static final int MODEL_OCR = 100;

    public static final int MODEL_FACE_DETECT = 401;

    public static final int MODEL_POSE = 402;

    public static final int MODEL_SEGMENT = 6;

    public static final int MODEL_DETECT = 2;

    public static final int MODEL_CLASSIFY = 1;

    public static final int MODEL_SEMANTIC_SEGMENT = 14;


    protected int model;

    static final int ACTION_TYPE_SCAN = 0;

    static final int ACTION_TYPE_TAKE = 1;

    private int actionType = ACTION_TYPE_TAKE;

    private ActionBarLayout actionBarLayout;
    private PreviewView mPreviewView;
    private ImageView switchSideButton;
    private ImageView takePictureButton;
    private ImageView albumSelectButton;

    private ViewGroup realtimeControlViewGroup;

    private ResultMaskView resultMaskView;
    private ResultMaskView realtimeResultMaskView;

    private SeekBar confidenceSeekbar;
    private TextView seekbarText;

    private SeekBar realtimeConfidenceSeekbar;
    private TextView realtimeSeekbarText;

    private ImageView realtimeToggleButton;

    private ResultListView detectResultView;
    private ResultListView classifyResultView;

    private PreviewDecoratorView mPreviewDecoratorView;

    boolean isRealtimeStatusRunning = false;

    private ImageView backInResult;
    private ImageView backInPreview;

    private TextView actionTakePictureBtn;
    private TextView actionRealtimeBtn;

    private View mCameraPageView;
    private ViewGroup mResultPageView;

    private ViewGroup resultTablePopview;

    private ViewGroup recLoading;

    private ImageView resultImage;

    private static final int REQUEST_PERMISSION_CODE_CAMERA = 100;
    private static final int REQUEST_PERMISSION_CODE_STORAGE = 101;

    private static final int INTENT_CODE_PICK_IMAGE = 100;

    protected static final int PAGE_CAMERA = 100;
    protected static final int PAGE_RESULT = 101;

    protected int pageCode;

    private float resultConfidence;

    private float realtimeConfidence;


    Handler uiHandler;

    private String name;

    public abstract void onActivityCreate();

    public abstract void onActivityDestory();

    public abstract void onDetectBitmap(Bitmap bitmap, float confidence,
                                        ResultListener.DetectListener listener);

    public abstract void onClassifyBitmap(Bitmap bitmap, float confidence,
                                          ResultListener.ClassifyListener listener);

    public abstract void onSegmentBitmap(Bitmap bitmap, float confidence,
                                         ResultListener.SegmentListener listener);

    public abstract void onOcrBitmap(Bitmap bitmap, float confidence,
                                     ResultListener.OcrListener listener);

    private List<BasePolygonResultModel> poseResultModelCache;

    public abstract void dumpDetectResult(List<DetectResultModel> model,
                                          Bitmap bitmap, float min);

    public abstract void dumpClassifyResult(List<ClassifyResultModel> model,
                                            Bitmap bitmap, float min);

    protected void setConfidence(float value) {
        resultConfidence = value;
        realtimeConfidence = value;
        confidenceSeekbar.setProgress((int) (resultConfidence * 100));
        realtimeConfidenceSeekbar.setProgress((int) (realtimeConfidence * 100));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = getIntent().getStringExtra("name");
        model = getIntent().getIntExtra("model_type", MODEL_DETECT);

        requestPermissionCamera();
    }

    private void init() {
        uiHandler = new Handler(getMainLooper());
        pageCode = PAGE_CAMERA;

        setContentView(R.layout.ui_activity_main);
        actionBarLayout = findViewById(R.id.action_bar);
        resultImage = findViewById(R.id.result_image);
        mCameraPageView = findViewById(R.id.camera_page);
        mResultPageView = findViewById(R.id.result_page);
        mPreviewDecoratorView = findViewById(R.id.preview_decorator_view);

        resultMaskView = findViewById(R.id.result_mask);
        resultMaskView.setHandler(uiHandler);
        realtimeResultMaskView = findViewById(R.id.realtime_result_mask);
        realtimeResultMaskView.setHandler(uiHandler);

        resultMaskView = findViewById(R.id.result_mask);

        seekbarText = findViewById(R.id.seekbar_text);
        realtimeSeekbarText = findViewById(R.id.realtime_seekbar_text);
        realtimeControlViewGroup = findViewById(R.id.realtime_confidence_control);

        recLoading = findViewById(R.id.rec_loading);

        resultTablePopview = findViewById(R.id.result_table_popview);
        confidenceSeekbar = findViewById(R.id.confidence_seekbar);
        realtimeConfidenceSeekbar = findViewById(R.id.realtime_confidence_seekbar);
        ((TextView) findViewById(R.id.model_name)).setText(name);

        initSeekbarArea();
        addListener();
        updateRealtimeControlViewGroup();
        updateRealtimeResultPopViewGroup();

        setAutoTakePictureProcess();

        onActivityCreate();
    }

    private void initSeekbarArea() {
        if (model == MODEL_CLASSIFY) {
            setConfidence(0);
        }
    }

    private void updateRealtimeControlViewGroup() {
        if (actionType == ACTION_TYPE_SCAN) {
            realtimeControlViewGroup.setVisibility(View.VISIBLE);
        } else {
            realtimeControlViewGroup.setVisibility(View.GONE);
        }
    }

    public abstract void onPoseBitmap(Bitmap bitmap, float confidence, ResultListener.PoseListener listener);


    private volatile boolean isProcessingAutoTakePicture = false;

    protected boolean canAutoRun = false;

    private boolean autoTakeFlag = true;

    private void updateRealtimeResultPopViewGroup() {
        if (model == MODEL_CLASSIFY) {
            if (actionType == ACTION_TYPE_SCAN && isRealtimeStatusRunning) {
                resultTablePopview.setVisibility(View.VISIBLE);
            } else {
                resultTablePopview.setVisibility(View.GONE);
            }
        }
        List<Integer> modelList = Arrays.asList(
                MODEL_DETECT, MODEL_SEGMENT, MODEL_OCR, MODEL_FACE_DETECT, MODEL_POSE, MODEL_SEMANTIC_SEGMENT);
        if (modelList.contains(model)) {
            resultTablePopview.setVisibility(View.GONE);
            if (actionType == ACTION_TYPE_SCAN && !isRealtimeStatusRunning) {
                realtimeResultMaskView.clear();
            }
        }

    }

    protected void showMessage(final String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setTitle("提示")
                        .setMessage(msg)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

    }

    protected void showMessage(final int errorCode, final String msg) {
        showMessage("errorcode: " + errorCode + ", " + msg);
    }

    protected void setTakePictureButtonAvailable(boolean available) {
        if (takePictureButton == null) {
            return;
        }
        if (available) {
            takePictureButton.setColorFilter(null);
        } else {
            takePictureButton.setColorFilter(Color.GRAY);
        }
    }

    private void setAutoTakePictureProcess() {
        ThreadPoolManager.execute(new Runnable() {
            @Override
            public void run() {
                while (autoTakeFlag) {
                    synchronized (this) {
                        if (!isProcessingAutoTakePicture && isRealtimeStatusRunning) {
                            mPreviewView.takePicture(new CameraListener.TakePictureListener() {
                                @Override
                                public void onTakenPicture(final Bitmap bitmap) {
                                    UiLog.info("auto picture has processed");
                                    isProcessingAutoTakePicture = true;
                                    resolveDetectResult(bitmap,
                                            realtimeConfidence, new ResultListener.ListListener<DetectResultModel>() {
                                                @Override
                                                public void onResult(List<BasePolygonResultModel> results) {
                                                    if (isRealtimeStatusRunning && results != null) {
                                                        if (model == MODEL_DETECT || model == MODEL_FACE_DETECT) {
                                                            if (results.size() > LIMIT_SIZE_AUTO) {
                                                                Collections.sort(results,
                                                                        new Comparator<BasePolygonResultModel>() {
                                                                            @Override
                                                                            public int compare(
                                                                                    BasePolygonResultModel o1,
                                                                                    BasePolygonResultModel o2) {
                                                                                float c = o2.getConfidence()
                                                                                        - o1.getConfidence();
                                                                                if (c > 0) {
                                                                                    return 1;
                                                                                } else if (c < 0) {
                                                                                    return -1;
                                                                                } else {
                                                                                    return 0;
                                                                                }
                                                                            }
                                                                        });
                                                                results = results.subList(0, LIMIT_SIZE_AUTO);
                                                            }
                                                            realtimeResultMaskView.setPolygonListInfo(results,
                                                                    bitmap.getWidth(), bitmap.getHeight());
                                                        } else {
                                                            if (model == MODEL_POSE) {
                                                                realtimeResultMaskView.setPolygonListInfo(results,
                                                                        bitmap.getWidth(), bitmap.getHeight());
                                                            }
                                                            if (model == MODEL_OCR) {
                                                                realtimeResultMaskView.setPolygonListInfo(results,
                                                                        bitmap.getWidth(), bitmap.getHeight());
                                                            }
                                                            if (results.size() > LIMIT_SIZE_AUTO) {
                                                                results = results.subList(0, LIMIT_SIZE_AUTO);
                                                            }
                                                            if (model == MODEL_CLASSIFY) {
                                                                fillClassifyList(results);
                                                            }
                                                            if (model == MODEL_SEGMENT) {
                                                                realtimeResultMaskView.setPolygonListInfo(results,
                                                                        bitmap.getWidth(), bitmap.getHeight());
                                                            }

                                                        }

                                                    }
                                                    isProcessingAutoTakePicture = false;
                                                }
                                            });

                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private boolean isLoading = false;

    private void toggleLoading(final boolean isLoading) {
        this.isLoading = isLoading;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isLoading) {
                    mPreviewView.stopPreview();
                    recLoading.setVisibility(View.VISIBLE);
                } else {
                    mPreviewView.start();
                    recLoading.setVisibility(View.GONE);
                }
            }
        });
    }

    private void toggleDecoratorView(boolean isVisible) {
        if (isVisible) {
            mPreviewDecoratorView.setVisibility(View.VISIBLE);
        } else {
            mPreviewDecoratorView.setVisibility(View.GONE);
        }
    }

    /**
     * 切换扫描或者拍照模式
     *
     * @param isTakePicture
     */
    private void toggleOperationBtns(boolean isTakePicture) {
        if (isTakePicture) {
            takePictureButton.setVisibility(View.VISIBLE);
            albumSelectButton.setVisibility(View.VISIBLE);
            realtimeToggleButton.setVisibility(View.GONE);

            if (isRealtimeStatusRunning) {
                toggleRealtimeStatus();
            }

            actionType = ACTION_TYPE_TAKE;
        } else {
            takePictureButton.setVisibility(View.INVISIBLE);
            albumSelectButton.setVisibility(View.GONE);
            realtimeToggleButton.setVisibility(View.VISIBLE);
            actionType = ACTION_TYPE_SCAN;
        }
        updateRealtimeResultPopViewGroup();
        updateRealtimeControlViewGroup();
        clearMaskInfo();
    }

    /**
     * 清除实时
     */
    private void clearMaskInfo() {
        if (model == MODEL_DETECT || model == MODEL_SEGMENT || model == MODEL_FACE_DETECT) {
            realtimeResultMaskView.clear();
        }

        if (model == MODEL_CLASSIFY) {
            classifyResultView.clear();
        }
    }

    private void toggleRealtimeStatus() {
        if (canAutoRun || isRealtimeStatusRunning) {
            isRealtimeStatusRunning = !isRealtimeStatusRunning;
            updateRealtimeResultPopViewGroup();
            toggleRealtimeStyle();
        }
    }


    private void toggleRealtimeStyle() {
        if (isRealtimeStatusRunning) {
            mPreviewDecoratorView.setStatus(true);
            realtimeToggleButton.setImageResource(R.drawable.realtime_stop_btn);
        } else {
            mPreviewDecoratorView.setStatus(false);
            realtimeToggleButton.setImageResource(R.drawable.realtime_start_btn);
        }
    }

    private void setActionBtnHighlight(TextView btn) {
        btn.setBackgroundResource(R.color.bk_black);
        ColorStateList color = getResources().getColorStateList(R.color.textColorHighlight);
        btn.setTextColor(color);
    }

    private void setActionBtnDefault(TextView btn) {
        btn.setBackgroundResource(R.color.bk_black);
        ColorStateList color = getResources().getColorStateList(R.color.textColor);
        btn.setTextColor(color);
    }

    private void openAlum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_CODE_PICK_IMAGE);
    }

    private void showResultPage(final Bitmap bitmap) {

        ThreadPoolManager.executeSingle(new Runnable() {
            @Override
            public void run() {
                resolveDetectResult(bitmap, 0,
                        new ResultListener.ListListener<DetectResultModel>() {
                            @Override
                            public void onResult(List<BasePolygonResultModel> results) {
                                if (results != null) {
                                    updateResultImageAndList(results, bitmap, resultConfidence);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toggleLoading(false);
                                            mCameraPageView.setVisibility(View.GONE);
                                            mResultPageView.setVisibility(View.VISIBLE);
                                            resultImage.setImageBitmap(bitmap);
                                            pageCode = PAGE_RESULT;
                                            detectBitmapCache = bitmap;
                                        }
                                    });
                                } else {
                                    toggleLoading(false);
                                }
                            }
                        });

            }
        });
    }

    private Bitmap detectBitmapCache;

    private List<BasePolygonResultModel> detectResultModelCache;

    private List<BasePolygonResultModel> segmentResultModelCache;

    private List<BasePolygonResultModel> ocrResultModelCache;

    private void addListener() {
        mPreviewView = findViewById(R.id.preview_view);

        switchSideButton = findViewById(R.id.switchSide);
        takePictureButton = findViewById(R.id.takePicture);
        setTakePictureButtonAvailable(false);
        albumSelectButton = findViewById(R.id.albumSelect);

        actionTakePictureBtn = findViewById(R.id.action_takepicture_btn);
        actionRealtimeBtn = findViewById(R.id.action_realtime_btn);

        backInResult = findViewById(R.id.back_in_result);
        backInPreview = findViewById(R.id.back_in_preview);

        realtimeToggleButton = findViewById(R.id.realtime_toggle_btn);

        detectResultView = findViewById(R.id.result_list_view);
        detectResultView.setHandler(uiHandler);
        classifyResultView = findViewById(R.id.result_list_popview);
        classifyResultView.setHandler(uiHandler);


        switchSideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionType == ACTION_TYPE_SCAN && (model == MODEL_DETECT || model == MODEL_FACE_DETECT)) {
                    realtimeResultMaskView.clear();
                }
                if (actionType == ACTION_TYPE_SCAN && model == MODEL_CLASSIFY) {
                    classifyResultView.clear();
                }
                if (actionType == ACTION_TYPE_SCAN && model == MODEL_SEGMENT) {
                    realtimeResultMaskView.clear();
                }
                mPreviewView.switchSide();
            }
        });


        confidenceSeekbar.setMax(100);
        confidenceSeekbar.setProgress((int) (resultConfidence * 100));
        seekbarText.setText(StringUtil.formatFloatString(resultConfidence));
        confidenceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                resultConfidence = seekBar.getProgress() / 100f;
                seekbarText.setText(StringUtil.formatFloatString(resultConfidence));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if ((model == MODEL_DETECT || model == MODEL_FACE_DETECT) && detectResultModelCache != null) {
                    updateResultImageAndList(detectResultModelCache, detectBitmapCache, resultConfidence);
                }
                if (model == MODEL_CLASSIFY && classifyResultModelCache != null) {
                    updateResultImageAndList(classifyResultModelCache, detectBitmapCache, resultConfidence);
                }
                if (model == MODEL_SEGMENT && segmentResultModelCache != null) {
                    updateResultImageAndList(segmentResultModelCache, detectBitmapCache, resultConfidence);
                }
                if (model == MODEL_OCR && ocrResultModelCache != null) {
                    updateResultImageAndList(ocrResultModelCache, detectBitmapCache, resultConfidence);
                }
                if (model == MODEL_POSE && poseResultModelCache != null) {
                    updateResultImageAndList(poseResultModelCache, detectBitmapCache, resultConfidence);
                }
            }
        });


        realtimeConfidenceSeekbar.setMax(100);
        realtimeConfidenceSeekbar.setProgress((int) (realtimeConfidence * 100));
        realtimeSeekbarText.setText(StringUtil.formatFloatString(resultConfidence));
        realtimeConfidenceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                realtimeConfidence = seekBar.getProgress() / 100f;
                realtimeSeekbarText.setText(StringUtil.formatFloatString(realtimeConfidence));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });


        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoading) {
                    return;
                }
                if (isProcessingAutoTakePicture) {
                    showMessage("请等待实时识别完成");
                    return;
                }
                mPreviewView.takePicture(new CameraListener.TakePictureListener() {
                    @Override
                    public void onTakenPicture(Bitmap bitmap) {
                        UiLog.info("picture has taken");
                        toggleLoading(true);
                        showResultPage(bitmap);
                    }
                });
            }
        });

        actionTakePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActionBtnHighlight(actionTakePictureBtn);
                setActionBtnDefault(actionRealtimeBtn);
                toggleDecoratorView(false);
                toggleOperationBtns(true);
            }
        });

        actionRealtimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canAutoRun) {
                    setActionBtnHighlight(actionRealtimeBtn);
                    setActionBtnDefault(actionTakePictureBtn);
                    toggleDecoratorView(true);
                    toggleOperationBtns(false);
                    toggleRealtimeStatus();
                } else {
                    showMessage("模型初始化中，请稍后");
                }
            }
        });

        realtimeToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRealtimeStatus();
            }
        });

        albumSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoading) {
                    return;
                }
                toggleLoading(true);
                requestPermissionAlbum();
            }
        });

        backInResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToCamera();
            }
        });

        backInPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canAutoRun) {
                    mPreviewView.stopPreview();
                    finish();
                } else {
                    showMessage("模型未初始化");
                }
            }
        });
    }

    private List<ClassifyResultModel> classifyResultModelCache;

    private void resolveDetectResult(Bitmap bitmap, float confidence,
                                     final ResultListener.ListListener listener) {
        if (model == MODEL_DETECT || model == MODEL_FACE_DETECT) {
            onDetectBitmap(bitmap, confidence, new ResultListener.DetectListener() {
                @Override
                public void onResult(List<BasePolygonResultModel> models) {
                    if (models == null) {
                        listener.onResult(null);
                        return;
                    }
                    detectResultModelCache = models;
                    listener.onResult(models);
                }
            });

        }
        if (model == MODEL_CLASSIFY) {
            onClassifyBitmap(bitmap, confidence, new ResultListener.ClassifyListener() {
                @Override
                public void onResult(List<ClassifyResultModel> models) {
                    if (models == null) {
                        listener.onResult(null);
                        return;
                    }

                    classifyResultModelCache = models;
                    listener.onResult(models);
                }
            });
        }
        if (model == MODEL_SEGMENT || model == MODEL_SEMANTIC_SEGMENT) {

            onSegmentBitmap(bitmap, confidence, new ResultListener.SegmentListener() {

                @Override
                public void onResult(List<BasePolygonResultModel> models) {
                    if (models == null) {
                        listener.onResult(null);
                        return;
                    }
                    segmentResultModelCache = models;
                    listener.onResult(models);
                }
            });
        }

        if (model == MODEL_OCR) {

            onOcrBitmap(bitmap, confidence, new ResultListener.OcrListener() {

                @Override
                public void onResult(List<BasePolygonResultModel> models) {
                    if (models == null) {
                        listener.onResult(null);
                        return;
                    }
                    ocrResultModelCache = models;
                    listener.onResult(models);
                }
            });
        }

        if (model == MODEL_POSE) {
            onPoseBitmap(bitmap, confidence, new ResultListener.PoseListener() {
                @Override
                public void onResult(List<BasePolygonResultModel> models) {
                    if (models == null) {
                        listener.onResult(null);
                        return;
                    }
                    poseResultModelCache = models;
                    listener.onResult(models);
                }
            });
        }
    }

    private void updateResultImageAndList(List<? extends BaseResultModel> results, final Bitmap bitmap, float min) {

        if (model == MODEL_DETECT || model == MODEL_FACE_DETECT) {
            List<DetectResultModel> filteredList
                    = filterDetectListByConfidence((List<DetectResultModel>) results, min);
            fillDetectList(filteredList);
            fillRectImageView(filteredList, bitmap);
        }

        if (model == MODEL_CLASSIFY) {
            List<ClassifyResultModel> filteredList
                    = filterClassifyListByConfidence((List<ClassifyResultModel>) results, min);
            fillDetectList(filteredList);
        }

        if (model == MODEL_SEGMENT || model == MODEL_SEMANTIC_SEGMENT) {
            List<SegmentResultModel> filteredList;
            if (model == MODEL_SEGMENT) {
                filteredList = filterSegmentListByConfidence((List<SegmentResultModel>) results, min);
                fillDetectList(filteredList);
            } else {
                // 语义分割不展示标签
                filteredList = (List<SegmentResultModel>) results;
            }
            fillRectImageView(filteredList, bitmap);
        }

        if (model == MODEL_OCR) {
            List<OcrViewResultModel> filteredList
                    = filterOcrListByConfidence((List<OcrViewResultModel>) results, min);
            fillDetectList(filteredList);
            fillRectImageView(filteredList, bitmap);
        }

        if (model == MODEL_POSE) {
            List<PoseViewResultModel> filteredList
                    = filterPoseListByConfidence((List<PoseViewResultModel>) results, min);
            fillDetectList(filteredList);
            fillRectImageView(filteredList, bitmap);
        }

    }

    private static final int LIMIT_SIZE = 50;
    private static final int LIMIT_SIZE_AUTO = 5;

    private List<ClassifyResultModel> filterClassifyListByConfidence(List<ClassifyResultModel> results, float min) {
        List<ClassifyResultModel> filteredList = new ArrayList<>();
        int size = results.size() > LIMIT_SIZE ? LIMIT_SIZE : results.size();
        int j = 0;
        for (int i = 0; i < size; i++) {
            ClassifyResultModel mBaseResultModel = results.get(i);
            if (mBaseResultModel.getConfidence() > min) {
                filteredList.add(new ClassifyResultModel(++j, mBaseResultModel.getName(),
                        mBaseResultModel.getConfidence()));
            }
        }
        return filteredList;
    }

    private List<DetectResultModel> filterDetectListByConfidence(List<DetectResultModel> results, float min) {
        List<DetectResultModel> filteredList = new ArrayList<>();
        List<DetectResultModel> filteredResults = new ArrayList<>(results);

        /* 去掉低置信度结果 */
        Iterator<DetectResultModel> iterator = filteredResults.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getConfidence() <= min) {
                iterator.remove();
            }
        }

        int size = filteredResults.size() > LIMIT_SIZE ? LIMIT_SIZE : filteredResults.size();
        int j = 0;
        if (size == LIMIT_SIZE) {
            /* 计算几个label */
            int labelNum = 1;
            for (int i = 1; i < filteredResults.size(); i++) {
                if (!filteredResults.get(i).getName().equals(filteredResults.get(i - 1).getName())) {
                    labelNum++;
                }
            }
            // 如果标签数量大于最大显示数量，使divide至少等于1
            int divide = size / Math.min(labelNum, size);
            int remain = divide;
            String lastLabelName = "";
            for (int i = 0; i < filteredResults.size(); i++) {
                DetectResultModel detectResultModel = filteredResults.get(i);
                if (!detectResultModel.getName().equals(lastLabelName)) {
                    /* 如果当前结果和上一个结果的标签不相同，开始新一个标签的计算 */
                    filteredList.add(new DetectResultModel(++j, detectResultModel.getName(),
                            detectResultModel.getConfidence(), detectResultModel.getRect()));
                    lastLabelName = detectResultModel.getName();
                    remain = divide;
                } else if (remain > 0) {
                    /* 如果当前结果与上一个结果的标签相同，remain>0才可add */
                    filteredList.add(new DetectResultModel(++j, detectResultModel.getName(),
                            detectResultModel.getConfidence(), detectResultModel.getRect()));
                    remain--;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                DetectResultModel mDetectResultModel = filteredResults.get(i);
                filteredList.add(new DetectResultModel(++j, mDetectResultModel.getName(),
                        mDetectResultModel.getConfidence(), mDetectResultModel.getRect()));
            }
        }
        return filteredList;
    }

    private List<OcrViewResultModel> filterOcrListByConfidence(List<OcrViewResultModel> results, float min) {
        List<OcrViewResultModel> filteredList = new ArrayList<>();
//        int size = results.size() > LIMIT_SIZE ? LIMIT_SIZE : results.size();
        int j = 0;
        for (int i = 0; i < results.size(); i++) {
            OcrViewResultModel mOcrResultModel = results.get(i);
            if (mOcrResultModel.getConfidence() > min) {
                filteredList.add(new OcrViewResultModel(++j, mOcrResultModel.getName(),
                        mOcrResultModel.getConfidence(), mOcrResultModel.getBounds()));
            }
        }
        return filteredList;
    }

    private List<SegmentResultModel> filterSegmentListByConfidence(List<SegmentResultModel> results, float min) {
        List<SegmentResultModel> filteredList = new ArrayList<>();
        int size = results.size() > LIMIT_SIZE ? LIMIT_SIZE : results.size();
        int j = 0;
        for (int i = 0; i < size; i++) {
            SegmentResultModel mSegmentResultModel = results.get(i);
            if (mSegmentResultModel.getConfidence() > min) {
                SegmentResultModel model = new SegmentResultModel(++j, mSegmentResultModel.getName(),
                        mSegmentResultModel.getConfidence(), mSegmentResultModel.getBounds(),
                        mSegmentResultModel.getMask());
                model.setColorId(mSegmentResultModel.getColorId());
                filteredList.add(model);
            }
        }
        return filteredList;
    }

    private List<PoseViewResultModel> filterPoseListByConfidence(List<PoseViewResultModel> results, float min) {
        List<PoseViewResultModel> filteredList = new ArrayList<>();
        int size = results.size() > LIMIT_SIZE ? LIMIT_SIZE : results.size();
        int j = 0;
        for (int i = 0; i < size; i++) {
            PoseViewResultModel model = results.get(i);
            if (model.getConfidence() > min) {
                model.setIndex(++j);
                filteredList.add(model);
            }
        }
        return filteredList;
    }

    private void fillRectImageView(List<? extends BasePolygonResultModel> result, Bitmap bitmap) {
        resultMaskView.setPolygonListInfo((List<BasePolygonResultModel>) result, bitmap.getWidth(),
                bitmap.getHeight());
    }

    /**
     * 绘制 编号 名字 置信度的列表
     *
     * @param results
     */
    private void fillDetectList(List<? extends BaseResultModel> results) {
        final DetectResultAdapter adapter = new DetectResultAdapter(this,
                R.layout.result_detect_item, (List<BaseResultModel>) results);

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                detectResultView.setAdapter(adapter);
                detectResultView.invalidate();
            }
        });
    }

    private void fillClassifyList(List<? extends BaseResultModel> results) {
        final ClassifyResultAdapter adapter = new ClassifyResultAdapter(this,
                R.layout.result_table_popview_item, results);

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                classifyResultView.setAdapter(adapter);
            }
        });
    }

    private void returnToCamera() {
        mCameraPageView.setVisibility(View.VISIBLE);
        mResultPageView.setVisibility(View.GONE);
        pageCode = PAGE_CAMERA;
        resultMaskView.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE_CAMERA:
                if (grantResults[0] == -1) {
                    Toast toast = Toast.makeText(this, "请选择权限", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                } else {
                    init();
                }
                break;
            case REQUEST_PERMISSION_CODE_STORAGE:
                if (grantResults[0] == -1) {
                    Toast toast = Toast.makeText(this, "请选择权限", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    openAlum();
                }
                break;
            default:
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String path = getRealPathFromURI(uri);
                UiLog.info("pick image url: " + path);
                Bitmap bitmap = readFile(path);
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(path);
                    int exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    int rotation = ImageUtil.exifToDegrees(exifRotation);
                    Bitmap rotateBitmap = ImageUtil.createRotateBitmap(bitmap, rotation);
                    showResultPage(rotateBitmap);
                } catch (IOException e) {
                    return;
                }
            } else {
                toggleLoading(false);
            }
        }
    }

    private Bitmap readFile(String path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            UiLog.info("pick image success");
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void requestPermissionAlbum() {
        // 判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_STORAGE);
        } else {
            openAlum();
        }
    }

    protected abstract void onSetMenu(PopupMenu actionBarMenu);

    protected abstract void onSetMenuItem(boolean isOnline);

    protected void showActionBarMenu() {
        PopupMenu actionBarMenu = new PopupMenu(MainActivity.this, actionBarLayout, Gravity.END);
        actionBarMenu.getMenuInflater().inflate(R.menu.actionbar_more_menu, actionBarMenu.getMenu());
        actionBarMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d("menu", menuItem.getOrder() + "");
                menuItem.setChecked(true);
                switch (menuItem.getOrder()) {
                    case 0:
                        onSetMenuItem(false);
                        break;
                    case 1:
                        onSetMenuItem(true);
                        break;
                    default:
                        break;
                }
                return false;
            }

        });
        onSetMenu(actionBarMenu);
        actionBarMenu.show();

    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    @Override
    public void onBackPressed() {
        isRealtimeStatusRunning = false;
        if (pageCode == PAGE_CAMERA) {
            super.onBackPressed();
        }
        if (pageCode == PAGE_RESULT) {
            returnToCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRealtimeStatusRunning = false;
        if (mPreviewView != null) {
            mPreviewView.destory();
        }
        autoTakeFlag = false;
        onActivityDestory();
    }

    private void requestPermissionCamera() {
        // 判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // 如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE_CAMERA);
        } else {

            init();
        }
    }
}
