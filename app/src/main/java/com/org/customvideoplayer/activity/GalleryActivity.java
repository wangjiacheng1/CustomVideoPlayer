package com.org.customvideoplayer.activity;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.PermissionChecker;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.org.customvideoplayer.R;
import com.org.customvideoplayer.adapter.GridGalleryAdapter;
import com.org.customvideoplayer.bean.LocalMedia;
import com.org.customvideoplayer.bean.LocalMediaFolder;
import com.org.customvideoplayer.helper.MediaLoader;
import com.org.customvideoplayer.interfaces.LoadMediaResultCallback;
import com.org.customvideoplayer.interfaces.OnPhotoClickListener;

import java.util.List;


public class GalleryActivity extends BaseActivity implements OnPhotoClickListener {

    protected static final String TAG = "GalleryActivity";

    private static final int GRID_SPAN_COUNT = 4;

    RecyclerView pictureRecycle;
    TextView mFolderTitleTv;
    GridGalleryAdapter mAdapter;
    MediaLoader mMediaLoader;
    Context mContext;

    List<LocalMediaFolder> mLocalMediaFolders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutView());
        mContext = getApplicationContext();
        mMediaLoader = new MediaLoader(mContext);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()){
            initData();
        }else {
            Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    }

    protected int getLayoutView(){
        return R.layout.activity_gallery;
    }

    protected void initView(){
        pictureRecycle = findViewById(R.id.recycle_picture);
        mFolderTitleTv =findViewById(R.id.tv_folder_title);

        pictureRecycle.setLayoutManager(new GridLayoutManager(mContext, GRID_SPAN_COUNT));

        mAdapter = new GridGalleryAdapter(mContext, this);
        pictureRecycle.setAdapter(mAdapter);
    }

    public boolean checkPermission(){
        if (PermissionChecker.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED){
            return false;
        }
        if (PermissionChecker.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

    protected void initData(){
        if (mAdapter == null){
            mAdapter = new GridGalleryAdapter(mContext, this);
            pictureRecycle.setAdapter(mAdapter);
        }
        if (mAdapter.isDataEmpty()){
            //异步查询数据
            showProgressDialog(R.string.loading_media_msg);
            loadAllMedia();
        }
    }

    protected void loadAllMedia(){
        mMediaLoader.loadAllMedia(new LoadMediaResultCallback<LocalMediaFolder>() {
            @Override
            public void onLoadComplete(List<LocalMediaFolder> data, int currentPage, boolean isHasMore) {
                Log.i(TAG, "loadAllMedia.onLoadComplete");
                handleLoadMediaComplete(data);
            }

            @Override
            public void onLoadError(int errCode, String errMsg, Throwable e) {
                Log.i(TAG, "initData.onLoadError, errCode = " + errCode + ", errMsg = " + errMsg + e == null ? "" : "Throwable : " + e);
            }
        });
    }

    protected void handleLoadMediaComplete(List<LocalMediaFolder> folderList){
        dismissProgressDialog();
        if (folderList == null || folderList.isEmpty()){
            //TODO 空数据处理
            return;
        }
        Log.i(TAG, "handleLoadMediaComplete : " + folderList.size());
        mLocalMediaFolders = folderList;
        LocalMediaFolder folder = folderList.get(0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFolderTitleTv.setText(folder.getName());
                List<LocalMedia> data = folder.getData();
                if (mAdapter != null){
                    mAdapter.bindData(data);
                }
            }
        });
    }

    /**
     * 添加图片点击事件
     */
    @Override
    public void onAddClick() {

    }

    /**
     * 图片点击事件
     * @param data
     * @param position
     */
    @Override
    public void onPhotoClick(Object data, int position) {
        LocalMedia photo = (LocalMedia) data;
        Log.d(TAG, "onPhotoClick, photo = " + photo.getRealPath());
    }
}