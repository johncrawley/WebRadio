package com.jcrawley.webradio.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;

import java.io.IOException;

import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {

    public static final String ACTION_START_PLAYER = "com.jcrawley.webradio.startPlayer";
    public static final String ACTION_STOP_PLAYER = "com.jcrawley.webradio.stopPlayer";
    public static final String ACTION_CHANGE_STATION = "com.jcrawley.webradio.changeStation";
    public static final String ACTION_UPDATE_STATION_COUNT = "com.jcrawley.webradio.updateStationCount";
    public static final String ACTION_PLAY_CURRENT = "com.jcrawley.webradio.playCurrent";
    public static final String ACTION_SELECT_PREVIOUS_STATION = "com.jcrawley.webradio.selectPreviousStation";
    public static final String ACTION_SELECT_NEXT_STATION = "com.jcrawley.webradio.selectNextStation";
    public static final String TAG_STATION_URL = "station_url";
    public static final String TAG_STATION_NAME = "station_name";
    public static final String TAG_STATION_COUNT = "station_count";
    private MediaPlayer mediaPlayer;
    private final int NOTIFICATION_ID = 1001;
    final String NOTIFICATION_CHANNEL_ID = "com.jcrawley.webradio-notification";
    private PendingIntent pendingIntent;
    public boolean hasEncounteredError;
    private boolean isPlaying;
    private String currentStationName  = "";
    private String currentUrl = "";
    private int stationCount;


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
            System.out.println("Entered onReceive() for start player(), station: " + currentStationName + " url: " + currentUrl);
            play(currentUrl);
        }
    };


    private final BroadcastReceiver serviceReceiverForPlayCurrent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            play(currentUrl);
        }
    };


    private final BroadcastReceiver serviceReceiverForUpdateStationCount = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int oldStationCount = stationCount;
            stationCount = intent.getIntExtra(TAG_STATION_COUNT, 0);
            if(stationCount != oldStationCount){
                updateNotification();
            }
        }
    };


    private final BroadcastReceiver serviceReceiverForChangeStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentStationName = intent.getStringExtra(TAG_STATION_NAME);
            currentUrl = intent.getStringExtra(TAG_STATION_URL);
            if(isPlaying){
                return;
            }
            hasEncounteredError = false;
            updateNotification();
        }
    };



    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(serviceReceiverForStopPlayer, new IntentFilter(ACTION_STOP_PLAYER));
        registerReceiver(serviceReceiverForStartPlayer, new IntentFilter(ACTION_START_PLAYER));
        registerReceiver(serviceReceiverForChangeStation, new IntentFilter(ACTION_CHANGE_STATION));
        registerReceiver(serviceReceiverForPlayCurrent, new IntentFilter(ACTION_PLAY_CURRENT));
        registerReceiver(serviceReceiverForUpdateStationCount, new IntentFilter(ACTION_UPDATE_STATION_COUNT));
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
        dismissNotification();
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
        setupNotificationChannel();
        setupNotificationClickForActivity();
        Notification notification = createNotification(getCurrentStatus(), "");
        startForeground(NOTIFICATION_ID, notification);
    }


    private void updateNotification() {
        Notification notification = createNotification(getCurrentStatus(), currentStationName);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private String getCurrentStatus(){
        int resId = hasEncounteredError ? R.string.status_error : isPlaying ? R.string.status_playing : R.string.status_ready;
        return getApplicationContext().getString(resId);
    }


    private void setupNotificationClickForActivity(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }


    private void setupNotificationChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                "webradio-notification-channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }


    private Notification createNotification(String heading, String channelName){


        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(heading)
                .setContentText(channelName)
                .setSilent(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setNumber(-1)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        addPreviousButtonTo(notification);
        addPlayButtonTo(notification);
        addStopButtonTo(notification);
        addNextButtonTo(notification);
        return notification.build();
    }


    private void addPlayButtonTo(NotificationCompat.Builder notification){
        if(!isPlaying && !currentUrl.isEmpty()){
            notification.addAction(R.drawable.play_button_background, "play", createPendingIntentFor(ACTION_PLAY_CURRENT));
        }
    }


    private void addStopButtonTo(NotificationCompat.Builder notification){
        if(isPlaying){
            notification.addAction(R.drawable.ic_launcher_background, "stop", createPendingIntentFor(ACTION_STOP_PLAYER));
        }
    }


    private void addPreviousButtonTo(NotificationCompat.Builder notification){
        if(stationCount < 2) {
            return;
        }
        notification.addAction(R.drawable.ic_launcher_background, "Previous", createPendingIntentFor(ACTION_SELECT_PREVIOUS_STATION));
    }


    private void addNextButtonTo(NotificationCompat.Builder notification){
        if(stationCount < 2) {
            return;
        }
        notification.addAction(R.drawable.ic_launcher_background, "Next", createPendingIntentFor(ACTION_SELECT_NEXT_STATION));
    }


    private PendingIntent createPendingIntentFor(String action){
       return PendingIntent.getBroadcast(this,
               0,
               new Intent(action),
               PendingIntent.FLAG_IMMUTABLE);
    }


    private void dismissNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    public void play(String currentURL) {
        if(mediaPlayer!= null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = true;
        hasEncounteredError = false;
        if(currentURL == null) {
            stopPlayer();
            hasEncounteredError = true;
            return;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes( new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
       setupOnErrorListener();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(currentURL));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            stopPlayer();
            hasEncounteredError = true;
        }
        updateNotification();
    }


    private void setupOnErrorListener(){
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            stopPlayer();
            hasEncounteredError = true;
            updateNotification();
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
        updateNotification();
    }

}
