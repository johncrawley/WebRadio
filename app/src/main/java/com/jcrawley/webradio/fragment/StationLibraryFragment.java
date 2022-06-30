package com.jcrawley.webradio.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;
import com.jcrawley.webradio.list.ListAdapterHelper;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.repository.StationsRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class StationLibraryFragment extends DialogFragment {

    private StationsRepository stationsRepository;
    private ListView stationsList;
    private ListAdapterHelper listAdapterHelper;
    private MainActivity activity;

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
        stationsRepository = activity.getStationRepository();
        setupStationList(activity, view);
        setupCloseButton(view);
        setupSpinner(view, stationsRepository.getAllGenres());
        FragmentUtils.setupDimensions(view, activity);
    }



    private void setupStationList(MainActivity activity, View parentView){
        listAdapterHelper = new ListAdapterHelper(activity,
                parentView.findViewById(R.id.stationsLibraryList),
                this::selectStation,
                this::doNothing);
    }


    private void selectStation(StationEntity station){
        station.toggleFavouriteStatus();
        stationsRepository.setAsFavourite(station, station.isFavourite());
    }


    private void doNothing(StationEntity station){

    }

    @Override
    public void dismiss(){
        super.dismiss();
        activity.refreshListFromDb();
    }


    private void setupCloseButton(View parentView){
        Button okButton = parentView.findViewById(R.id.closebutton);
        okButton.setOnClickListener((View v)-> dismiss());
    }


    private void setupSpinner(View rootView, List<String> genres){
        Spinner spinner = rootView.findViewById(R.id.genreSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(rootView.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                genres);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)adapterView.getItemAtPosition(i);
                setupListFor(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(itemSelectedListener);
        setupListFor((String)spinner.getSelectedItem());
    }


    private void setupListFor(String genre){
        List<StationEntity> stations = stationsRepository.getFromLibraryWithGenre(genre);
        //listAdapterHelper.setupList(stations, android.R.layout.simple_list_item_1, null);
        listAdapterHelper.setupList(stations, true, null);
    }
}
