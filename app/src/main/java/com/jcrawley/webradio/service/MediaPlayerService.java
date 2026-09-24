package com.jcrawley.webradio.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import com.jcrawley.webradio.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jcrawley.webradio.service.MediaNotificationManager.NOTIFICATION_CHANNEL_ID;
import static com.jcrawley.webradio.service.MediaNotificationManager.NOTIFICATION_ID;

public class MediaPlayerService extends Service {

    public static final String ACTION_START_PLAYER = "com.jcrawley.webradio.startPlayer";
   public static final String ACTION_STOP_PLAYER = "com.jcrawley.webradio.stopPlayer";
    public static final String ACTION_CHANGE_STATION = "com.jcrawley.webradio.changeStation";
    public static final String ACTION_REQUEST_STATUS = "com.jcrawley.webradio.requestStatus";
    public static final String ACTION_UPDATE_STATION_COUNT = "com.jcrawley.webradio.updateStationCount";
    public static final String ACTION_PLAY_CURRENT = "com.jcrawley.webradio.playCurrent";

    public static final String ACTION_SELECT_PREVIOUS_STATION = "com.jcrawley.webradio.selectPreviousStation";
    public static final String ACTION_SELECT_NEXT_STATION = "com.jcrawley.webradio.selectNextStation";
    public static final String ACTION_NOTIFY_VIEW_OF_STOP = "com.jcrawley.webradio.notifyViewOfStop";
    public static final String ACTION_NOTIFY_VIEW_OF_ERROR = "com.jcrawley.webradio.notifyViewOfError";
    public static final String ACTION_NOTIFY_VIEW_OF_CONNECTING = "com.jcrawley.webradio.notifyViewOfPlay";
    public static final String ACTION_NOTIFY_VIEW_OF_PLAYING = "com.jcrawley.webradio.notifyViewOfPlayInfo";


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
    private MediaNotificationManager mediaNotificationManager;
    private final ScheduledExecutorService executorService;
    private MetadataHandler metadataHandler;
    private final IBinder binder = new LocalBinder();
    private RadioView radioView;
    Map<BroadcastReceiver, String> broadcastReceiverMap;


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    public MediaPlayerService() {
        executorService = Executors.newScheduledThreadPool(3);
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
            log("serviceReceiverForStartPlayer : URL: " + currentUrl);

            play();
        }
    };


    private final BroadcastReceiver serviceReceiverForPlayCurrent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            play();
        }
    };


    private final BroadcastReceiver serviceReceiverForRequestStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String broadcast = isPlaying ? ACTION_NOTIFY_VIEW_OF_PLAYING : ACTION_NOTIFY_VIEW_OF_STOP;
            sendBroadcast(broadcast);
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
                stopPlayer(false);
                play();
            }
            hasEncounteredError = false;
            mediaNotificationManager.updateNotification();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private void log(String msg){
        System.out.println("^^^ MediaPlayerService: " + msg);
    }


    boolean isPlaying(){
        return isPlaying;
    }


    boolean isStopped(){
        return !isPlaying;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        metadataHandler = new MetadataHandler();
        setupBroadcastReceivers();
        mediaNotificationManager = new MediaNotificationManager(getApplicationContext(), this);
        moveToForeground();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayerAndLocks();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        unregisterBroadcastReceivers();
        mediaNotificationManager.dismissNotification();
        this.stopSelf();
    }

    private void setupBroadcastReceivers(){
        setupBroadcastReceiversMap();
        registerBroadcastReceivers();
    }


    private void setupBroadcastReceiversMap(){
        broadcastReceiverMap = new HashMap<>();
        broadcastReceiverMap.put(serviceReceiverForStopPlayer,          ACTION_STOP_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForStartPlayer,         ACTION_START_PLAYER);
        broadcastReceiverMap.put(serviceReceiverForChangeStation,       ACTION_CHANGE_STATION);
        broadcastReceiverMap.put(serviceReceiverForPlayCurrent,         ACTION_PLAY_CURRENT);
        broadcastReceiverMap.put(serviceReceiverForUpdateStationCount,  ACTION_UPDATE_STATION_COUNT);
        broadcastReceiverMap.put(serviceReceiverForRequestStatus,       ACTION_REQUEST_STATUS);
    }


    private void registerBroadcastReceivers(){
        for(var bcr : broadcastReceiverMap.keySet()){
            var intentFilter = new IntentFilter(broadcastReceiverMap.get(bcr));
            registerReceiver(bcr, intentFilter, RECEIVER_NOT_EXPORTED);
        }
    }


    private void unregisterBroadcastReceivers(){
        for(BroadcastReceiver bcr : broadcastReceiverMap.keySet()){
            unregisterReceiver(bcr);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY; // service is not restarted when terminated
    }


    private void releaseMediaPlayerAndLocks(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }


    private void moveToForeground(){
        mediaNotificationManager.init();
        var notification = mediaNotificationManager.createNotification(getCurrentStatus(), NOTIFICATION_CHANNEL_ID);
        startForeground(NOTIFICATION_ID, notification);
    }


    public void setView(RadioView radioView){
        this.radioView = radioView;
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


    boolean hasValidUrl(){
        return currentUrl != null && !currentUrl.isBlank();
    }


    int getStationCount(){
        return stationCount;
    }


    public void play(String currentUrl, String currentStationName){
        log("entered play: currentUrl: " + currentUrl);
        this.currentUrl = currentUrl;
        this.currentStationName = currentStationName;
        play();
    }


    public void play() {
        updateViewsForConnecting();
        stopRunningMediaPlayer();
        executorService.schedule(this::testUrlAndThenConnectWithMediaPlayer, 1, TimeUnit.MILLISECONDS);
    }


    private void stopRunningMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


    private void updateViewsForConnecting(){
        //sendBroadcast(ACTION_NOTIFY_VIEW_OF_CONNECTING);
        radioView.updateStatusViewOnConnecting();
        isPlaying = true;
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
    }


    private void testUrlAndThenConnectWithMediaPlayer(){
        if(UrlChecker.isCurrentUrlReachable(currentUrl)) {
            connectWithMediaPlayer();
            return;
        }
        handleConnectionError();
    }


    private void connectWithMediaPlayer(){
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


    private void createNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        setCpuWakeLock();
    }


    private void setCpuWakeLock(){
        if (checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }


    private void prepareAndPlay(){
        try {
            assert mediaPlayer != null;
            metadataHandler.initMetaDataRetriever(currentUrl);
            mediaPlayer.setDataSource(this, Uri.parse(currentUrl));
            mediaPlayer.prepareAsync();
            setupOnInfoListener();
            setupOnErrorListener();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IOException | RuntimeException e) {
            stopPlayer();
            hasEncounteredError = true;
        }
    }


    private void setupOnInfoListener(){
        mediaPlayer.setOnInfoListener((mediaPlayer, i, i1) -> {
            updateStatusFromConnectingToPlaying();
            executorService.schedule(()-> metadataHandler.updateMetadata(), 2000, TimeUnit.MILLISECONDS);
            return false;
        });
    }


    private void updateStatusFromConnectingToPlaying(){
        if(!wasInfoFound){
            //sendBroadcast(ACTION_NOTIFY_VIEW_OF_PLAYING);
            radioView.updateStatusViewOnPlaying();
            wasInfoFound = true;
            mediaNotificationManager.updateNotification();
        }
    }


    private void setupOnErrorListener(){
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            stopPlayer();
            handleConnectionError();
            return false;
        });
    }


    private void handleConnectionError(){
        hasEncounteredError = true;
        isPlaying = false;
        mediaNotificationManager.updateNotification();
        sendBroadcast(ACTION_NOTIFY_VIEW_OF_ERROR);
    }


    public void stopPlayer(){
        stopPlayer(true);
    }


    private void stopPlayer(boolean notifyView){
        releaseAndResetMediaPlayer();
        isPlaying = false;
        wasInfoFound = false;
        mediaNotificationManager.updateNotification();
        if(notifyView) {
            if(radioView!= null){
                radioView.updateStatusViewOnStop();
            }
        }
    }


    private void releaseAndResetMediaPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private void sendBroadcast(String action){
        sendBroadcast(new Intent(action));
    }

}
