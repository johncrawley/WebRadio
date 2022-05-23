package com.jcrawley.webradio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ListAdapterHelper listAdapterHelper;
    private StationsRepository stationsRepository;
    private String currentURL;
    private PlayNotifier playNotifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startPauseButton = findViewById(R.id.playPauseButton);
        mediaPlayer = new MediaPlayer();
        startPauseButton.setOnClickListener((View view)-> initializeMediaPlayer());
        findViewById(R.id.stopButton).setOnClickListener((View view) -> stopMediaPlayer());
        ListView stationsList = findViewById(R.id.stationsList);
        stationsRepository = new StationsRepositoryImpl(this.getApplicationContext());
        listAdapterHelper = new ListAdapterHelper(this,
                stationsList,
                this::select,
                this::startEditStationFragment);
        refreshListFromDb();
    }


    private void stopMediaPlayer(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        dismissNotification();
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
        stopPlayer();
        dismissNotification();
    }

    private void stopPlayer(){
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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


    private void initializeMediaPlayer() {
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