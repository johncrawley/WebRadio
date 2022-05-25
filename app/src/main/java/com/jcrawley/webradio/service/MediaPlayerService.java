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
import android.os.Binder;
import android.os.IBinder;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;

import java.io.IOException;

import androidx.core.app.NotificationCompat;

public class MediaPlayerService extends Service {

    public static final String ACTION_START_PLAYER = "com.jcrawley.webradio.startPlayer";
    public static final String ACTION_STOP_PLAYER = "com.jcrawley.webradio.stopPlayer";
    public static final String TAG_STATION_URL = "station_url";
    public static final String TAG_STATION_NAME = "station_name";
    private MediaPlayer mediaPlayer;
    private final int NOTIFICATION_ID = 1001;
    final String NOTIFICATION_CHANNEL_ID = "com.jcrawley.webradio-notification";
    private PendingIntent pendingIntent;


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
            String url = intent.getStringExtra(TAG_STATION_URL);
            String stationName = intent.getStringExtra(TAG_STATION_NAME);
            play(url, stationName);
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
        moveToForeground();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        dismissNotification();
        this.stopSelf();
    }


    private void moveToForeground(){
        setupNotificationChannel();
        setupNotificationClickForActivity();
        Notification notification = createNotification("");
        startForeground(NOTIFICATION_ID, notification);
    }


    private void updateNotification(String text) {
        Notification notification = createNotification(text);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private void setupNotificationClickForActivity(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }


    private void setupNotificationChannel(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "webradio-notification-channel-name", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setSound(null, null);
        notificationChannel.setShowBadge(false);
        notificationManager.createNotificationChannel(notificationChannel);
    }


    private Notification createNotification(String text){
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Webradio")
                .setContentText(text)
                .setSilent(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        return notification.build();
    }


    private void dismissNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiverForStartPlayer);
        unregisterReceiver(serviceReceiverForStopPlayer);
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


    public void play(String currentURL, String currentStationName) {
        if(mediaPlayer!= null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes( new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        try {
            mediaPlayer.setDataSource(this, Uri.parse(currentURL));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        updateNotification(currentStationName);
    }


    public void stopPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public class MyBinder extends Binder {
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

}
