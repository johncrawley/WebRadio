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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcrawley.webradio.fragment.AboutAppFragment;
import com.jcrawley.webradio.fragment.EditStationFragment;
import com.jcrawley.webradio.fragment.AddStationFragment;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;
import com.jcrawley.webradio.repository.StationsRepositoryImpl;
import com.jcrawley.webradio.service.MediaPlayerService;

import java.util.List;

import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_ERROR;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_CONNECTING;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_PLAYING;
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
    private final String PREF_PREVIOUS_STATION_WEBSITE_LINK = "previous_station_website_link";
    private final String PREF_PREVIOUS_STATION_LIST_INDEX = "previous_station_list_index";
    private TextView stationNameTextView, statusTextView;
    private Button playButton, stopButton;
    private boolean isConnectionErrorShowing = false;
    private String stationWebsite;



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

    private final BroadcastReceiver serviceReceiverForNotifyConnecting = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatusViewOnConnecting();
        }
    };

    private final BroadcastReceiver serviceReceiverForNotifyPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStatusViewOnPlaying();
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
        unregisterReceiver(serviceReceiverForNotifyConnecting);
        unregisterReceiver(serviceReceiverForNotifyPlaying);
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
        else if(id == R.id.action_about){
            startAboutAppFragment();
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
        register(serviceReceiverForPreviousStation, ACTION_SELECT_PREVIOUS_STATION);
        register(serviceReceiverForNextStation, ACTION_SELECT_NEXT_STATION);
        register(serviceReceiverForNotifyStop, ACTION_NOTIFY_VIEW_OF_STOP);
        register(serviceReceiverForNotifyConnecting, ACTION_NOTIFY_VIEW_OF_CONNECTING);
        register(serviceReceiverForNotifyPlaying, ACTION_NOTIFY_VIEW_OF_PLAYING);
        register(serviceReceiverForNotifyError, ACTION_NOTIFY_VIEW_OF_ERROR);
    }


    private void register(BroadcastReceiver receiver, String action){
        registerReceiver(receiver, new IntentFilter(action));
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


    private void updateStatusViewOnConnecting(){
        stopButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.GONE);
        statusTextView.setText(R.string.status_connecting);
        isConnectionErrorShowing = false;
    }


    private void updateStatusViewOnPlaying(){
        stopButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.GONE);
        statusTextView.setText(R.string.status_playing);
        isConnectionErrorShowing = false;
    }


    private void onClick(){
        if(stationWebsite == null){
            return;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(stationWebsite));
        startActivity(browserIntent);
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
        stationWebsite = station.getLink();
        stationNameTextView.setText(currentStationName);
        saveCurrentStationPreference();
        sendChangeStationBroadcast();
        changeConnectionErrorStatus();
        setWebsiteLinkVisibility();
    }


    private void setWebsiteLinkVisibility(){


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
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        AddStationFragment stationDetailFragment = AddStationFragment.newInstance();
        stationDetailFragment.show(fragmentTransaction, tag);
    }


    private void startEditStationFragment(StationEntity station){
        String tag = "edit_station";
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
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


    private void startAboutAppFragment(){
        String tag = "about_app";
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        AboutAppFragment.newInstance().show(fragmentTransaction, tag);
    }


    private void removePreviousFragmentTransaction(String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
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
        editor.putString(PREF_PREVIOUS_STATION_WEBSITE_LINK, stationWebsite);
        editor.putInt(PREF_PREVIOUS_STATION_LIST_INDEX, listAdapterHelper.getSelectedIndex());
        editor.apply();
    }


    private void loadCurrentStationPreference(){
        if(isStationListEmpty()){
            return;
        }
        sharedPreferences = getSharedPreferences("webRadioEditor", MODE_PRIVATE);
        StationEntity station = buildStationFromPrefs();
        listAdapterHelper.setSelectedIndex(getPreviousStationIndex());
        updateStatusViewOnStop();
        selectStationAfterDelay(station);
    }


    private StationEntity buildStationFromPrefs(){
        return StationEntity.Builder.newInstance()
                .url(getPrefStr(PREF_PREVIOUS_STATION_URL))
                .name(getPrefStr(PREF_PREVIOUS_STATION_NAME))
                .link(getPrefStr(PREF_PREVIOUS_STATION_WEBSITE_LINK))
                .build();
    }


    private void selectStationAfterDelay(StationEntity station){
        new Handler(Looper.getMainLooper()).postDelayed(()->{
            sendUpdateStationCountBroadcast();
            select(station);
        } , 500);
    }


    private String getPrefStr(String key){
       return sharedPreferences.getString(key, "");
    }

    private int getPreviousStationIndex(){
        return sharedPreferences.getInt(PREF_PREVIOUS_STATION_LIST_INDEX, 0);
    }

}