package com.jcrawley.webradio.service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
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

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;

import java.io.IOException;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class MediaPlayerService extends Service {

    private final Context context;
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    public static final String ACTION_START_PLAYER = "com.jcrawley.webradio.startPlayer";
    public static final String ACTION_STOP_PLAYER = "com.jcrawley.webradio.stopPlayer";
    public static final String TAG_TRACK_URL = "track_url";
    private MediaPlayer mediaPlayer;


    public MediaPlayerService() {
        context = MediaPlayerService.this;
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
            String url = intent.getStringExtra(TAG_TRACK_URL);
            play(url);
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
        //moveToForeground();
    }


    private void moveToForeground(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
            final String CHANNEL_ID = "com.jcrawley.webradio-notification";

            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("WebRadio")
                    .setContentText("Ready To Play")
                    .setContentIntent(pendingIntent).build();
            startForeground(7771, notification);
        }
    }


    private void log(String msg){
        System.out.println("^^^ MediaPlayerService: " + msg);
        System.out.flush();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiverForStartPlayer);
        unregisterReceiver(serviceReceiverForStopPlayer);
    }

    //send broadcast from activity to all receivers listening to the action "ACTION_STRING_ACTIVITY"
    private void sendBroadcast() {
        log("Entered sendBroadcast()");
        Intent intent = new Intent();
        intent.setAction(ACTION_STRING_ACTIVITY);
        sendBroadcast(intent);
    }


    /*
        Service.START_STICKY - service is restarted if terminated, intent passed in has null value
        Service.START_NOT_STICKY - service is not restarted
        Service.START_REDELIVER_INTENT - service is restarted if terminated, original intent is passed in

     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        log(" hello! onStartCommand() initiated!");
        return Service.START_REDELIVER_INTENT;
    }


    public void play(String currentURL) {
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
            //change with setDataSource(Context,Uri);
            mediaPlayer.setDataSource(this, Uri.parse(currentURL));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        issueNotification();
    }


    public void stopPlayer(){
        log("Entered stopPlayer()");
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }



    private void issueNotification(){

    }







    public class MyBinder extends Binder {
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

}
