package com.org.customvideoplayer.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.org.customvideoplayer.R;


public class RoundProgressDialog extends Dialog {

    ImageView mProgressImg;
    TextView mProgressMsgTv;
    AnimationDrawable mProgressAnimation;

    public RoundProgressDialog(Context context) {
        super(context, R.style.Round_Progressbar_Dialog);
        initView();
    }

    public RoundProgressDialog(Context context, int theme) {
        super(context, theme);
        initView();
    }

    public void initView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.round_progress_dialog, null);
        mProgressImg = (ImageView) root.findViewById(R.id.progress_img);
        mProgressMsgTv = (TextView) root.findViewById(R.id.progress_msg_tv);
        setTitle("");
        setContentView(root);
    }

    /**
     * 当窗口焦点改变时调用
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        // 获取ImageView上的动画背景
        mProgressAnimation = (AnimationDrawable) mProgressImg.getBackground();
        // 开始动画
        mProgressAnimation.start();
    }

    @Override
    public void dismiss() {
        //对话框消失前停止动画
        if (mProgressAnimation != null) {
            mProgressAnimation.stop();
        }
        super.dismiss();
    }

    /**
     * 给Dialog设置提示信息
     *
     * @param message
     */
    public void setMessage(CharSequence message) {
        if (!TextUtils.isEmpty(message)) {
            mProgressMsgTv.setVisibility(View.VISIBLE);
            mProgressMsgTv.setText(message);
            //mProgressMsgTv.invalidate();
        } else {
            mProgressMsgTv.setText("");
            mProgressMsgTv.setVisibility(View.GONE);
        }
    }

    /**
     * 弹出自定义ProgressDialog
     *
     * @param context        上下文
     * @param message        提示
     * @param cancelable     是否按返回键取消
     * @param cancelListener 按下返回键监听
     * @return
     */
    public static RoundProgressDialog show(Context context, CharSequence message, boolean cancelable, OnCancelListener cancelListener) {
        return show(context, message, cancelable, false, cancelListener);
    }

    /**
     * 弹出自定义ProgressDialog
     *
     * @param context        上下文
     * @param message        提示
     * @param cancelable     是否按返回键取消
     * @param canceledOnTouchOutside     是否点击外部对话框消失
     * @param cancelListener 按下返回键监听
     * @return
     */
    public static RoundProgressDialog show(Context context, CharSequence message, boolean cancelable,
                                           boolean canceledOnTouchOutside, OnCancelListener cancelListener) {
        RoundProgressDialog dialog = new RoundProgressDialog(context);
        dialog.setMessage(message);

        // 按返回键是否取消
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        // 监听返回键处理
        dialog.setOnCancelListener(cancelListener);
        // 设置居中
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        // 设置背景层透明度
        lp.dimAmount = 0.2f;
        dialog.getWindow().setAttributes(lp);
        // dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.show();
        return dialog;
    }
}
