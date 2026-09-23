package com.jcrawley.webradio;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import com.jcrawley.webradio.fragment.FaqDialogFragment;
import com.jcrawley.webradio.fragment.StationLibraryFragment;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.repository.InitialStationsLoader;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;
import com.jcrawley.webradio.repository.StationsRepositoryImpl;
import com.jcrawley.webradio.service.MediaPlayerService;
import com.jcrawley.webradio.service.RadioView;

import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_ERROR;
//import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_CONNECTING;
//import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_PLAYING;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_NOTIFY_VIEW_OF_STOP;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_SELECT_NEXT_STATION;
import static com.jcrawley.webradio.service.MediaPlayerService.ACTION_SELECT_PREVIOUS_STATION;

import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends AppCompatActivity implements RadioView {
    private ListAdapterHelper listAdapterHelper;
    private StationsRepository stationsRepository;
    private String currentURL;
    private boolean isServiceBound;
    private String currentStationName;
    private SharedPreferences sharedPreferences;
    private final String PREF_PREVIOUS_STATION_NAME = "previous_station_name";
    private final String PREF_PREVIOUS_STATION_URL = "previous_station_url";
    private final String PREF_PREVIOUS_STATION_WEBSITE_LINK = "previous_station_website_link";
    private final String PREF_PREVIOUS_STATION_LIST_INDEX = "previous_station_list_index";
    private TextView stationNameTextView, statusTextView, websiteLinkTextView;
    private Button playButton, stopButton;
    private boolean isConnectionErrorShowing = false;
    private String stationWebsite;
    private StationLibraryFragment stationLibraryFragment;
    private StationEntity stationEntity;
    private AlertDialog.Builder removeFromFavouritesConfirmationDialog;
    private MediaPlayerService mediaPlayerService;
    private final AtomicBoolean isServiceConnected = new AtomicBoolean(false);


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            var binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.setView(MainActivity.this);

            isServiceConnected.set(true);
        }

        @Override public void onServiceDisconnected(ComponentName arg0) {
            isServiceConnected.set(false);
        }
    };



    @Override
    public void selectPreviousStation(){
        select(listAdapterHelper.getPreviousStation());
    }


    @Override
    public void  selectNextStation(){
        select(listAdapterHelper.getNextStation());
    }


    @Override
    public void updateStatusViewOnStop(){
        hideStopShowPlay();
        statusTextView.setText(R.string.status_ready);
    }


    @Override
    public void  updateStatusViewOnConnecting(){
        hidePlayShowStop();
        statusTextView.setText(R.string.status_connecting);
        isConnectionErrorShowing = false;
    }


    @Override
    public void updateStatusViewOnPlaying(){
        hidePlayShowStop();
        statusTextView.setText(R.string.status_playing);
        isConnectionErrorShowing = false;
    }


    @Override
    public void updateStatusViewOnError(){
        hideStopShowPlay();
        isConnectionErrorShowing = true;
        statusTextView.setText(R.string.status_error);
    }



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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setupRepository();
        setupStationList();
        setupViews();
        refreshListFromDb();
        startMediaPlayerService();
        loadCurrentStationPreference();
        setupBroadcastReceivers();
        setupWebsiteLink();
        setInitialStatus();
        loadInitialStations();
        setupDeleteConfirmationDialog();
    }


    private void loadInitialStations(){
        new InitialStationsLoader(MainActivity.this, getSharedPreferences()).load();
    }


    @Override
    protected void onStart(){
        super.onStart();
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
            startLibraryFragment();
        }
        else if(id == R.id.action_about){
            startAboutAppFragment();
        }
        return true;
    }


    public void saveStation(StationEntity stationEntity){
        listAdapterHelper.addToList(stationEntity);
        stationsRepository.createStation(stationEntity);
        updateStatusAfterListChange();
        selectStationIfItsTheOnlyOne();
    }


    private void selectStationIfItsTheOnlyOne(){
        if(listAdapterHelper.getCount() == 1){
            listAdapterHelper.clickFirstItem();
        }
    }


    public void updateStation(StationEntity stationEntity){
        stationsRepository.update(stationEntity);
        refreshListFromDb();
    }


    public void deleteStation(long stationId){
        listAdapterHelper.delete(stationId);
        stationsRepository.delete(stationId);
    }


    private void setupBroadcastReceivers(){
        register(serviceReceiverForPreviousStation, ACTION_SELECT_PREVIOUS_STATION);
        register(serviceReceiverForNextStation, ACTION_SELECT_NEXT_STATION);
        register(serviceReceiverForNotifyStop, ACTION_NOTIFY_VIEW_OF_STOP);
        //register(serviceReceiverForNotifyConnecting, ACTION_NOTIFY_VIEW_OF_CONNECTING);
        //register(serviceReceiverForNotifyPlaying, ACTION_NOTIFY_VIEW_OF_PLAYING);
        register(serviceReceiverForNotifyError, ACTION_NOTIFY_VIEW_OF_ERROR);
    }


    private void register(BroadcastReceiver receiver, String action){
        registerReceiver(receiver, new IntentFilter(action), RECEIVER_NOT_EXPORTED);
    }


    private void setupViews(){
        setupNameTextView();
        setupButtons();
        statusTextView = findViewById(R.id.playStatusTextView);
        websiteLinkTextView = findViewById(R.id.websiteLinkTextView);
    }


    private void setupButtons(){
        playButton = (Button)setupButton(R.id.playButton, this::startPlayer);
        stopButton = (Button)setupButton(R.id.stopButton, this::sendStopBroadcast);
        setupButton(R.id.addStationBigButton, this::startLibraryFragment);
    }


    private View setupButton(int viewId, Runnable runnable){
        View view = findViewById(viewId);
        view.setOnClickListener((View v)-> runnable.run());
        return view;
    }


    private void setupNameTextView(){
        stationNameTextView = findViewById(R.id.stationNameTextView);
        if(isStationListEmpty()){
            stationNameTextView.setText("");
        }
    }


    private void setupWebsiteLink(){
        View websiteLinkTextView = findViewById(R.id.websiteLinkTextView);
        websiteLinkTextView.setOnClickListener((View v)->{
            if(stationWebsite == null || stationWebsite.trim().isEmpty()){
                return;
            }
            var browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(stationWebsite));
            startActivity(browserIntent);
        });
    }


    private void hideStopShowPlay(){
        log("entered hideStopShowPlay");
        stopButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
    }


    private void log(String msg){
        System.out.println("^^^ MainActivity: " + msg);
    }


    private void hidePlayShowStop(){
        log("entered hidePlayShowStop");
        playButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);
    }


    private void setupRepository(){
        stationsRepository = new StationsRepositoryImpl(this.getApplicationContext());
    }


    public StationsRepository getStationRepository(){
        return stationsRepository;
    }


    private void setupStationList(){
        listAdapterHelper = new ListAdapterHelper(this,
                findViewById(R.id.stationsList),
                this::select,
                this::showConfirmationDialog);
    }


    private void showConfirmationDialog(StationEntity station){
        stationEntity = station;
        removeFromFavouritesConfirmationDialog.show();
    }


    private void select(StationEntity station){
        setReadyStatus();
        currentURL = station.getUrl();
        currentStationName = station.getName();
        stationWebsite = station.getLink();
        stationNameTextView.setText(currentStationName);
        websiteLinkTextView.setText(stationWebsite);
        saveCurrentStationPreference();
        sendChangeStationBroadcast();
        changeConnectionErrorStatus();
        setWebsiteLinkVisibility();
    }


    private void setReadyStatus(){
        statusTextView.setText(R.string.status_ready);
        sendRequestStatusBroadcast();
        isConnectionErrorShowing = false;
    }


    private void setWebsiteLinkVisibility(){
        TextView websiteLinkTextView = findViewById(R.id.websiteLinkTextView);
        int visibility = stationWebsite == null || stationWebsite.trim().isEmpty() ?
                View.INVISIBLE :
                View.VISIBLE;
            websiteLinkTextView.setVisibility(visibility);
    }


    private void setInitialStatus(){
        setWebsiteLinkVisibility();
        if(currentURL == null){
            currentURL = "https://stream.live.vc.bbcmedia.co.uk/bbc_world_service";
            log("setInitialStatus() currentURL was null");
            updateStatusAfterListChange();
           // hidePlayButton();
            return;
        }
        playButton.setVisibility(View.VISIBLE);
        statusTextView.setText(R.string.status_ready);
    }


    private void updateStatusAfterListChange(){
        if(currentURL == null){
            int statusId = listAdapterHelper.getCount() == 0 ?
                    R.string.status_add_a_station_to_begin
                    : R.string.status_nothing_selected;
            statusTextView.setText(statusId);
            hidePlayButton();
            return;
        }
        sendRequestStatusBroadcast();
    }


    private void hidePlayButton(){
        playButton.setVisibility(View.INVISIBLE);
    }


    private void setupDeleteConfirmationDialog(){
        var dialogClickListener = createDialogClickListener();
        removeFromFavouritesConfirmationDialog = new AlertDialog.Builder(this);
        removeFromFavouritesConfirmationDialog.setMessage(getString(R.string.remove_station_from_favourites_confirmation_dialog_text))
                .setPositiveButton(getString(android.R.string.ok), dialogClickListener)
                .setNegativeButton(getString(android.R.string.cancel), dialogClickListener);
    }


    private DialogInterface.OnClickListener createDialogClickListener(){
        return (dialog, buttonChoice) -> {
            switch (buttonChoice){
                case DialogInterface.BUTTON_POSITIVE:
                    stationsRepository.setAsFavourite(stationEntity, false);
                    refreshListFromDb();
                    dialog.dismiss();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
    }


    private void changeConnectionErrorStatus(){
        if(isConnectionErrorShowing){
            isConnectionErrorShowing = false;
            statusTextView.setText(getString(R.string.status_ready));
        }
    }



    private void unbindService(){
        if (isServiceBound) {
           // unbindService(serviceConnection);
            isServiceBound = false;
        }
    }


    private void startMediaPlayerServiceOLD(){
        Intent mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        getApplicationContext().startForegroundService(mediaPlayerServiceIntent);
    }


    public void startMediaPlayerService(){
        var mediaPlayerServiceIntent = new Intent(this, MediaPlayerService.class);
        var context = getApplicationContext();
        context.startForegroundService(mediaPlayerServiceIntent);
        context.bindService(mediaPlayerServiceIntent, serviceConnection, 0);
    }


    public void startAddStationFragment(){
        String tag = "add_station";
        var fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        var stationDetailFragment = AddStationFragment.newInstance();
        stationDetailFragment.show(fragmentTransaction, tag);
    }


    public void startEditStationFragment(StationEntity station){
        String tag = "edit_station";
        var fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        var bundle = new Bundle();
        bundle.putLong(EditStationFragment.BUNDLE_STATION_ID, station.getId());
        bundle.putString(EditStationFragment.BUNDLE_STATION_NAME, station.getName());
        bundle.putString(EditStationFragment.BUNDLE_STATION_URL, station.getUrl());
        bundle.putString(EditStationFragment.BUNDLE_STATION_DESCRIPTION, station.getDescription());
        bundle.putString(EditStationFragment.BUNDLE_STATION_LINK, station.getLink());
        var editStationFragment = EditStationFragment.newInstance();
        editStationFragment.setArguments(bundle);
        editStationFragment.show(fragmentTransaction, tag);
    }


    private void startAboutAppFragment(){
        String tag = "about_app";
        var fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        AboutAppFragment.newInstance().show(fragmentTransaction, tag);
    }


    public void startFaqFragment(){
        String tag = "faq_dialog";
        View mainLayout = findViewById(R.id.mainLayout);
        var bundle = new Bundle();
        bundle.putInt(FaqDialogFragment.BUNDLE_TOTAL_HEIGHT, mainLayout.getMeasuredHeight());
        var fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        var faqDialogFragment = FaqDialogFragment.newInstance();
        faqDialogFragment.setArguments(bundle);
        faqDialogFragment.show(fragmentTransaction, tag);
    }


    public void startLibraryFragment(){
        String tag = "library";
        View mainLayout = findViewById(R.id.mainLayout);
        var bundle = new Bundle();
        bundle.putInt(FaqDialogFragment.BUNDLE_TOTAL_HEIGHT, mainLayout.getMeasuredHeight());
        var fragmentTransaction = getSupportFragmentManager().beginTransaction();
        removePreviousFragmentTransaction(tag, fragmentTransaction);
        stationLibraryFragment = StationLibraryFragment.newInstance();
        stationLibraryFragment.setArguments(bundle);
        stationLibraryFragment.show(fragmentTransaction, tag);
    }


    private void removePreviousFragmentTransaction(String tag, FragmentTransaction fragmentTransaction){
        Fragment prev = getSupportFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
    }

    /*
    private void sendStartBroadcast() {
        var intent = new Intent(MediaPlayerService.ACTION_START_PLAYER);
        intent.putExtra(MediaPlayerService.TAG_STATION_URL, currentURL);
        intent.putExtra(MediaPlayerService.TAG_STATION_NAME, currentStationName);
        log("sendStartBroadcast() about to send...");
        //sendBroadcast(intent);
    }

     */

    private void startPlayer(){
        mediaPlayerService.play(currentURL, currentStationName);
    }


    private void sendStopBroadcast(){
        var intent = new Intent();
        intent.setAction(MediaPlayerService.ACTION_STOP_PLAYER);
        sendBroadcast(intent);
    }


    private void sendChangeStationBroadcast(){
        var intent = new Intent(MediaPlayerService.ACTION_CHANGE_STATION);
        intent.putExtra(MediaPlayerService.TAG_STATION_NAME, currentStationName);
        intent.putExtra(MediaPlayerService.TAG_STATION_URL, currentURL);
        sendBroadcast(intent);
    }


    private void sendRequestStatusBroadcast(){
        Intent intent = new Intent(MediaPlayerService.ACTION_REQUEST_STATUS);
        sendBroadcast(intent);
    }


    private boolean isStationListEmpty(){
        return listAdapterHelper.getCount() == 0;
    }


    public void refreshListFromDb(){
        var items = stationsRepository.getAllForStationsList();
        items.sort((StationEntity a, StationEntity b) -> (int) (a.getTimeFavouriteWasEnabled() - b.getTimeFavouriteWasEnabled()));
        listAdapterHelper.setupList(items, android.R.layout.simple_list_item_1, findViewById(R.id.noResultsFoundLayout));
        updateStatusAfterListChange();
        if(stationLibraryFragment!= null){
            stationLibraryFragment.refreshList();
        }
        sendUpdateStationCountBroadcast();
    }


    private void sendUpdateStationCountBroadcast(){
        var intent = new Intent();
        intent.setAction(MediaPlayerService.ACTION_UPDATE_STATION_COUNT);
        intent.putExtra(MediaPlayerService.TAG_STATION_COUNT, listAdapterHelper.getCount());
        sendBroadcast(intent);
    }


    private void saveCurrentStationPreference(){
        sharedPreferences = getSharedPreferences();
        var editor = sharedPreferences.edit();
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
        sharedPreferences = getSharedPreferences();
        StationEntity station = buildStationFromPrefs();
        listAdapterHelper.setSelectedIndex(getPreviousStationIndex());
        updateStatusViewOnStop();
        selectStationAfterDelay(station);
    }


    public SharedPreferences getSharedPreferences(){
        return getSharedPreferences("webRadioEditor", MODE_PRIVATE);
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