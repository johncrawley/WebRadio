package com.jcrawley.webradio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcrawley.webradio.fragment.EditStationFragment;
import com.jcrawley.webradio.fragment.AddStationFragment;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;
import com.jcrawley.webradio.repository.StationsRepositoryImpl;
import com.jcrawley.webradio.service.MediaPlayerService;

import java.util.List;

import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_ERROR;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_PLAY;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_STOP;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_SELECT_NEXT_STATION;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_SELECT_PREVIOUS_STATION;


public class MainActivity extends AppCompatActivity {
    private ListAdapterHelper listAdapterHelper;
    private StationsRepository stationsRepository;
    private String currentURL;
    private Intent mediaPlayerServiceIntent;
    private boolean isServiceBound;
    private String currentStationName;
    private SharedPreferences sharedPreferences;
    private final String PREF_PREVIOUS_STATION_NAME = "previous_station_name";
    private final String PREF_PREVIOUS_STATION_URL = "previous_station_url";
    private final String PREF_PREVIOUS_STATION_LIST_INDEX = "previous_station_list_index";
    private TextView stationNameTextView, statusTextView;
    private Button playButton, stopButton;
    private boolean isConnectionErrorShowing = false;


    private final BroadcastReceiver serviceReceiverForPreviousStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            select(listAdapterHelper.getPreviousStation());
        }
    };

    private final BroadcastReceiver serviceReceiverForNextStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            select(listAdapterHelper.getNextStation());
        }
    };


    private final BroadcastReceiver serviceReceiverForNotifyStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatusViewOnStop();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatusViewOnPlay();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyError = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatusViewOnError();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupRepository();
        setupStationList();
        setupViews();
        refreshListFromDb();
        startMediaPlayerService();
        loadCurrentStationPreference();
        setupBroadcastReceivers();
    }


    @Override
    protected void onStart(){
        super.onStart();
        bindService();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }


    @Override
    protected  void onDestroy(){
        super.onDestroy();
        unregisterReceiver(serviceReceiverForPreviousStation);
        unregisterReceiver(serviceReceiverForNextStation);
        unregisterReceiver(serviceReceiverForNotifyStop);
        unregisterReceiver(serviceReceiverForNotifyPlay);
        unregisterReceiver(serviceReceiverForNotifyError);
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


    public void saveStation(StationEntity stationEntity){
        listAdapterHelper.addToList(stationEntity);
        stationsRepository.createStation(stationEntity);
        sendUpdateStationCountBroadcast();
    }


    public void updateStation(StationEntity stationEntity){
        stationsRepository.update(stationEntity);
        refreshListFromDb();
    }


    public void deleteStation(long stationId){
        listAdapterHelper.delete(stationId);
        stationsRepository.delete(stationId);
        sendUpdateStationCountBroadcast();
    }


    private void setupBroadcastReceivers(){
        registerReceiver(serviceReceiverForPreviousStation, new IntentFilter(ACTION_SELECT_PREVIOUS_STATION));
        registerReceiver(serviceReceiverForNextStation, new IntentFilter(ACTION_SELECT_NEXT_STATION));
        registerReceiver(serviceReceiverForNotifyStop, new IntentFilter(ACTION_NOTIFY_VIEW_OF_STOP));
        registerReceiver(serviceReceiverForNotifyPlay, new IntentFilter(ACTION_NOTIFY_VIEW_OF_PLAY));
        registerReceiver(serviceReceiverForNotifyError, new IntentFilter(ACTION_NOTIFY_VIEW_OF_ERROR));
    }


    private void setupViews(){
        findViewById(R.id.playButton).setOnClickListener((View view)-> sendStartBroadcast());
        findViewById(R.id.stopButton).setOnClickListener((View view) -> sendStopBroadcast());
        setupNameTextView();
        statusTextView = findViewById(R.id.playStatusTextView);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
    }


    private void setupNameTextView(){
        stationNameTextView = findViewById(R.id.stationNameTextView);
        if(isStationListEmpty()){
            stationNameTextView.setText("");
        }
    }


    private void updateStatusViewOnStop(){
        hideStopButtonShowPlayButton();
        statusTextView.setText(R.string.status_ready);
    }


    private void updateStatusViewOnError(){
        hideStopButtonShowPlayButton();
        isConnectionErrorShowing = true;
        statusTextView.setText(R.string.status_error);
    }


    private void updateStatusViewOnPlay(){
        stopButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.GONE);
        statusTextView.setText(R.string.status_playing);
        isConnectionErrorShowing = false;
    }


    private void hideStopButtonShowPlayButton(){
        playButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
    }


    private void setupRepository(){
        stationsRepository = new StationsRepositoryImpl(this.getApplicationContext());
    }


    private void setupStationList(){
        listAdapterHelper = new ListAdapterHelper(this,
                findViewById(R.id.stationsList),
                this::select,
                this::startEditStationFragment);
    }


    private void select(StationEntity station){
        currentURL = station.getUrl();
        currentStationName = station.getName();
        stationNameTextView.setText(currentStationName);
        saveCurrentStationPreference();
        sendChangeStationBroadcast();
        changeConnectionErrorStatus();
    }


    private void changeConnectionErrorStatus(){
        if(isConnectionErrorShowing){
            isConnectionErrorShowing = false;
            statusTextView.setText(getString(R.string.status_ready));
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName className, IBinder service) { isServiceBound = true; }
        @Override public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };


    private void bindService() {
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
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
    }


    private void startAddStationFragment(){
        String tag = "add_station";
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        AddStationFragment stationDetailFragment = AddStationFragment.newInstance();
        stationDetailFragment.show(fragmentTransaction, tag);
    }


    private void startEditStationFragment(StationEntity station){
        String tag = "edit_station";
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        Bundle bundle = new Bundle();
        bundle.putLong(EditStationFragment.BUNDLE_STATION_ID, station.getId());
        bundle.putString(EditStationFragment.BUNDLE_STATION_NAME, station.getName());
        bundle.putString(EditStationFragment.BUNDLE_STATION_URL, station.getUrl());
        bundle.putString(EditStationFragment.BUNDLE_STATION_DESCRIPTION, station.getDescription());
        bundle.putString(EditStationFragment.BUNDLE_STATION_LINK, station.getLink());
        EditStationFragment editStationFragment = EditStationFragment.newInstance();
        editStationFragment.setArguments(bundle);
        editStationFragment.show(fragmentTransaction, tag);
    }


    private void sendStartBroadcast() {
        Intent intent = new Intent(MediaPlayerService.ACTION_START_PLAYER);
        intent.putExtra(MediaPlayerService.TAG_STATION_URL, currentURL);
        intent.putExtra(MediaPlayerService.TAG_STATION_NAME, currentStationName);
        sendBroadcast(intent);
    }


    private void sendStopBroadcast(){
        Intent intent = new Intent();
        intent.setAction(MediaPlayerService.ACTION_STOP_PLAYER);
        sendBroadcast(intent);
    }


    private void sendChangeStationBroadcast(){
        Intent intent = new Intent(MediaPlayerService.ACTION_CHANGE_STATION);
        intent.putExtra(MediaPlayerService.TAG_STATION_NAME, currentStationName);
        intent.putExtra(MediaPlayerService.TAG_STATION_URL, currentURL);
        sendBroadcast(intent);
    }

    private boolean isStationListEmpty(){
        return listAdapterHelper.getCount() == 0;
    }


    private void sendUpdateStationCountBroadcast(){
        Intent intent = new Intent();
        intent.setAction(MediaPlayerService.ACTION_UPDATE_STATION_COUNT);
        intent.putExtra(MediaPlayerService.TAG_STATION_COUNT, listAdapterHelper.getCount());
        sendBroadcast(intent);
    }


    private void refreshListFromDb(){
        List<StationEntity> items = stationsRepository.getAll();
        listAdapterHelper.setupList(items, android.R.layout.simple_list_item_1, findViewById(R.id.noResultsFoundText));
    }


    private void saveCurrentStationPreference(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_PREVIOUS_STATION_NAME, currentStationName);
        editor.putString(PREF_PREVIOUS_STATION_URL, currentURL);
        editor.putInt(PREF_PREVIOUS_STATION_LIST_INDEX, listAdapterHelper.getSelectedIndex());
        editor.apply();
    }


    private void loadCurrentStationPreference(){
        if(isStationListEmpty()){
            return;
        }
        sharedPreferences = getSharedPreferences("webRadioEditor", MODE_PRIVATE);
        String name = sharedPreferences.getString(PREF_PREVIOUS_STATION_NAME, "");
        String url = sharedPreferences.getString(PREF_PREVIOUS_STATION_URL, "");
        StationEntity station = new StationEntity(name, url);
        int selectedIndex = sharedPreferences.getInt(PREF_PREVIOUS_STATION_LIST_INDEX, 0);
        listAdapterHelper.setSelectedIndex(selectedIndex);
        updateStatusViewOnStop();

        new Handler(Looper.getMainLooper()).postDelayed(()->{
            sendUpdateStationCountBroadcast();
            select(station);
        } , 500);
    }

}