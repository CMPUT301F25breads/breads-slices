package com.example.slices.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.SharedViewModel;
import com.example.slices.adapters.EntrantEventAdapter;
import com.example.slices.models.Event;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.BrowseFragmentBinding;
import com.example.slices.interfaces.EventListCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing a list of all events
 * Will later also include search functionality
 * @author Brad Erdely, Raj Prasad
 */
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

        SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        DBConnector db = new DBConnector();

        db.getAllFutureEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                try {
                    eventList.clear();
                    eventList.addAll(events);

                    EntrantEventAdapter eventAdapter = new EntrantEventAdapter(requireContext(), eventList);
                    eventAdapter.setViewModel(vm);
                    binding.browseEventList.setAdapter(eventAdapter);

                } catch (Exception e) {
                    Log.e("BrowseFragment", "Error setting adapter", e);
                    Toast.makeText(requireContext(), "Error displaying events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("BrowseFragment", "Error fetching events", e);

                Toast.makeText(requireContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();

                EntrantEventAdapter eventAdapter = new EntrantEventAdapter(requireContext(), eventList);
                binding.browseEventList.setAdapter(eventAdapter);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
