package com.jcrawley.webradio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jcrawley.webradio.fragment.StationDetailFragment;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;
import com.jcrawley.webradio.repository.StationsRepositoryImpl;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private MediaPlayer player;
    private ListAdapterHelper listAdapterHelper;
    private StationsRepository stationsRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startPauseButton = findViewById(R.id.playPauseButton);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        startPauseButton.setOnClickListener((View view)-> initializeMediaPlayer());
        ListView stationsList = findViewById(R.id.stationsList);
        stationsRepository = new StationsRepositoryImpl(this.getApplicationContext());
        listAdapterHelper = new ListAdapterHelper(this,
                stationsList,
                this::select,
                this::startDeleteConfirmation);
        refreshListFromDb();
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
       // Bundle bundle = new Bundle();
       // bundle.putBoolean(LoadImageDialogFragment.IS_FROM_FILE, isLoadingFromFile);
       // bundle.putString(LoadImageDialogFragment.PHOTO_FILE_PATH_TAG, photoFilePath);
        StationDetailFragment stationDetailFragment = StationDetailFragment.newInstance();
        //stationDetailFragment.setArguments(bundle);
        stationDetailFragment.show(fragmentTransaction, tag);
    }


    private void select(StationEntity listItem){

    }


    private void startDeleteConfirmation(StationEntity listItem){

    }


    public void saveStation(StationEntity stationEntity){
        listAdapterHelper.addToList(stationEntity);
        stationsRepository.createStation(stationEntity);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void refreshListFromDb(){
        List<StationEntity> items = stationsRepository.getAll();
        listAdapterHelper.setupList(items, android.R.layout.simple_list_item_1, findViewById(R.id.noResultsFoundText));
    }



    private void initializeMediaPlayer() {
        player = new MediaPlayer();
        String url = "SOME_URL_HERE";
        player.setAudioAttributes( new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        try {
            //change with setDataSource(Context,Uri);
            player.setDataSource(this, Uri.parse(url));
            player.prepareAsync();
            player.setOnPreparedListener(mp -> player.start());
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }
}