package com.jcrawley.webradio.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class StationLibraryFragment extends DialogFragment {


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
        MainActivity activity = (MainActivity) getActivity();
        if(activity == null){
            return;
        }
        setupCloseButton(view);
        FragmentUtils.setupDimensions(view, activity);
    }


    private void setupCloseButton(View parentView){
        Button okButton = parentView.findViewById(R.id.closebutton);
        okButton.setOnClickListener((View v)-> dismiss());
    }


    private void setupAddButton(View parentView){
        Button okButton = parentView.findViewById(R.id.openFaqButton);
        okButton.setOnClickListener((View v)-> dismiss());
    }

}
