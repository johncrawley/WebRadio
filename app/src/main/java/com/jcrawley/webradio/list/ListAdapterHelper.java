package com.jcrawley.webradio.list;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jcrawley.webradio.repository.StationEntity;

import java.util.List;

import androidx.core.util.Consumer;


public class ListAdapterHelper {

    private final Context context;
    private ListItemArrayAdapter arrayAdapter;
    private final ListView list;
    private final Consumer<StationEntity> clickConsumer;
    private final Consumer<StationEntity> longClickConsumer;
    private int selectedIndex;

    public ListAdapterHelper(Context context, ListView list,
                             Consumer<StationEntity> clickConsumer,
                             Consumer<StationEntity> longClickConsumer){
        this.context = context;
        this.list = list;
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

    public void setupList(final List<StationEntity> items, int layoutRes, View noResultsFoundView){
        if(list == null){
            return;
        }
        arrayAdapter = new ListItemArrayAdapter(context, layoutRes, items);

        AdapterView.OnItemLongClickListener longClickListener = (parent, view, position, id) -> {
            if(position < items.size()){
                StationEntity item = items.get(position);
                longClickConsumer.accept(item);
                return true;
            }
            return false;
        };


        AdapterView.OnItemClickListener clickListener = (parent, view, position, id) -> {
            if(position >= items.size()){
                return;
            }
            selectedIndex = position;
            clickConsumer.accept(items.get(position));
        };

        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        list.setAdapter(arrayAdapter);
        setupEmptyView(noResultsFoundView);
        list.setOnItemLongClickListener(longClickListener);
        list.setOnItemClickListener(clickListener);

    }


    private void setupEmptyView(View noResultsFoundView){
        if(noResultsFoundView == null){
            return;
        }
        list.setEmptyView(noResultsFoundView);
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


}
