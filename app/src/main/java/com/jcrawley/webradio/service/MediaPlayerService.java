package com.jcrawley.webradio.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import com.jcrawley.webradio.R;

import java.io.IOException;

import static com.jcrawley.webradio.service.MediaNotificationManager.NOTIFICATION_ID;

public class MediaPlayerService extends Service {

    public static final String ACTION_START_PLAYER = "com.jcrawley.webradio.startPlayer";
    public static final String ACTION_STOP_PLAYER = "com.jcrawley.webradio.stopPlayer";
    public static final String ACTION_CHANGE_STATION = "com.jcrawley.webradio.changeStation";
    public static final String ACTION_UPDATE_STATION_COUNT = "com.jcrawley.webradio.updateStationCount";
    public static final String ACTION_PLAY_CURRENT = "com.jcrawley.webradio.playCurrent";
    public static final String ACTION_SELECT_PREVIOUS_STATION = "com.jcrawley.webradio.selectPreviousStation";
    public static final String ACTION_SELECT_NEXT_STATION = "com.jcrawley.webradio.selectNextStation";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.jcrawley.webradio.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.jcrawley.webradio.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.jcrawley.webradio.notifyViewOfPlayInfo";
    public static final String ACTION_NOTIFY_VIEW_OF_ERROR = "com.jcrawley.webradio.notifyViewOfError";
    public static final String TAG_STATION_URL = "station_url";
    public static final String TAG_STATION_NAME = "station_name";
    public static final String TAG_STATION_COUNT = "station_count";
    private MediaPlayer mediaPlayer;
    public boolean hasEncounteredError;
    private boolean isPlaying;
    private String currentStationName  = "";
    private String currentUrl = "";
    private int stationCount;
    boolean wasInfoFound = false;
    private MediaMetadataRetriever metaRetriever;
    private MediaNotificationManager mediaNotificationManager;

    public MediaPlayerService() {
    }


    private final BroadcastReceiver serviceReceiverForStopPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopPlayer();
        }
    };


    private final BroadcastReceiver serviceReceiverForStartPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentUrl = intent.getStringExtra(TAG_STATION_URL);
            currentStationName = intent.getStringExtra(TAG_STATION_NAME);
            play();
        }
    };


    private final BroadcastReceiver serviceReceiverForPlayCurrent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            play();
        }
    };


    private final BroadcastReceiver serviceReceiverForUpdateStationCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int oldStationCount = stationCount;
            stationCount = intent.getIntExtra(TAG_STATION_COUNT, 0);
            if(stationCount != oldStationCount){
                mediaNotificationManager.updateNotification();
            }
        }
    };


    private final BroadcastReceiver serviceReceiverForChangeStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentStationName = intent.getStringExtra(TAG_STATION_NAME);
            currentUrl = intent.getStringExtra(TAG_STATION_URL);
            if(isPlaying){
                stopPlayer();
                play();
            }
            hasEncounteredError = false;
            mediaNotificationManager.updateNotification();
        }
    };



    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    boolean isPlaying(){
        return isPlaying;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(serviceReceiverForStopPlayer, new IntentFilter(ACTION_STOP_PLAYER));
        registerReceiver(serviceReceiverForStartPlayer, new IntentFilter(ACTION_START_PLAYER));
        registerReceiver(serviceReceiverForChangeStation, new IntentFilter(ACTION_CHANGE_STATION));
        registerReceiver(serviceReceiverForPlayCurrent, new IntentFilter(ACTION_PLAY_CURRENT));
        registerReceiver(serviceReceiverForUpdateStationCount, new IntentFilter(ACTION_UPDATE_STATION_COUNT));
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        moveToForeground();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiverForStartPlayer);
        unregisterReceiver(serviceReceiverForStopPlayer);
        unregisterReceiver(serviceReceiverForChangeStation);
        unregisterReceiver(serviceReceiverForUpdateStationCount);
        unregisterReceiver(serviceReceiverForPlayCurrent);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mediaNotificationManager.dismissNotification();
        this.stopSelf();
    }


    /*
        Service.START_STICKY - service is restarted if terminated, intent passed in has null value
        Service.START_NOT_STICKY - service is not restarted
        Service.START_REDELIVER_INTENT - service is restarted if terminated, original intent is passed in
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY;
    }


    private void moveToForeground(){
        mediaNotificationManager.init();
        Notification notification = mediaNotificationManager.createNotification(getCurrentStatus(), "");
        startForeground(NOTIFICATION_ID, notification);
    }


    String getCurrentStatus(){
        int resId = R.string.status_ready;
        if(hasEncounteredError){
            resId = R.string.status_error;
        }
        else if(isPlaying){
            resId = wasInfoFound ? R.string.status_playing : R.string.status_connecting;
        }
        return getApplicationContext().getString(resId);
    }


    String getCurrentStationName(){
        return currentStationName;
    }


    String getCurrentUrl(){
        return currentUrl;
    }


    int getStationCount(){
        return stationCount;
    }


    public void play() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = true;
        hasEncounteredError = false;
        if(currentUrl == null) {
            stopPlayer();
            hasEncounteredError = true;
            return;
        }
        createNewMediaPlayer();
        prepareAndPlay();
        mediaNotificationManager.updateNotification();
    }


    private void createNewMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes( new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
    }


    private void prepareAndPlay(){
        try {
            assert mediaPlayer != null;
            mediaPlayer.setDataSource(this, Uri.parse(currentUrl));
            mediaPlayer.prepareAsync();
            setupOnInfoListener();
            setupOnErrorListener();
            sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            stopPlayer();
            hasEncounteredError = true;
        }
    }


    private void setupOnInfoListener(){
        mediaPlayer.setOnInfoListener((mediaPlayer, i, i1) -> {
            if(!wasInfoFound){
                sendBroadcast(ACTION_NOTIFY_VIEW_OF_PLAYING);
                wasInfoFound = true;
                mediaNotificationManager.updateNotification();
            }
            return false;
        });
    }


    private void setupOnErrorListener(){
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            stopPlayer();
            hasEncounteredError = true;
            mediaNotificationManager.updateNotification();
            sendBroadcast(ACTION_NOTIFY_VIEW_OF_ERROR);
            return false;
        });
    }


    public void stopPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_STOP);
    }


    private void sendBroadcast(String action){
        sendBroadcast(new Intent(action));
    }

}
