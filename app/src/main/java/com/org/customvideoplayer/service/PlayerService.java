package com.org.customvideoplayer.service;

import android.app.NotificationChannel;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.org.customvideoplayer.R;

import java.io.File;

public class PlayerService extends Service {

    private static final String TAG = "PlayerService";
    private static final String CHANNEL_ID = "FgVideoPlayerChannel";

    private ExoPlayer player;

    public class MyPlayerBinder extends Binder {
        public PlayerService getService(){
            return PlayerService.this;
        }
    }

    private MyPlayerBinder myPlayerBinder = new MyPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    public void initPlayer(String videoPath) {
        File file = checkVidoFile(videoPath);
        if (file == null){
            return;
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "exoPlayerDemo"));
        Uri uri = Uri.fromFile(file);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri));
//        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(MediaItem.fromUri(Uri.parse(videoPath)));
        player = new SimpleExoPlayer.Builder(this).build();
        player.setMediaSource(mediaSource);
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void playVideo() {
        player.prepare();
        player.setPlayWhenReady(true);
    }

    public void pauseVideo() {
        player.pause();
    }

    public void releasePlayer() {
        if (player != null){
            player.release();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        Log.d(TAG, "ACTION_RECONNECT action start Notification");
        try {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getResources().getString(R.string.app_name),
                    android.app.NotificationManager.IMPORTANCE_NONE);
            android.app.NotificationManager manager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            startForeground(1, builder.build());
        }catch (Exception e){
            Log.e(TAG, "createNotificationChannel catch Exception: " + e.getMessage(), e);
        }
    }

    private File checkVidoFile(String videoPath){
        if (TextUtils.isEmpty(videoPath)){
            Log.w(TAG, "playVideo, path is empty");
            return null;
        }
        File file = new File(videoPath);
        if (!file.exists()){
            Log.w(TAG, "playVideo, file is not exist");
            return null;
        }
//        if (!file.canRead() || !file.canWrite()){
//            Log.w(TAG, "playVideo, file can not operate");
//            return null;
//        }
        return file;
    }
}
