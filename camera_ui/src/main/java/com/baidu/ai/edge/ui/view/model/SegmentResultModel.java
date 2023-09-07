package com.baidu.ai.edge.ui.view.model;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.List;

/**
 * Created by ruanshimin on 2018/5/13.
 */

public class SegmentResultModel extends BasePolygonResultModel {

    public SegmentResultModel() {
        super();
    }

    public SegmentResultModel(int index, String name, float confidence, List<Point> bounds, byte[] mask) {
        super(index, name, confidence, bounds);
        this.setMask(mask);
    }

    public SegmentResultModel(int index, String name, float confidence, List<Point> bounds) {
        super(index, name, confidence, bounds);
    }


}
