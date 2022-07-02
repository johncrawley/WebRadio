package com.jcrawley.webradio.list;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.jcrawley.webradio.R;
import com.jcrawley.webradio.repository.StationEntity;

import java.util.List;

import androidx.core.util.Consumer;


public class ListAdapterHelper {

    private final Context context;
    private ArrayAdapter<StationEntity> arrayAdapter;
    private final ListView listView;
    private final Consumer<StationEntity> clickConsumer;
    private final Consumer<StationEntity> longClickConsumer;
    private int selectedIndex;

    public ListAdapterHelper(Context context, ListView listView,
                             Consumer<StationEntity> clickConsumer,
                             Consumer<StationEntity> longClickConsumer){
        this.context = context;
        this.listView = listView;
        this.clickConsumer = clickConsumer;
        this.longClickConsumer = longClickConsumer;
    }


    public int getCount(){
        if(arrayAdapter == null){
            return 0;
        }
        return arrayAdapter.getCount();
    }


    public void clickFirstItem(){
        clickConsumer.accept(arrayAdapter.getItem(0));
    }


    public StationEntity getNextStation(){
        if(arrayAdapter.getCount() == 0){
            return createEmptyStation();
        }
        selectedIndex++;
        if(selectedIndex >= arrayAdapter.getCount()){
            selectedIndex = 0;
        }
        return arrayAdapter.getItem(selectedIndex);
    }


    public StationEntity getPreviousStation(){
        if(arrayAdapter.getCount() == 0){
            return createEmptyStation();
        }
        selectedIndex--;
        if(selectedIndex < 0){
            selectedIndex = arrayAdapter.getCount()-1;
        }
        return arrayAdapter.getItem(selectedIndex);
    }


    public void setSelectedIndex(int selectedIndex){
        this.selectedIndex = Math.min(arrayAdapter.getCount()-1, selectedIndex);
    }


    public int getSelectedIndex(){
        return selectedIndex;
    }


    private StationEntity createEmptyStation(){
        return new StationEntity("","");
    }

    public void setupList(final List<StationEntity> stations, boolean hasCheckBox, View noResultsFoundView){
        ArrayAdapter<StationEntity> adapter = hasCheckBox
                ? new CheckedListItemArrayAdapter(context, R.layout.checked_list_item, stations)
                : new ListItemArrayAdapter(context, R.layout.station_list_item, stations);

        setupList(stations, adapter, noResultsFoundView);
    }

    public void setupList(final List<StationEntity> items,
                          ArrayAdapter<StationEntity> arrayAdapter,
                          View noResultsFoundView){
        if(listView == null){
            return;
        }
        this.arrayAdapter = arrayAdapter;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(arrayAdapter);
        setupEmptyView(noResultsFoundView);
        listView.setOnItemClickListener(createClickListener(items));
        listView.setOnItemLongClickListener(createLongClickListener(items));
    }


    public void setupList(final List<StationEntity> items, int layoutRes, View noResultsFoundView){
        if(listView == null){
            return;
        }
        arrayAdapter = new ListItemArrayAdapter(context, layoutRes, items);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(arrayAdapter);
        setupEmptyView(noResultsFoundView);
        listView.setOnItemClickListener(createClickListener(items));
        listView.setOnItemLongClickListener(createLongClickListener(items));
    }


    private AdapterView.OnItemClickListener createClickListener(List<StationEntity> stations){
        return (parent, view, position, id) -> {
            if(position >= stations.size()){
                return;
            }
            selectedIndex = position;
            StationEntity station = stations.get(position);
            clickConsumer.accept(station);
            toggleCheckbox(view, station);
        };
    }


    private AdapterView.OnItemLongClickListener createLongClickListener(List<StationEntity> stations){
        return (parent, view, position, id) -> {
            if(position < stations.size()){
                StationEntity item = stations.get(position);
                longClickConsumer.accept(item);
                return true;
            }
            return false;
        };
    }


    private void toggleCheckbox(View listElement, StationEntity station){
        View selectedStatusIcon = listElement.findViewById(R.id.selected_status_icon);
        if( selectedStatusIcon == null){
            return;
        }
        selectedStatusIcon.setVisibility(station.isFavourite() ? View.VISIBLE : View.GONE);
        listElement.findViewById(R.id.unselected_status_icon).setVisibility(station.isFavourite()? View.GONE : View.VISIBLE);
    }


    private void setupEmptyView(View noResultsFoundView){
        if(noResultsFoundView == null){
            return;
        }
        listView.setEmptyView(noResultsFoundView);
    }


    public void addToList(StationEntity item){
        if (contains(item)) {
            return;
        }
        arrayAdapter.add(item);
    }


    public boolean contains(StationEntity item) {
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            StationEntity item1 = arrayAdapter.getItem(i);
            if(item1 == null){
                continue;
            }
            String name = item1.getName();
            if (name.equals(item.getName())) {
                return true;
            }
        }
        return false;
    }


    public void delete(long id){
        int indexToDelete = -1;
        for(int i=0; i< arrayAdapter.getCount(); i++){
            if(arrayAdapter.getItem(i).getId() == id){
                indexToDelete = i;
                break;
            }
        }
        if(indexToDelete > -1){
           arrayAdapter.remove(arrayAdapter.getItem(indexToDelete));
        }
    }

    public void clear(){
        arrayAdapter.clear();
    }


}
