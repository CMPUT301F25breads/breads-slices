package com.example.slices.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.adapters.AdminEventAdapter;

import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.Event;
import com.example.slices.testing.DebugLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for Admins to view, filter, and remove events.
 * Displays all events from Firestore in a RecyclerView.
 * @author Sasieni
 */
public class AdminEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminEventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private EditText searchBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity())
                    .getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment_content_main);
            if (!navController.popBackStack()) {
                requireActivity().onBackPressed();
            }
        });

        //RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventAdapter = new AdminEventAdapter(requireContext(), eventList, true); // true = admin mode
        recyclerView.setAdapter(eventAdapter);

        //Search Bar
        searchBar = view.findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        //Load Events from Database

        loadEventsFromDB();
    }

    /**
     * Fetch all events from Firestore via DBConnector
     */
    private void loadEventsFromDB() {
        EventController.getAllEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.updateFullList(events);
                eventAdapter.notifyDataSetChanged();

                DebugLogger.d("AdminEventsFragment", "Loaded " + events.size() + " events");
            }

            @Override
            public void onFailure(Exception e) {
                DebugLogger.d("AdminEventsFragment", "Failed to load events: " + e.getMessage());
                Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }
}