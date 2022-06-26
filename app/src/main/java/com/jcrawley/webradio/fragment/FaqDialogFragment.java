package com.jcrawley.webradio.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.jcrawley.webradio.MainActivity;
import com.jcrawley.webradio.R;
import com.jcrawley.webradio.list.faq.FaqListHelper;
import com.jcrawley.webradio.list.faq.FaqListItem;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class FaqDialogFragment extends DialogFragment {

    public final static String BUNDLE_TOTAL_HEIGHT = "totalHeight";
    FaqListHelper faqListHelper;
    private int totalAvailableHeight;


    public static FaqDialogFragment newInstance() {
        return new FaqDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faq, container, false);
        assignBundleVars(rootView);
        return rootView;
    }



    private void assignBundleVars(View rootView){
        Bundle bundle = getArguments();
        if(bundle !=null){
            totalAvailableHeight = bundle.getInt(BUNDLE_TOTAL_HEIGHT, 500);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        if(activity == null){
            return;
        }
        setupOkButton(view);
        setupFaqList(view);
        setupTitle(activity, view, R.string.faq_dialog_title);
        FragmentUtils.setupDimensions(view, activity);
        setupPostViewCreated(view);
    }


    private void setupPostViewCreated(View parentView){
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    private void setupFaqList(View parentView){
        List<FaqListItem> faqItems = Arrays.asList(
                new FaqListItem(R.string.faq_question_1, R.string.faq_answer_1),
                new FaqListItem(R.string.faq_question_2, R.string.faq_answer_2),
                new FaqListItem(R.string.faq_question_3, R.string.faq_answer_3)
        );

        faqListHelper = new FaqListHelper(getContext(), parentView.findViewById(R.id.faqList));
        faqListHelper.setupList(faqItems, R.layout.faq_list_item);

    }



    private void setupOkButton(View parentView){
        Button okButton = parentView.findViewById(R.id.faqOkButton);
        okButton.setOnClickListener((View v)-> dismiss());
    }


    static void setupTitle(Activity activity, View rootView, int strId ){
        TextView titleText = rootView.findViewById(R.id.fragmentTitleText);
        if(titleText == null){
            return;
        }
        titleText.setText(activity.getString(strId));
    }

}
