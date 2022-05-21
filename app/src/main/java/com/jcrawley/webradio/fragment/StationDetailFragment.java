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

public class StationDetailFragment extends DialogFragment {

    private MainActivity activity;
    private EditText stationNameEditText, stationUrlEditText;

    public static StationDetailFragment newInstance() {
        return new StationDetailFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_station_detail, container, false);

        Bundle bundle = getArguments();
        return rootView;
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
        setupViews(view);
        setupSaveButton(view);
        setupCancelButton(view);
    }


    private void setupViews(View parentView){
        stationNameEditText = parentView.findViewById(R.id.stationNameEditText);
        stationUrlEditText = parentView.findViewById(R.id.stationUrlEditText);
    }


    private void setupSaveButton(View parentView){
        Button saveButton = parentView.findViewById(R.id.save_button);
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
