package com.jcrawley.webradio.fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

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


    static String getTextOf(EditText editText){
        return editText.getText().toString().trim();
    }


}
