package com.org.customvideoplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.org.customvideoplayer.R;
import com.org.customvideoplayer.common.Constants;
import com.org.customvideoplayer.service.PlayerService;

import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayerActivity";
    
    // UI组件
    private PlayerView mPlayView;
    private ImageButton playPauseButton;
    private ImageButton speedButton;
    private ImageView backButton;
    private TextView videoTitle;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private SeekBar videoProgress;
    private Switch loopSwitch;
    private View topController;
    private LinearLayout bottomController;
    private ImageButton orientationButton;
    private boolean isFullScreen = false;
    
    // 控制相关变量
    private boolean isSpeedUp = false;
    private boolean isControllerShow = true;
    private Handler hideControllerHandler = new Handler();
    private static final long CONTROLLER_HIDE_TIMEOUT = 3000L; // 3秒后隐藏控制栏

    // Service相关
    private Context mContext;
    private ExoPlayer player;
    private PlayerService.MyPlayerBinder serviceBinder;
    private PlayerService playerService;
    private boolean isBound = false;

    String path = "/storage/emulated/0/Download/QuarkDownloads/CloudDrive/c6d30dc45ad030fba581e89a33740ed3/321123.mov";
    String mediaUrl = "";

    private boolean isLocked = false;
    private float initialX, initialY;
    private int screenWidth, screenHeight;
    private AudioManager audioManager;
    private int maxVolume;
    private int currentVolume;
    private float currentBrightness;

    private ImageButton lockButton;
    private View unlockButton;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        mContext = getApplicationContext();
        
        initView();
        handleIntent();
        bindPlayerService();
        initControls();
        setupGestureDetector();

        // 初始化音量和亮度控制
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        currentBrightness = getWindow().getAttributes().screenBrightness;

        // 获取屏幕宽高
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    private void initView() {
        mPlayView = findViewById(R.id.player_view);
        playPauseButton = findViewById(R.id.btn_play_pause);
        speedButton = findViewById(R.id.btn_speed);
        backButton = findViewById(R.id.btn_back);
        videoTitle = findViewById(R.id.video_title);
        currentTimeText = findViewById(R.id.text_current_time);
        totalTimeText = findViewById(R.id.text_total_time);
        videoProgress = findViewById(R.id.video_progress);
        loopSwitch = findViewById(R.id.switch_loop);
        topController = findViewById(R.id.top_controller);
        bottomController = findViewById(R.id.bottom_controller);
        orientationButton = findViewById(R.id.btn_orientation);
        lockButton = findViewById(R.id.btn_lock);
        unlockButton = findViewById(R.id.btn_unlock);
    }

    protected void handleIntent(){
        Intent intent = getIntent();
        String url = intent.getStringExtra(Constants.PARAM_KEY_START_PLAY_MEDIA_URL);
        if (!TextUtils.isEmpty(url)){
            mediaUrl = url;
        }
    }

    private void bindPlayerService(){
        Intent playerServerIntent = new Intent(this, PlayerService.class);
        bindService(playerServerIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void preparePlayer() {
        if (!TextUtils.isEmpty(mediaUrl)) {
            playerService.initPlayer(Uri.parse(mediaUrl));
            videoTitle.setText(getVideoNameFromUrl(mediaUrl));
        } else {
            playerService.initPlayerByPath(path);
            videoTitle.setText(getVideoNameFromPath(path));
        }
        
        player = playerService.getPlayer();
        if (player == null) {
            Toast.makeText(this, "播放器初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        mPlayView.setPlayer(player);
        playerService.playVideo();
        
        // 设置播放状态监听
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    updatePlayTime();
                    initVideoProgress();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                playPauseButton.setImageResource(isPlaying ? 
                    R.drawable.ic_pause_white : R.drawable.ic_play_white);
            }
        });
    }

    private void initControls() {
        // 返回按钮
        backButton.setOnClickListener(v -> finish());

        // 播放/暂停按钮
        playPauseButton.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    playerService.pauseVideo();
                    playPauseButton.setImageResource(R.drawable.ic_play_white);
                } else {
                    playerService.playVideo();
                    playPauseButton.setImageResource(R.drawable.ic_pause_white);
                }
                showController();
            }
        });

        // 长按加速按钮
        speedButton.setOnTouchListener((v, event) -> {
            if (player == null) return false;
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isSpeedUp = true;
                    player.setPlaybackSpeed(2.0f);
                    showController();
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:    
                    isSpeedUp = false;
                    player.setPlaybackSpeed(1.0f);
                    showController();
                    return true;
            }
            return false;
        });

        // 循环播放开关
        loopSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (playerService != null) {
                playerService.setLooping(isChecked);
            }
            showController();
        });

        // 进度条
        videoProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    player.seekTo(progress);
                    updatePlayTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                removeControllerHideCallbacks();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                hideControllerDelay();
            }
        });

        // 横竖屏切换���钮
        orientationButton.setOnClickListener(v -> {
            toggleOrientation();
            showController();
        });

        // 锁屏按钮
        lockButton.setOnClickListener(v -> {
            isLocked = true;
            lockButton.setVisibility(View.GONE);
            unlockButton.setVisibility(View.VISIBLE);
            hideController();
        });

        // 解锁按钮
        unlockButton.setOnClickListener(v -> {
            isLocked = false;
            lockButton.setVisibility(View.VISIBLE);
            unlockButton.setVisibility(View.GONE);
            showController();
        });
    }

    private void setupGestureDetector() {
        mPlayView.setOnTouchListener((v, event) -> {
            if (isLocked) return true; // 锁屏状态下不响应其他手势

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getX();
                    initialY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - initialX;
                    float deltaY = event.getY() - initialY;
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        // 水平滑动，调节播放进度
                        adjustPlaybackPosition(deltaX);
                    } else {
                        if (initialX < screenWidth / 2) {
                            // 左半屏，调节亮度
                            adjustBrightness(deltaY);
                        } else {
                            // 右半屏，调节音量
                            adjustVolume(deltaY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
    }

    private void adjustPlaybackPosition(float deltaX) {
        if (player == null) return;
        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        long newPosition = position + (long) (deltaX / screenWidth * duration);
        player.seekTo(Math.max(0, Math.min(newPosition, duration)));
        updatePlayTime();
    }

    private void adjustVolume(float deltaY) {
        int volumeChange = (int) (deltaY / screenHeight * maxVolume);
        currentVolume = Math.max(0, Math.min(currentVolume - volumeChange, maxVolume));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
    }

    private void adjustBrightness(float deltaY) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        float brightnessChange = deltaY / screenHeight;
        currentBrightness = Math.max(0.01f, Math.min(currentBrightness - brightnessChange, 1.0f));
        layoutParams.screenBrightness = currentBrightness;
        getWindow().setAttributes(layoutParams);
    }

    private void toggleController() {
        if (isControllerShow) {
            hideController();
        } else {
            showController();
        }
    }

    private void showController() {
        if (!isControllerShow) {
            topController.setVisibility(View.VISIBLE);
            bottomController.setVisibility(View.VISIBLE);
            isControllerShow = true;
        }
        hideControllerDelay();
    }

    private void hideController() {
        if (isControllerShow) {
            topController.setVisibility(View.GONE);
            bottomController.setVisibility(View.GONE);
            isControllerShow = false;
        }
    }

    private void hideControllerDelay() {
        removeControllerHideCallbacks();
        hideControllerHandler.postDelayed(this::hideController, CONTROLLER_HIDE_TIMEOUT);
    }

    private void removeControllerHideCallbacks() {
        hideControllerHandler.removeCallbacks(null);
    }

    private void updatePlayTime() {
        if (player == null) return;
        
        long duration = player.getDuration();
        long position = player.getCurrentPosition();
        
        currentTimeText.setText(formatTime(position));
        totalTimeText.setText(formatTime(duration));
        
        if (duration > 0) {
            videoProgress.setProgress((int) position);
        }
    }

    private void initVideoProgress() {
        if (player == null) return;
        
        long duration = player.getDuration();
        videoProgress.setMax((int) duration);
        
        // 定时更新进度
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    updatePlayTime();
                }
                new Handler().postDelayed(this, 1000);
            }
        }, 1000);
    }

    private String formatTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String getVideoNameFromUrl(String url) {
        try {
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "未知视频";
        }
    }

    private String getVideoNameFromPath(String path) {
        try {
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "未知视频";
        }
    }

    private void toggleOrientation() {
        if (isFullScreen) {
            // 切换到竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            orientationButton.setImageResource(R.drawable.ic_fullscreen);
            isFullScreen = false;
        } else {
            // ���换到横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            orientationButton.setImageResource(R.drawable.ic_fullscreen_exit);
            isFullScreen = true;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏布局调整
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            
            ViewGroup.LayoutParams params = mPlayView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mPlayView.setLayoutParams(params);
            
            // 调整控制栏布局
            topController.setVisibility(View.GONE);
            bottomController.setOrientation(LinearLayout.HORIZONTAL);
            bottomController.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            // 竖屏布局调整
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            
            ViewGroup.LayoutParams params = mPlayView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mPlayView.setLayoutParams(params);
            
            // 恢复控制栏布局
            topController.setVisibility(View.VISIBLE);
            bottomController.setOrientation(LinearLayout.VERTICAL);
            bottomController.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            // 如果当前是横屏，则先切换回竖屏
            toggleOrientation();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeControllerHideCallbacks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideControllerHandler.removeCallbacks(null);
        if (isBound) {
            unbindService(connection);
        }
    }
}