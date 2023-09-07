package com.baidu.ai.edge.ui.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.baidu.ai.edge.ui.R;
import com.baidu.ai.edge.ui.util.StringUtil;
import com.baidu.ai.edge.ui.view.model.BaseResultModel;
import com.baidu.ai.edge.ui.view.model.DetectResultModel;

import java.util.List;

/**
 * Created by ruanshimin on 2018/5/13.
 */

public class DetectResultAdapter extends ArrayAdapter<BaseResultModel> {
    private int resourceId;

    public DetectResultAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public DetectResultAdapter(@NonNull Context context, int resource, @NonNull List<BaseResultModel> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BaseResultModel model = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView indexText = (TextView) view.findViewById(R.id.index);
        TextView nameText = (TextView) view.findViewById(R.id.name);
        TextView confidenceText = (TextView) view.findViewById(R.id.confidence);
        indexText.setText(String.valueOf(model.getIndex()));
        nameText.setText(String.valueOf(model.getName()));
        confidenceText.setText(StringUtil.formatFloatString(model.getConfidence()));
        return view;
    }
}
