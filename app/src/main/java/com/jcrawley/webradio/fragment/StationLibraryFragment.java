package com.jcrawley.webradio.fragment;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;

import static com.jcrawley.webradio.fragment.FragmentUtils.setupTitle;

public class StationLibraryFragment extends DialogFragment {

    private final String SPINNER_POSITION_PREF = "genre_spinner_position_pref";
    private StationsRepository stationsRepository;
    private ListAdapterHelper listAdapterHelper;
    private MainActivity activity;
    private Spinner genreSpinner;


    public static StationLibraryFragment newInstance() {
        return new StationLibraryFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_station_library, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = (MainActivity) getActivity();
        if(activity == null){
            return;
        }
        setupTitle(activity, view, R.string.station_library_title);
        stationsRepository = activity.getStationRepository();
        setupStationList(activity, view);
        setupCloseButton(view);
        setupAddStationButton(view);
        setupSpinner(view, stationsRepository.getAllGenres());
        FragmentUtils.setupDimensions(view, activity);
    }



    private void setupAddStationButton(View parentView){
        View openButton = parentView.findViewById(R.id.titleBarDeleteButton);
        Drawable openIcon = AppCompatResources.getDrawable(activity,android.R.drawable.stat_sys_download);
        openButton.setBackground(openIcon);
        openButton.setVisibility(View.VISIBLE);
        openButton.setOnClickListener((View v) -> activity.startAddStationFragment());
    }



    private void setupStationList(MainActivity activity, View parentView){
        listAdapterHelper = new ListAdapterHelper(activity,
                parentView.findViewById(R.id.stationsLibraryList),
                this::selectStation,
                this::startEditStationFragment);
    }


    private void selectStation(StationEntity station){
        station.toggleFavouriteStatus();
        stationsRepository.setAsFavourite(station, station.isFavourite());
    }


    private void startEditStationFragment(StationEntity station){
        activity.startEditStationFragment(station);
    }


    @Override
    public void dismiss(){
        super.dismiss();
        activity.refreshListFromDb();
    }


    private void setupCloseButton(View parentView){
        Button okButton = parentView.findViewById(R.id.doneButton);
        okButton.setOnClickListener((View v)-> dismiss());
    }


    public void refreshList(){
        if(genreSpinner == null){
            return;
        }
        setupListFor((String)genreSpinner.getSelectedItem());
    }


    private void setupSpinner(View rootView, List<String> genres){
        genreSpinner = rootView.findViewById(R.id.genreSpinner);
        sortGenres(genres);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(),
                android.R.layout.simple_spinner_dropdown_item, genres);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)adapterView.getItemAtPosition(i);
                setupListFor(item);
                saveSpinnerPositionPreference(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
        genreSpinner.setAdapter(adapter);
        genreSpinner.setOnItemSelectedListener(itemSelectedListener);
        setupGenrePosition();
        refreshList();
    }


    private void setupGenrePosition(){
        SharedPreferences prefs = activity.getSharedPreferences();
        int positionIndex = prefs.getInt(SPINNER_POSITION_PREF, 0);
        genreSpinner.setSelection(positionIndex);
    }


    private void saveSpinnerPositionPreference(int position){
        SharedPreferences.Editor editor = activity.getSharedPreferences().edit();
        editor.putInt(SPINNER_POSITION_PREF, position);
        editor.apply();
    }


    private void sortGenres(List<String> genres){
        Collections.sort(genres);
        if(getContext() != null) {
            String userLabel = getContext().getString(R.string.genre_user_label);
            if(genres.contains(userLabel)){
                genres.remove(userLabel);
                genres.add(0, userLabel);
            }
        }
    }


    private void setupListFor(String genre){
        List<StationEntity> stations = stationsRepository.getFromLibraryWithGenre(genre);
        listAdapterHelper.setupList(stations, true, null);
    }
}
