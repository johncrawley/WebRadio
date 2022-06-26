package com.jcrawley.webradio.list.faq;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.List;


public class FaqListHelper {

    private final Context context;
    private final ListView list;

    public FaqListHelper(Context context, ListView list){
        this.context = context;
        this.list = list;
    }


    public void setupList(final List<FaqListItem> items, int layoutRes){
        if(list == null){
            return;
        }
        FaqListItemArrayAdapter arrayAdapter = new FaqListItemArrayAdapter(context, layoutRes, items);
        list.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        list.setAdapter(arrayAdapter);
    }

}
