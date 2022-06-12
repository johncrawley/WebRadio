package com.jcrawley.webradio.fragment;

import android.app.Dialog;
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

public class AboutAppFragment extends DialogFragment {


    public static AboutAppFragment newInstance() {
        return new AboutAppFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_app, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog =  getDialog();
        MainActivity activity = (MainActivity) getActivity();
        if(activity == null){
            return;
        }
        if(dialog != null){
            dialog.setTitle(activity.getString(R.string.add_station_title));
        }
        setupOkButton(view);
        FragmentUtils.setupDimensions(view, activity);
    }


    private void setupOkButton(View parentView){
        Button okButton = parentView.findViewById(R.id.okButton);
        okButton.setOnClickListener((View v)-> dismiss());
    }

}
