package com.jcrawley.webradio.list;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcrawley.webradio.R;
import com.jcrawley.webradio.repository.StationEntity;

import java.util.List;

import androidx.annotation.NonNull;

public class CheckedListItemArrayAdapter extends ArrayAdapter<StationEntity> {

    private final Context context;
    private final List<StationEntity> items;

    public CheckedListItemArrayAdapter(Context context, int viewResourceId, List<StationEntity> items) {
        super(context, viewResourceId, items);
        this.context = context;
        this.items = items;
    }


    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.checked_list_item, null);
        }

        StationEntity station = items.get(position);
        if (station == null || station.getName().isEmpty()) {
            return view;
        }

       view.findViewById(R.id.selected_status_icon).setVisibility(station.isFavourite() ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.unselected_status_icon).setVisibility(station.isFavourite()? View.GONE : View.VISIBLE);
        TextView textView = view.findViewById(R.id.stationNameTextView);
        textView.setText(station.getName());
        return view;
    }
}
