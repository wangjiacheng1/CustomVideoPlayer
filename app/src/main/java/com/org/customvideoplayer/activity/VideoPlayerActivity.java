package com.org.customvideoplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.org.customvideoplayer.R;
import com.org.customvideoplayer.service.PlayerService;

public class VideoPlayerActivity extends AppCompatActivity {

    PlayerView mPlayView;

    Context mContext;
    ExoPlayer player;

    PlayerService.MyPlayerBinder serviceBinder;
    PlayerService playerService;
    private boolean isBound = false;

    String path = "/storage/emulated/0/Download/QuarkDownloads/CloudDrive/c6d30dc45ad030fba581e89a33740ed3/321123.mov";

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBinder = (PlayerService.MyPlayerBinder)service;
            playerService = serviceBinder.getService();
            isBound = true;
            preparePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mContext = getApplicationContext();
        initView();
        bindPlayerService();
    }

    private void initView(){
        mPlayView = findViewById(R.id.player_view);
    }

    private void bindPlayerService(){
        Intent playerServerIntent = new Intent(this, PlayerService.class);
        bindService(playerServerIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void preparePlayer() {
        playerService.initPlayer(path);
        player = playerService.getPlayer();
        if (player == null){
            Toast.makeText(this, "player初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        mPlayView.setPlayer(player);
        playerService.playVideo();
    }

}