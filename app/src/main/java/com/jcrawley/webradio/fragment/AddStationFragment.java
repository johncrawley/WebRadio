package com.jcrawley.webradio.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;
import com.jcrawley.webradio.repository.StationEntity;
import com.jcrawley.webradio.service.UrlChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;

import static com.jcrawley.webradio.fragment.FragmentUtils.disableButtonWhenAnyEmptyInputs;
import static com.jcrawley.webradio.fragment.FragmentUtils.getTextOf;
import static com.jcrawley.webradio.fragment.FragmentUtils.setupTitle;

public class AddStationFragment extends DialogFragment {

    private MainActivity activity;
    private EditText stationNameEditText, stationUrlEditText, linkEditText;
    private Button saveButton;
    private ExecutorService executorService;

    private ActivityResultLauncher<Intent> loadImageActivityResultLauncher;

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
        activity = (MainActivity)getActivity();
        if(activity == null){
            return;
        }
        setDialogTitle();
        saveButton = view.findViewById(R.id.save_button);
        setupTitle(activity, view, R.string.add_station_title);
        setupViews(view);
        setupSaveButton();
        setupCancelButton(view);
        FragmentUtils.setupDimensions(view, activity);
        setupOpenPlaylistButton(view);
        initLoadFileResultLauncher();
    }


    private void setDialogTitle(){
        Dialog dialog =  getDialog();
        if(dialog != null){
            dialog.setTitle(activity.getString(R.string.add_station_title));
        }
    }


    private void setupOpenPlaylistButton(View parentView){
        View openButton = parentView.findViewById(R.id.titleBarDeleteButton);
        Drawable openIcon = AppCompatResources.getDrawable(activity,R.mipmap.ic_action_collection);
        openButton.setBackground(openIcon);
        openButton.setVisibility(View.VISIBLE);
        openButton.setOnClickListener((View v) -> startOpenDocumentActivity());
    }


    private void initLoadFileResultLauncher(){
        loadImageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result == null){
                        return;
                    }
                    Intent data = result.getData();
                    if(data == null){
                        return;
                    }
                    Uri uri = data.getData();
                    if(uri == null){
                        return;
                    }
                    loadFile(data);
                });
    }


    private void startOpenDocumentActivity(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/x-mpegurl");
        loadImageActivityResultLauncher.launch(intent);
    }


    public void loadFile(Intent data) {
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        try {
            InputStream input = activity.getContentResolver().openInputStream(uri);
            if (input == null) {
                return;
            }
            Reader reader = new InputStreamReader(input);
            BufferedReader buf = new BufferedReader(reader);
            String line = buf.readLine();
            if(line != null) {
                stationUrlEditText.setText(line.trim());
                updateTextFieldsWithMetadata();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.status_error_loading_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMetaDataRetrievalKeyListener(){
        stationUrlEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateTextFieldsWithMetadata();
                return true;
            }
            return false;
        });
    }


    private void updateTextFieldsWithMetadata(){
        executorService.execute(() -> {
            StationEntity stationEntity = UrlChecker.getMetadata(stationUrlEditText.getText().toString().trim());
            if (stationEntity != null) {
                updateEditTextsWithValuesFrom(stationEntity);
            }
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
