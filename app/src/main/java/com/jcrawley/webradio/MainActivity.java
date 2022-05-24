package com.jcrawley.webradio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jcrawley.webradio.fragment.EditStationFragment;
import com.jcrawley.webradio.fragment.StationDetailFragment;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.notifier.PlayNotifier;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;
import com.jcrawley.webradio.repository.StationsRepositoryImpl;
import com.jcrawley.webradio.service.MediaPlayerService;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ListAdapterHelper listAdapterHelper;
    private StationsRepository stationsRepository;
    private String currentURL;
    private PlayNotifier playNotifier;
    Intent mediaPlayerServiceIntent;
    private boolean isServiceBound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startPauseButton = findViewById(R.id.playPauseButton);
        startPauseButton.setOnClickListener((View view)-> sendStartBroadcast());
        findViewById(R.id.stopButton).setOnClickListener((View view) -> sendStopBroadcast());
        ListView stationsList = findViewById(R.id.stationsList);
        stationsRepository = new StationsRepositoryImpl(this.getApplicationContext());
        listAdapterHelper = new ListAdapterHelper(this,
                stationsList,
                this::select,
                this::startEditStationFragment);
        refreshListFromDb();
        startMediaPlayerService();
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        log("Entered onStart()");
        bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            MediaPlayerService.MyBinder binder = (MediaPlayerService.MyBinder) service;

            isServiceBound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };


    private void bindService() {
        log("Entered bindService()");
        bindService(mediaPlayerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void unbindService(){
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }


    private void startMediaPlayerService(){
        mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        startService(mediaPlayerServiceIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_station){
            startAddStationFragment();
        }
        return true;
    }


    public void startAddStationFragment(){
        String tag = "add_station";
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        StationDetailFragment stationDetailFragment = StationDetailFragment.newInstance();
        stationDetailFragment.show(fragmentTransaction, tag);
    }


    public void startEditStationFragment(StationEntity listItem){
        String tag = "edit_station";
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        Bundle bundle = new Bundle();
        bundle.putLong(EditStationFragment.BUNDLE_STATION_ID, listItem.getId());
        bundle.putString(EditStationFragment.BUNDLE_STATION_NAME, listItem.getName());
        bundle.putString(EditStationFragment.BUNDLE_STATION_URL, listItem.getUrl());
        EditStationFragment editStationFragment = EditStationFragment.newInstance();
        editStationFragment.setArguments(bundle);
        editStationFragment.show(fragmentTransaction, tag);
    }


    private void sendStartBroadcast() {
        Intent intent = new Intent();
        intent.putExtra(MediaPlayerService.TAG_TRACK_URL, currentURL);
        intent.setAction(MediaPlayerService.ACTION_START_PLAYER);
        sendBroadcast(intent);
    }


    private void sendStopBroadcast(){
        log("Entered send stopBroadcast()");
        Intent intent = new Intent();
        intent.setAction(MediaPlayerService.ACTION_STOP_PLAYER);
        sendBroadcast(intent);
    }



    @SuppressWarnings("deprecation")
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }


    private void select(StationEntity listItem){
        currentURL = listItem.getUrl();
    }


    public void saveStation(StationEntity stationEntity){
        listAdapterHelper.addToList(stationEntity);
        stationsRepository.createStation(stationEntity);
    }


    public void updateStation(StationEntity stationEntity){

    }


    public void deleteStation(long stationId){
        listAdapterHelper.delete(stationId);
        stationsRepository.delete(stationId);
    }


    @Override
    protected void onPause() {
        super.onPause();
        dismissNotification();
    }


    private void dismissNotification(){
        if(playNotifier != null) {
            playNotifier.dismissNotification();
        }
    }


    public void refreshListFromDb(){
        List<StationEntity> items = stationsRepository.getAll();
        listAdapterHelper.setupList(items, android.R.layout.simple_list_item_1, findViewById(R.id.noResultsFoundText));
    }


    public void issueNotification(){
        if(playNotifier != null && playNotifier.isActive()){
            return;
        }
        runOnUiThread(() -> {
            playNotifier = new PlayNotifier(MainActivity.this);
            playNotifier.issueNotification();
        });
    }
}