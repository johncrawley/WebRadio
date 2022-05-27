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

import static com.jcrawley.webradio.fragment.FragmentUtils.areAnyEmpty;
import static com.jcrawley.webradio.fragment.FragmentUtils.disableButtonWhenAnyEmptyInputs;


public class EditStationFragment extends DialogFragment {

    private MainActivity activity;
    private EditText stationNameEditText, stationUrlEditText;
    public static final String BUNDLE_STATION_ID = "STATION_ID";
    public static final String BUNDLE_STATION_NAME = "STATION_NAME";
    public static final String BUNDLE_STATION_URL = "STATION_URL";
    public long stationId;
    private Button updateButton;


    public static EditStationFragment newInstance() {
        return new EditStationFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_station, container, false);
        assignBundleVars(rootView);
        return rootView;
    }


    private void assignBundleVars(View rootView){
        Bundle bundle = getArguments();
        if(bundle !=null){
            stationId = bundle.getLong(BUNDLE_STATION_ID, -1);
            assignText(R.id.stationNameEditText, rootView, BUNDLE_STATION_NAME);
            assignText(R.id.stationUrlEditText, rootView, BUNDLE_STATION_URL);
        }
    }


    private void assignText(int resId, View rootView, String bundleVarName){
        EditText editText = rootView.findViewById(resId);
        Bundle bundle = getArguments();
        if(bundle == null){
            return;
        }
        String text = bundle.getString(bundleVarName, "");
        editText.setText(text);
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
            dialog.setTitle(activity.getString(R.string.update_station_dialog_title));
        }
        setupViews(view);
    }


    private void setupViews(View parentView){
        updateButton = parentView.findViewById(R.id.update_button);
        stationNameEditText = parentView.findViewById(R.id.stationNameEditText);
        stationUrlEditText = parentView.findViewById(R.id.stationUrlEditText);
        disableButtonWhenAnyEmptyInputs(updateButton, stationNameEditText, stationUrlEditText);
        setupUpdateButton();
        setupCancelButton(parentView);
        setupDeleteButton(parentView);
    }


    private void setupUpdateButton(){
        disableButtonIfInputsAreEmpty();
        updateButton.setOnClickListener((View v) -> {
            String name = stationNameEditText.getText().toString();
            String url = stationUrlEditText.getText().toString();
            if(activity != null){
                activity.updateStation(new StationEntity(stationId, name, url));
            }
            dismiss();
        });
    }

    private void disableButtonIfInputsAreEmpty(){
        if(areAnyEmpty(stationNameEditText, stationUrlEditText)){
            updateButton.setEnabled(false);
        }
    }


    private void setupDeleteButton(View parentView){
        Button deleteButton = parentView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener((View v) -> {
            if(activity == null){
                return;
            }
            activity.deleteStation(stationId);
            dismiss();
        });
    }


    private void setupCancelButton(View parentView){
        Button cancelButton = parentView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener((View v)-> dismiss());
    }

}