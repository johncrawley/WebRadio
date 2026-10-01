package com.jcrawley.webradio.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jcrawley.webradio.R;

public class StationsFragment extends Fragment {
    private MainViewModel viewModel;
    private final ButtonThemeHelper buttonThemeHelper = new ButtonThemeHelper();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        var view = inflater.inflate(R.layout.fragment_stations_list, container, false);
        setupViewModel();
        setupList(view);
        setupToolbar(view);
        setupAddButton(view);
        return view;
    }


    private void setupToolbar(View parentView){
        var toolbar = (MaterialToolbar)parentView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.lists_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_remove_all) {
                showDeleteAllConfirmation();
                return true;
            }
            else if (item.getItemId() == R.id.action_about_app) {
                showAboutDialog();
                return true;
            }
            return false;
        });
    }


    private void showDeleteAllConfirmation() {
        showRemoveConfirmation(requireContext(),R.string.alert_title_delete_all_lists, ()-> viewModel.deleteAllLists());
    }


    private void setupList(View parentView){
        var recyclerView = (RecyclerView)parentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        var adapter = new SimpleListAdapter(this::showListFragment);
        recyclerView.setAdapter(adapter);

        viewModel.getAllLists().observe(getViewLifecycleOwner(), adapter::submitList);
    }


    private void showListFragment(long id, String title){
        var fragment = ItemsFragment.newInstance(title, id);
        viewModel.selectList(id);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


    private void setupViewModel(){
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }


    private void setupAddButton(View parentView){
        var button = (FloatingActionButton) parentView.findViewById(R.id.createListButton);
        button.setOnClickListener(v -> showCreateListDialog());
        buttonThemeHelper.applyThemeAwareTint(button, this);
    }


    private void showCreateListDialog(){
        var dialog = new CreateListDialogFragment();
        dialog.show(getParentFragmentManager(), "CreateListDialog");
    }


    private void showAboutDialog() {
        var dialog = AboutDialogFragment.newInstance();
        dialog.show(requireActivity().getSupportFragmentManager(), "AboutDialog");
    }
}