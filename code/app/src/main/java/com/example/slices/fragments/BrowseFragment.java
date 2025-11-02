package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.Event;
import com.example.slices.SharedViewModel;
import com.example.slices.adapters.EntrantEventAdapter;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.BrowseFragmentBinding;
import com.example.slices.interfaces.EventActions;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.Entrant;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {
    private BrowseFragmentBinding binding;
    private ArrayList<Event> eventList = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = BrowseFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        DBConnector db = new DBConnector();

        db.getAllFutureEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);

                // implementation of entrant adapter w/ Join/Leave UI for button
                final EntrantEventAdapter entrantAdapter = new EntrantEventAdapter
                        (requireContext(), eventList);
                entrantAdapter.setViewModel(vm); // gives adapter power to read/write waitlist

                binding.browseEventList.setAdapter(entrantAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
