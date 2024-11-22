package com.org.customvideoplayer.bean;


import com.org.customvideoplayer.common.PictureConfig;

public class SelectionConfig {

    //模式：all/只照片/只视频
    public int mode;
    //是否加载gif
    public boolean isGif = false;
    //视频最大时间
    public long videoMaxSecond = 0;
    //视频最小时间
    public long videoMinSecond = 0;
    //特殊格式
    public String specifiedFormat = "";

    public static SelectionConfig getDefault(){
        SelectionConfig defaultConfig = new SelectionConfig();
        defaultConfig.mode = PictureConfig.TYPE_VIDEO;
        defaultConfig.isGif = false;
        defaultConfig.videoMaxSecond = 0;
        defaultConfig.videoMinSecond = 0;
        defaultConfig.specifiedFormat = "";

        return defaultConfig;
    }
}
