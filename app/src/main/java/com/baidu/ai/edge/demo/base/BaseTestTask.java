package com.baidu.ai.edge.demo.base;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

public abstract class BaseTestTask<Params, P extends CharSequence, R extends CharSequence> extends AsyncTask<Params, P, R> {
    private static final String TAG = "BaseTestTask";
    protected static final String RESULT_FIN = "\nTask finished";

    protected Context context;
    protected TextView tv;
    protected String serialNum;

    public BaseTestTask(Context context, TextView tv, String serialNum) {
        this.context = context;
        this.serialNum = serialNum;
        this.tv = tv;
    }

    @Override
    protected void onProgressUpdate(P... values) {
        Log.i(TAG, values[0].toString());
        tv.append(values[0]);
    }

    @Override
    protected void onPostExecute(R result) {
        Log.i(TAG, result.toString());
        tv.append(result);
    }

    protected CharSequence genErrStr(String str) {
        Spannable errStr = new SpannableString(str);
        errStr.setSpan(new ForegroundColorSpan(Color.RED), 0, errStr.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return errStr;
    }

    protected void pLog(String msg) {
        Log.i(TAG, msg);
    }

    protected void pError(Throwable tr) {
        Log.e(TAG, "ERROR", tr);
    }
}
