package com.org.customvideoplayer.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.org.customvideoplayer.view.RoundProgressDialog;

public class BaseActivity extends Activity {

    protected static final String TAG = "BaseActivity";

    protected RoundProgressDialog mProgressDialog;

    protected boolean mProgressCanceled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    DialogInterface.OnCancelListener mProgressCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            Log.w(TAG, "mProgressCancelListener.onCancel ...");
            mProgressCanceled = true;
            //进度对话框被取消时,取消异步任务
            onProgressCancel();
        }
    };

    /**
     * 默认可取消，eg: 方便用户及时中断不可实现的网络请求
     * @param message
     */
    public void showProgressDialog(String message) {
        showProgressDialog(message, true, mProgressCancelListener);
    }

    public void showProgressDialog(int resId) {
        showProgressDialog(getString(resId));
    }

    public void showProgressDialog(int msgId, boolean cancelable, DialogInterface.OnCancelListener listener) {
        showProgressDialog(getString(msgId), cancelable, listener);
    }

    public void showProgressDialog(String msg, boolean cancelable, DialogInterface.OnCancelListener listener) {
        mProgressCanceled = false;
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.setMessage(msg);
                mProgressDialog.setOnCancelListener(listener);
                return;
            }
//			mProgressDialog.dismiss();
        }
        if(!isFinishing()){
            mProgressDialog = RoundProgressDialog.show(this, msg, cancelable, listener);
        }
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    protected void onProgressCancel(){

    }
}
