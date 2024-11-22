package com.org.customvideoplayer.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.org.customvideoplayer.common.MimeConstant;

import java.io.File;

public class ImageUtils {

    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.startsWith("content://");
    }

    /**
     * Get Image mimeType
     *
     * @param path
     * @return
     */
    public static String getImageMimeType(String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                String fileName = file.getName();
                int last = fileName.lastIndexOf(".") + 1;
                String temp = fileName.substring(last);
                return "image/" + temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MimeConstant.MIME_TYPE_IMAGE;
        }
        return MimeConstant.MIME_TYPE_IMAGE;
    }

    /**
     * isGif
     *
     * @param mimeType
     * @return
     */
    public static boolean isGif(String mimeType) {
        return mimeType != null && (mimeType.equals("image/gif") || mimeType.equals("image/GIF"));
    }

    public static void loadImageByGlide(Context context, String url, ImageView imageView){
//        RequestOptions options = new RequestOptions()
////                .placeholder(R.drawable.loading)     //占位图
////                .error(R.drawable.error)	   //异常占位图
//                .override(200, 100)
//                .diskCacheStrategy(DiskCacheStrategy.NONE);//不进行磁盘缓存
        Glide.with(context)
                .load(url)
                //.apply(options)
                .into(imageView);
    }
}
