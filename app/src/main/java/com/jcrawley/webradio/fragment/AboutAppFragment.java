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
        MainActivity activity = (MainActivity) getActivity();
        if(activity == null){
            return;
        }
        setupOkButton(view);
        setupFaqButton(view, activity);
        FragmentUtils.setupDimensions(view, activity);
    }


    private void setupOkButton(View parentView){
        Button okButton = parentView.findViewById(R.id.okButton);
        okButton.setOnClickListener((View v)-> dismiss());
    }


    private void setupFaqButton(View parentView, MainActivity activity){
        Button okButton = parentView.findViewById(R.id.openFaqButton);
        okButton.setOnClickListener((View v)-> {
            dismiss();
            activity.startFaqFragment();
        });
    }

}
