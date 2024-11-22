package com.org.customvideoplayer.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


import com.org.customvideoplayer.R;
import com.org.customvideoplayer.bean.LocalMedia;
import com.org.customvideoplayer.bean.LocalMediaFolder;
import com.org.customvideoplayer.bean.SelectionConfig;
import com.org.customvideoplayer.common.ErrorInfo;
import com.org.customvideoplayer.common.MimeConstant;
import com.org.customvideoplayer.common.PictureConfig;
import com.org.customvideoplayer.interfaces.LoadMediaResultCallback;
import com.org.customvideoplayer.utils.DeviceUtils;
import com.org.customvideoplayer.utils.ImageUtils;
import com.org.customvideoplayer.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MediaLoader {
    
    public static final String TAG = "MediaLoader";

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_BUCKET_ID = "bucket_id";
    private static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";
    private static final String NOT_GIF_UNKNOWN = "!='image/*'";
    private static final String NOT_GIF = "!='image/gif' AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF_UNKNOWN;
    private static final String GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id";

    /**
     * Image
     */
    private static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_NOT_GIF = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF;

    /**
     * Queries for images with the specified suffix
     */
    private static final String SELECTION_SPECIFIED_FORMAT = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + " AND " + MediaStore.MediaColumns.MIME_TYPE;

    private static final String[] PROJECTION_29 = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE};

    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID};

    private Context mContext;
    private SelectionConfig mConfig;
    
    public MediaLoader(Context context){
        this.mContext = context;
        this.mConfig = SelectionConfig.getDefault();
    }
    
    public MediaLoader(Context context, SelectionConfig config){
        this.mContext = context;
        this.mConfig = config;
    }
    
    public void setConfig(SelectionConfig config){
        this.mConfig = config;
    }
    
    public SelectionConfig getConfig(){
        return mConfig;
    }
    
    public void loadAllPageMedia(LoadMediaResultCallback callback){
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String projection[] = DeviceUtils.checkAndroid_Q() ? PROJECTION_29 : PROJECTION;
                String selection = getSelection();
                String[] args = getSelectionArgs();
                Log.d(TAG, "loadAllMedia, selection = " + selection + ", \n args = " + Arrays.toString(args));
                Cursor cursor = mContext.getContentResolver().query(QUERY_URI, projection, selection, args, ORDER_BY);
                if (cursor == null || cursor.getCount() == 0){
                    Log.w(TAG, "loadAllMedia, data is empty");
                    callbackDataEmptyError(callback);
                }
                int count = cursor.getCount();
                int totalCount = 0;
                List<LocalMediaFolder> mediaFolders = new ArrayList<>();
                try{
                    Map<Long, Integer> folderCountMap = new HashMap<>();
                    if (DeviceUtils.checkAndroid_Q()){
                        //Android 10
                        while (cursor.moveToNext()){
                            long bucketId = cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID));
                            Integer newCount = folderCountMap.get(bucketId);
                            if (newCount == null){
                                newCount = 1;
                            }else {
                                newCount++;
                            }
                            folderCountMap.put(bucketId, newCount);
                        }

                        if (cursor.moveToFirst()){
                            Set<Long> folderSet = new HashSet<>();
                            do {
                                long bucketId = cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID));
                                if (folderSet.contains(bucketId)){
                                    continue;
                                }
                                String displayName = cursor.getString(cursor.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                                int size = folderCountMap.get(bucketId);
                                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                                LocalMediaFolder folder = new LocalMediaFolder();
                                folder.setBucketId(bucketId);
                                folder.setName(displayName);
                                folder.setImageNum(size);
                                folder.setFirstImagePath(getRealPathAndroid_Q(id));
                                mediaFolders.add(folder);

                                folderSet.add(bucketId);
                                totalCount += size;
                            } while (cursor.moveToNext());
                        }
                    } else {
                        //Android 9
                        cursor.moveToFirst();
                        do {
                            long bucketId = cursor.getLong(cursor.getColumnIndex(COLUMN_BUCKET_ID));
                            String displayName = cursor.getString(cursor.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                            int size = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
                            String url = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                            LocalMediaFolder folder = new LocalMediaFolder();
                            folder.setBucketId(bucketId);
                            folder.setName(displayName);
                            folder.setImageNum(size);
                            folder.setFirstImagePath(url);
                            mediaFolders.add(folder);

                            totalCount += size;
                        }while (cursor.moveToNext());
                    }

                    //所有照片
                    LocalMediaFolder allFolder = new LocalMediaFolder();
                    allFolder.setImageNum(totalCount);
                    allFolder.setChecked(true);
                    allFolder.setBucketId(-1);
                    allFolder.setName(mContext.getString(R.string.folder_name_all_pic));
                    if (cursor.moveToFirst()) {
                        String firstUrl = DeviceUtils.checkAndroid_Q() ? getFirstUrlUpAndroidQ(cursor) : getFirstUrl(cursor);
                        allFolder.setFirstImagePath(firstUrl);
                    }

                    mediaFolders.add(0, allFolder);
                    //回调完成
                    callbackComplete(mediaFolders, 0, false, callback);
                }catch (Exception e){
                    Log.e(TAG, "loadAllMedia catch exception : " + e.getMessage(), e);
                    callbackException(e, callback);
                }
            }
        });
    }

    public void loadAllMedia(LoadMediaResultCallback callback){
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String[] projection = PROJECTION;
                String selection = getSelection();
                String[] args = getSelectionArgs();
                Cursor cursor = mContext.getContentResolver().query(QUERY_URI, projection, selection, args, ORDER_BY);
                if (cursor == null || cursor.getCount() == 0){
                    Log.w(TAG, "loadAllMedia, data is empty");
                    callbackDataEmptyError(callback);
                }

                List<LocalMediaFolder> imageFolders = new ArrayList<>();
                List<LocalMedia> latelyImages = new ArrayList<>();
                LocalMediaFolder allFolder = new LocalMediaFolder();
                try {
                    cursor.moveToFirst();
                    do {
                        long id = cursor.getLong
                                (cursor.getColumnIndexOrThrow(PROJECTION[0]));

                        String absolutePath = cursor.getString
                                (cursor.getColumnIndexOrThrow(PROJECTION[1]));

                        String path = DeviceUtils.checkAndroid_Q() ? getRealPathAndroid_Q(id) : absolutePath;

                        String mimeType = cursor.getString
                                (cursor.getColumnIndexOrThrow(PROJECTION[2]));

                        mimeType = TextUtils.isEmpty(mimeType) ? MimeConstant.MIME_TYPE_JPG : mimeType;
                        // Here, it is solved that some models obtain mimeType and return the format of image / *,
                        // which makes it impossible to distinguish the specific type, such as mi 8,9,10 and other models
                        if (mimeType.endsWith("image/*")) {
                            if (ImageUtils.isValidUrl(path)) {
                                mimeType = ImageUtils.getImageMimeType(absolutePath);
                            } else {
                                mimeType = ImageUtils.getImageMimeType(path);
                            }
                            if (!mConfig.isGif) {
                                boolean isGif = ImageUtils.isGif(mimeType);
                                if (isGif) {
                                    continue;
                                }
                            }
                        }
                        int width = cursor.getInt
                                (cursor.getColumnIndexOrThrow(PROJECTION[3]));

                        int height = cursor.getInt
                                (cursor.getColumnIndexOrThrow(PROJECTION[4]));

                        long duration = cursor.getLong
                                (cursor.getColumnIndexOrThrow(PROJECTION[5]));

                        long size = cursor.getLong
                                (cursor.getColumnIndexOrThrow(PROJECTION[6]));

                        String folderName = cursor.getString
                                (cursor.getColumnIndexOrThrow(PROJECTION[7]));

                        String fileName = cursor.getString
                                (cursor.getColumnIndexOrThrow(PROJECTION[8]));

                        long bucketId = cursor.getLong(cursor.getColumnIndexOrThrow(PROJECTION[9]));

                        LocalMedia image = new LocalMedia
                                (id, path, absolutePath, folderName, fileName, fileName + mimeType, mimeType, width, height, size, bucketId);
                        LocalMediaFolder folder = getImageFolder(path, folderName, imageFolders);
                        folder.setBucketId(image.getBucketId());
                        List<LocalMedia> images = folder.getData();
                        images.add(image);
                        folder.setImageNum(folder.getImageNum() + 1);
                        folder.setBucketId(image.getBucketId());
                        latelyImages.add(image);
                        int imageNum = allFolder.getImageNum();
                        allFolder.setImageNum(imageNum + 1);

                    } while (cursor.moveToNext());

                    //所有照片
                    if (latelyImages.size() > 0) {
                        allFolder.setChecked(true);
                        allFolder.setBucketId(-1);
                        allFolder.setName(mContext.getString(R.string.folder_name_all_pic));
                        allFolder.setFirstImagePath
                                (latelyImages.get(0).getPath());
                        allFolder.setData(latelyImages);

                        imageFolders.add(0, allFolder);
                    }
                    //回调完成
                    callbackComplete(imageFolders, 0, false, callback);
                }catch (Exception e){
                    Log.e(TAG, "loadAllMedia catch exception : " + e.getMessage(), e);
                    callbackException(e, callback);
                }
            }
        });
    }

    /**
     * 筛选selection
     * @return
     */
    private String getSelection() {
        switch (mConfig.mode) {
            case PictureConfig.TYPE_ALL:
                // Get all, not including audio
                return getSelectionForAllMediaCondition(getDurationCondition(0, 0), mConfig.isGif);
            case PictureConfig.TYPE_IMAGE:
                if (!TextUtils.isEmpty(mConfig.specifiedFormat)) {
                    // Gets the image of the specified type
                    return SELECTION_SPECIFIED_FORMAT + "='" + mConfig.specifiedFormat + "'";
                }
                return mConfig.isGif ? SELECTION : SELECTION_NOT_GIF;
            case PictureConfig.TYPE_VIDEO:
                // Access to video
                if (!TextUtils.isEmpty(mConfig.specifiedFormat)) {
                    // Gets the image of the specified type
                    return SELECTION_SPECIFIED_FORMAT + "='" + mConfig.specifiedFormat + "'";
                }
                return getSelectionForSingleMediaCondition();
        }
        return null;
    }

    /**
     * All mode conditions
     * 模式为all的selection
     * @param timeCondition
     * @param isGif
     * @return
     */
    private static String getSelectionForAllMediaCondition(String timeCondition, boolean isGif) {
        String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + (isGif ? "" : " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)
                + " OR "
                + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + timeCondition) + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0)";
        return selection;
    }

    /**
     * Query criteria (video)
     *
     * @return
     */
    private static String getSelectionForSingleMediaCondition() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0";
    }

    /**
     * Get video (maximum or minimum time)
     * 视频的时长限制
     * @param exMaxLimit
     * @param exMinLimit
     * @return
     */
    private String getDurationCondition(long exMaxLimit, long exMinLimit) {
        long maxS = mConfig.videoMaxSecond == 0 ? Long.MAX_VALUE : mConfig.videoMaxSecond;
        if (exMaxLimit != 0) {
            maxS = Math.min(maxS, exMaxLimit);
        }
        return String.format(Locale.CHINA, "%d <%s " + MediaStore.MediaColumns.DURATION + " and " + MediaStore.MediaColumns.DURATION + " <= %d",
                Math.max(exMinLimit, mConfig.videoMinSecond),
                Math.max(exMinLimit, mConfig.videoMinSecond) == 0 ? "" : "=",
                maxS);
    }

    private String[] getSelectionArgs() {
        switch (mConfig.mode) {
            case PictureConfig.TYPE_ALL:
                return SELECTION_ALL_ARGS;
            case PictureConfig.TYPE_IMAGE:
                // Get photo
                return getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            case PictureConfig.TYPE_VIDEO:
                // Get video
                return getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        }
        return null;
    }

    /**
     * mode为all的args
     */
    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    /**
     * Gets a file of the specified type
     * 指定类型的args
     * @param mediaType
     * @return
     */
    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    /**
     * Get cover uri
     * 获取第一张照片path
     * @param cursor
     * @return
     */
    private static String getFirstUrlUpAndroidQ(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
        return getRealPathAndroid_Q(id);
    }

    /**
     * Get cover url
     * 获取第一张照片path
     * @param cursor
     * @return
     */
    private static String getFirstUrl(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
    }

    /**
     * 跟进文件夹名称从列表中找到指定文件夹，没有则新建一个
     * @param path
     * @param folderName
     * @param folderList
     * @return
     */
    private LocalMediaFolder getImageFolder(String path, String folderName, List<LocalMediaFolder> folderList){
        for (LocalMediaFolder folder : folderList){
            String name = folder.getName();
            if (TextUtils.isEmpty(name)){
                continue;
            }
            if (name.equals(folderName)){
                return folder;
            }
        }

        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderName);
        newFolder.setFirstImagePath(path);
        folderList.add(newFolder);
        return newFolder;
    }

    /**
     * Android Q
     *
     * @param id
     * @return
     */
    private static String getRealPathAndroid_Q(long id) {
        return QUERY_URI.buildUpon().appendPath(String.valueOf(id)).build().toString();
    }

    private void callbackDataEmptyError(LoadMediaResultCallback callback){
        callbackError(ErrorInfo.ERROR_CODE_DATA_EMPTY, ErrorInfo.ERROR_MSG_DATA_EMPTY, null, callback);
    }
    private void callbackException(Throwable e, LoadMediaResultCallback callback){
        callbackError(ErrorInfo.ERROR_CODE_EXCEPTION, e.getMessage(), e, callback);
    }
    private void callbackError(int errCode, String errMsg, Throwable e, LoadMediaResultCallback callback){
        if (callback != null){
            callback.onLoadError(errCode, errMsg, e);
        }else {
            Log.w(TAG, "callbackError, callback is null, errCode = " + errCode + ", errMsg = " + errMsg);
        }
    }
    private void callbackComplete(List<LocalMediaFolder> data, int curPage, boolean isHasMore, LoadMediaResultCallback callback){
        if (callback != null){
            callback.onLoadComplete(data, curPage, isHasMore);
        }else {
            Log.w(TAG, "callbackComplete, callback is null");
        }
    }
    
}
