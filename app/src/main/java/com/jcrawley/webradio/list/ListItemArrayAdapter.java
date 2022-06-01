package com.jcrawley.webradio.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jcrawley.webradio.repository.StationEntity;

import java.util.List;

import androidx.annotation.NonNull;

public class ListItemArrayAdapter extends ArrayAdapter<StationEntity> {

    private final Context context;
    private final List<StationEntity> items;

    public ListItemArrayAdapter(Context context, int viewResourceId, List<StationEntity> items){
        super(context, viewResourceId, items);
        this.context = context;
        this.items = items;
    }


    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent){
        if(view == null){
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(android.R.layout.simple_list_item_1,null);
        }

        StationEntity item = items.get(position);
        if(item == null || item.getName().isEmpty()){
            return view;
        }

        TextView textView = (TextView)view;
        textView.setText(item.getName());
        return view;
    }


}

