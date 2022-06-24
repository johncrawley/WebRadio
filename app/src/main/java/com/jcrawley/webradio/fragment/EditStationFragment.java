package com.jcrawley.webradio.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import static com.jcrawley.webradio.fragment.FragmentUtils.getTextOf;
import static com.jcrawley.webradio.fragment.FragmentUtils.setupTitle;


public class EditStationFragment extends DialogFragment {

    private MainActivity activity;
    private EditText stationNameEditText, stationUrlEditText, linkEditText;
    public static final String BUNDLE_STATION_ID = "STATION_ID";
    public static final String BUNDLE_STATION_NAME = "STATION_NAME";
    public static final String BUNDLE_STATION_URL = "STATION_URL";
    public static final String BUNDLE_STATION_DESCRIPTION = "STATION_DESCRIPTION";
    public static final String BUNDLE_STATION_LINK = "STATION_LINK";
    public long stationId;
    private Button updateButton;
    private AlertDialog.Builder deleteConfirmationDialog;


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
            assignText(R.id.linkEditText, rootView, BUNDLE_STATION_LINK);
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
        setupDeleteConfirmationDialog();
        setupTitle(activity, view, R.string.update_station_dialog_title);
        setupViews(view);
        FragmentUtils.setupDimensions(view, activity);
    }


    private void setupViews(View rootView){
        updateButton = rootView.findViewById(R.id.update_button);
        stationNameEditText = rootView.findViewById(R.id.stationNameEditText);
        stationUrlEditText = rootView.findViewById(R.id.stationUrlEditText);
        linkEditText = rootView.findViewById(R.id.linkEditText);
        disableButtonWhenAnyEmptyInputs(updateButton, stationNameEditText, stationUrlEditText);
        setupUpdateButton();
        setupCancelButton(rootView);
        setupDeleteButton(rootView);
    }


    private void setupUpdateButton(){
        disableButtonIfInputsAreEmpty();
        updateButton.setOnClickListener((View v) -> {
            StationEntity station = StationEntity.Builder.newInstance()
                    .id(stationId)
                    .name(getTextOf(stationNameEditText))
                    .url(getTextOf(stationUrlEditText))
                    .link(getTextOf(linkEditText))
                    .build();
            if(activity != null){
                activity.updateStation(station);
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
        View deleteButton = parentView.findViewById(R.id.titleBarDeleteButton);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener((View v) -> {
            if(activity == null){
                return;
            }
            deleteConfirmationDialog.show();
        });
    }


    private void setupDeleteConfirmationDialog(){
        DialogInterface.OnClickListener dialogClickListener = (dialog, buttonChoice) -> {
            switch (buttonChoice){
                case DialogInterface.BUTTON_POSITIVE:
                   activity.deleteStation(stationId);
                    dismiss();
                    dialog.dismiss();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        deleteConfirmationDialog = new AlertDialog.Builder(activity);
        deleteConfirmationDialog.setMessage(getString(R.string.delete_station_confirmation_dialog_text))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener);
    }


    private void setupCancelButton(View parentView){
        Button cancelButton = parentView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener((View v)-> dismiss());
    }

}