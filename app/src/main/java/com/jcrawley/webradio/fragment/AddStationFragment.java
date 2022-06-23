package com.jcrawley.webradio.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.service.UrlChecker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.jcrawley.webradio.fragment.FragmentUtils.disableButtonWhenAnyEmptyInputs;
import static com.jcrawley.webradio.fragment.FragmentUtils.getTextOf;
import static com.jcrawley.webradio.fragment.FragmentUtils.setupTitle;

public class AddStationFragment extends DialogFragment {

    private MainActivity activity;
    private EditText stationNameEditText, stationUrlEditText, linkEditText;
    private Button saveButton;
    private ExecutorService executorService;


    public static AddStationFragment newInstance() {
        return new AddStationFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        executorService = Executors.newFixedThreadPool(1);
        return inflater.inflate(R.layout.fragment_add_station, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog =  getDialog();
        activity = (MainActivity)getActivity();
        if(activity == null){
            return;
        }
        if(dialog != null){
            dialog.setTitle(activity.getString(R.string.add_station_title));
        }

        saveButton = view.findViewById(R.id.save_button);
        setupTitle(activity, view, R.string.add_station_title);
        setupViews(view);
        setupSaveButton();
        setupCancelButton(view);
        FragmentUtils.setupDimensions(view, activity);
    }


    private void setupMetaDataRetrievalKeyListener(){
        stationUrlEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                executorService.execute(()->{
                    StationEntity stationEntity = UrlChecker.getMetadata(stationUrlEditText.getText().toString().trim());
                    if(stationEntity != null) {
                        updateEditTextsWithValuesFrom(stationEntity);
                    }
                });
               // Toast.makeText(AddStationFragment.this, "asf", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }


    private void updateEditTextsWithValuesFrom(StationEntity stationEntity){
        new Handler(Looper.getMainLooper()).post(() ->{
            stationNameEditText.setText(stationEntity.getName());
            linkEditText.setText(stationEntity.getLink());
        });
    }


    private void setupViews(View parentView){
        stationNameEditText = parentView.findViewById(R.id.stationNameEditText);
        stationUrlEditText = parentView.findViewById(R.id.stationUrlEditText);
        linkEditText = parentView.findViewById(R.id.linkEditText);
        disableButtonWhenAnyEmptyInputs(saveButton, stationNameEditText, stationUrlEditText);
        setupMetaDataRetrievalKeyListener();
    }


    private void setupSaveButton(){
        saveButton.setOnClickListener((View v) -> {
            if(activity == null){
                return;
            }
            StationEntity station = StationEntity.Builder.newInstance()
                    .name(getTextOf(stationNameEditText))
                    .url(getTextOf(stationUrlEditText))
                    .link(getTextOf(linkEditText))
                    .build();
            activity.saveStation(station);
            dismiss();
        });
    }


    private void setupCancelButton(View parentView){
        Button cancelButton = parentView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener((View v)-> dismiss());
    }

}
