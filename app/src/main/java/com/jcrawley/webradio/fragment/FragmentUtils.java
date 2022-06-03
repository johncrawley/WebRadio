package com.jcrawley.webradio.fragment;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jcrawley.webradio.R;

public class FragmentUtils {

    static void disableButtonWhenAnyEmptyInputs(Button saveButton, EditText... editTextViews){
        for(EditText editTextView : editTextViews) {
            editTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    saveButton.setEnabled(!areAnyEmpty(editTextViews));
                }

                @Override
                public void afterTextChanged(Editable editable) { }
            });
        }
    }

    public static boolean areAnyEmpty(EditText... editTexts){
        for(EditText editText : editTexts){
            if(editText.getText().toString().trim().isEmpty()){
                return true;
            }
        }
        return false;
    }

    static void setupDimensions(View rootView, Activity activity){
        DisplayMetrics metrics = FragmentUtils.getDisplayMetrics(activity);
        int width = (int)(metrics.widthPixels /1.5f);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(width, rootView.getLayoutParams().height));
    }


    static void setupTitle(Activity activity, View rootView, int strId ){
        TextView titleText = rootView.findViewById(R.id.fragmentTitleText);
        titleText.setText(activity.getString(strId));
    }


    public static DisplayMetrics getDisplayMetrics(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }


    static String getTextOf(EditText editText){
        return editText.getText().toString().trim();
    }


}
