package com.example.slices.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.adapters.ImagesAdapter;
import com.example.slices.models.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for administrators to browse all event images in firestore
 * Loads every event and displays those with images
 * @author Sasieni
 */
public class AdminImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImagesAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.admin_img_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment_content_main
            );
            if (!navController.popBackStack()) {
                requireActivity().onBackPressed();
            }
        });

        // RecyclerView
        recyclerView = view.findViewById(R.id.adminimg_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ImagesAdapter(requireContext(), event -> {
            NavController navController = Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment_content_main);

            Bundle bundle = new Bundle();
            bundle.putString("eventID", String.valueOf(event.getId()));
            bundle.putBoolean("adminMode", true);

            navController.navigate(R.id.eventDetailsFragment, bundle);
        });

        recyclerView.setAdapter(adapter);

        loadAllImages();
    }

    private void loadAllImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> eventList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            Event e = doc.toObject(Event.class);

                            if (e != null &&
                                    e.getEventInfo() != null &&
                                    e.getEventInfo().getImage() != null &&
                                    e.getEventInfo().getImage().getUrl() != null &&
                                    !e.getEventInfo().getImage().getUrl().isEmpty()) {

                                eventList.add(e);
                            }

                        } catch (Exception ex) {
                            Log.e("AdminImages", "Error parsing event", ex);
                        }
                    }

                    adapter.setEvents(eventList);
                })
                .addOnFailureListener(e ->
                        Log.e("AdminImages", "Failed to load events", e)
                );
    }
}