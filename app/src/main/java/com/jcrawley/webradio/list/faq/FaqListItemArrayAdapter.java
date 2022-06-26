package com.jcrawley.webradio.list.faq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jcrawley.webradio.R;

import java.util.List;

import androidx.annotation.NonNull;

public class FaqListItemArrayAdapter extends ArrayAdapter<FaqListItem> {

    private final Context context;
    private final List<FaqListItem> items;

    public FaqListItemArrayAdapter(Context context, int viewResourceId, List<FaqListItem> items){
        super(context, viewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent){
        if(view == null){
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.faq_list_item,null);
        }

        FaqListItem item = items.get(position);
        if(item == null){
            return view;
        }
        setText(view, R.id.faqItemQuestionTextView, item.getQuestionId());
        setText(view, R.id.faqItemAnswerTextView, item.getAnswerId());

        return view;
    }

    private void setText(View parentView, int viewId, int strId){
        TextView textView = parentView.findViewById(viewId);
        textView.setText(context.getString(strId));
    }

}