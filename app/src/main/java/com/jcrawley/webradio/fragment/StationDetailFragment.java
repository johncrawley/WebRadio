package com.jcrawley.webradio.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;
import com.jcrawley.webradio.repository.StationEntity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.jcrawley.webradio.fragment.FragmentUtils.disableButtonWhenAnyEmptyInputs;

public class StationDetailFragment extends DialogFragment {

    private MainActivity activity;
    private EditText stationNameEditText, stationUrlEditText;
    private Button saveButton;


    public static StationDetailFragment newInstance() {
        return new StationDetailFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_station_detail, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog =  getDialog();
        activity = (MainActivity)getActivity();
        if(activity == null){
            System.out.println("onViewCreated, activity is null!");
            return;
        }
        if(dialog != null){
            dialog.setTitle(activity.getString(R.string.add_station_title));
        }

        saveButton = view.findViewById(R.id.save_button);
        setupViews(view);
        setupSaveButton();
        setupCancelButton(view);
    }


    private void setupViews(View parentView){
        stationNameEditText = parentView.findViewById(R.id.stationNameEditText);
        stationUrlEditText = parentView.findViewById(R.id.stationUrlEditText);
        disableButtonWhenAnyEmptyInputs(saveButton, stationNameEditText, stationUrlEditText);
    }


    private void setupSaveButton(){
        saveButton.setOnClickListener((View v) -> {
            String name = stationNameEditText.getText().toString();
            String url = stationUrlEditText.getText().toString();
            if(activity == null){
                System.out.println("Activity is null!");
                return;
            }
            activity.saveStation(new StationEntity(name, url));
            dismiss();
        });
    }


    private void setupCancelButton(View parentView){
        Button cancelButton = parentView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener((View v)-> dismiss());
    }

}
