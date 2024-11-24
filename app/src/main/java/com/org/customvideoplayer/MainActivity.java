package com.org.customvideoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.org.customvideoplayer.activity.GalleryActivity;
import com.org.customvideoplayer.activity.VideoPlayerActivity;
import com.org.customvideoplayer.bean.LocalMedia;
import com.org.customvideoplayer.common.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_REQUEST_PERMISSION = 10001;
    private static final int REQUEST_CODE_PICK_MEDIA = 10010;
    Button pickBtn;
    Button playBtn;
    TextView videoPathText;

    Context mContext;
    LocalMedia curSelectedMedia = null;
    boolean allPermissionsGranted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        initView();
        checkPermission();
    }

    private void initView(){
        pickBtn = findViewById(R.id.btn_pick);
        videoPathText = findViewById(R.id.tv_video_text);
        playBtn = findViewById(R.id.btn_play_view);

        pickBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
    }

    private void checkPermission(){
        for (int i = 0; i < Constants.ALL_PERMISSION.length; i++){
            if (ContextCompat.checkSelfPermission(mContext, Constants.ALL_PERMISSION[i]) != PackageManager.PERMISSION_GRANTED){
                allPermissionsGranted = false;
            }
        }
        if (!allPermissionsGranted){
            requestPermissions(Constants.ALL_PERMISSION, REQUEST_CODE_REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSION) {
            allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted){
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == null){
            return;
        }
        int id = view.getId();
        if (R.id.btn_play_view == id){
            handlePlayBtnClick();
        }else if (R.id.btn_pick == id){
            handlePickBtnClick();
        }
    }

    private void handlePickBtnClick(){
        if (!allPermissionsGranted){
            checkPermission();
        }
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA);
    }

    private void handlePlayBtnClick(){
        if (!allPermissionsGranted){
            checkPermission();
        }
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_PICK_MEDIA:
                handlePickResult(resultCode, data);
                break;
            default:
                Log.w(TAG, "onActivityResult, invalid requestCode: " + requestCode);
        }
    }

    /**
     * 选择结束处理
     * @param resultCode
     * @param data
     */
    protected void handlePickResult(int resultCode, Intent data){
        if (resultCode != RESULT_OK){
            Log.d(TAG, "handlePickResult, resultCode not ok");
            return;
        }
        String resultJson = data.getStringExtra(Constants.PARAM_KEY_SELECTED_MEDIA);
        LocalMedia media = new Gson().fromJson(resultJson, LocalMedia.class);
        if (media == null){
            Log.w(TAG, "handlePickResult, pick media is invalid");
            return;
        }
        curSelectedMedia = media;
        videoPathText.setText(curSelectedMedia.getDisplayName());
    }
}