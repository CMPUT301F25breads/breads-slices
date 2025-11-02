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

import com.example.slices.Event;
import com.example.slices.adapters.EntrantEventAdapter;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.BrowseFragmentBinding;
import com.example.slices.interfaces.EventActions;
import com.example.slices.interfaces.EventListCallback;

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

        DBConnector db = new DBConnector();

        db.getAllFutureEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                //EventAdapter eventAdapter = new EventAdapter(requireContext(), eventList);
                EventAdapter eventAdapter = new EventAdapter(requireContext(), eventList);
                binding.browseEventList.setAdapter(eventAdapter);
                //binding.browseEventList.setAdapter(new EventAdapter(requireContext(), events));
                //EventAdapter eventAdapter = new EventAdapter(requireContext(), events);
                //binding.browseEventList.setAdapter(eventAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //EventAdapter eventAdapter = new EventAdapter(requireContext(), eventList);
        //binding.browseEventList.setAdapter(eventAdapter);

        //binding.browseEventList.setOnItemClickListener();

        // Testing
//        for(int i = 0; i < 10; i++)
//            events.add(new Event("Testing", "https://letsenhance.io/static/73136da51c245e80edc6ccfe44888a99/396e9/MainBefore.jpg"));
//
//        EventAdapter eventAdapter = new EventAdapter(requireContext(), events);
//        binding.browseEventList.setAdapter(eventAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
