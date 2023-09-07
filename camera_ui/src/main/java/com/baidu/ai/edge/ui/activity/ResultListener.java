package com.baidu.ai.edge.ui.activity;

import com.baidu.ai.edge.ui.view.model.BasePolygonResultModel;
import com.baidu.ai.edge.ui.view.model.BaseRectBoundResultModel;
import com.baidu.ai.edge.ui.view.model.BaseResultModel;
import com.baidu.ai.edge.ui.view.model.ClassifyResultModel;
import com.baidu.ai.edge.ui.view.model.DetectResultModel;

import java.util.List;

/**
 * Created by ruanshimin on 2018/11/12.
 */

public interface ResultListener {
    interface ClassifyListener {
        void onResult(List<ClassifyResultModel> models);
    }
    interface SegmentListener {
        void onResult(List<BasePolygonResultModel> models);
    }

    interface DetectListener {
        void onResult(List<BasePolygonResultModel> models);
    }

    interface OcrListener {
        void onResult(List<BasePolygonResultModel> models);
    }

    interface ListListener<T extends BaseResultModel> {
        void onResult(List<BasePolygonResultModel> models);
    }

    interface PoseListener {
        void onResult(List<BasePolygonResultModel> models);
    }
}
