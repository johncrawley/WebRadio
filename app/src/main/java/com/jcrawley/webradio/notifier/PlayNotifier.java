package com.jcrawley.webradio.notifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.service.notification.StatusBarNotification;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;

public class PlayNotifier {

    private final Context context;
   // private final MainViewModel viewModel;
    private Notification.Builder notificationBuilder;
    private NotificationManager notificationManager;
    public static final String CHANNEL_ID = "com.jcrawley.web-radio.notifications";
    public static final String CHANNEL_NAME = "Play Notification";
    private final int NOTIFICATION_ID = 1010019;


    public PlayNotifier(Context context){
        this.context = context;
      //  this.viewModel = viewModel;
        createChannels();
        createNotification();
    }


    public boolean isActive(){
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for(StatusBarNotification notification : activeNotifications){
            if(notification.getId() == NOTIFICATION_ID){
                return true;
            }
        }
        return false;
    }


    public void createNotification(){
        String title = getStr(R.string.notification_title);
       // String enteredMessage = viewModel.reminderMessage.trim();
        String message = "Playing Radio Station: ";
        notificationBuilder = getAndroidChannelNotification(title, message);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setContentIntent(contentIntent);
    }


    private String getStr(int resId){
        return context.getResources().getString(resId);
    }


    public void createChannels() {
        NotificationChannel androidChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        androidChannel.enableLights(false);
        androidChannel.enableVibration(false);
        androidChannel.setLightColor(Color.GREEN);
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(androidChannel);
    }


    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        return new Notification.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(false);
    }


    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }


    public void issueNotification(){
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }


    public void dismissNotification(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

}
