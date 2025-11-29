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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.adapters.NotificationAdapter;
import com.example.slices.models.AdminNotification;
import com.example.slices.models.Notification;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays notifications sent
 * @author Sasieni
 */
public class AdminNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.adminnotification_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment_content_main);

            if (!navController.popBackStack()) requireActivity().onBackPressed();
        });

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.adminnotification_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(requireContext(), true);
        recyclerView.setAdapter(adapter);

        // Load notifications
        loadAllNotifications();
    }

    private void loadAllNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Log.e("AdminNotifications", "Error loading notifications", error);
                        return;
                    }

                    List<Notification> list = new ArrayList<>();

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {

                            try {
                                AdminNotification raw = doc.toObject(AdminNotification.class);

                                if (raw != null) {


                                    if (doc.contains("type")) {
                                        String typeStr = doc.getString("type");
                                        raw.setType(typeStr);
                                    }

                                    Notification n = new Notification();
                                    n.setTitle(raw.getTitle());
                                    n.setBody(raw.getBody());
                                    n.setId(raw.getId());
                                    n.setRecipientId(raw.getRecipientId() != null ? raw.getRecipientId().intValue() : 0);
                                    n.setSenderId(raw.getSenderId() != null ? raw.getSenderId().intValue() : 0);
                                    n.setEventId(raw.getEventId() != null ? raw.getEventId().intValue() : 0);
                                    n.setRead(raw.getRead() != null && raw.getRead());
                                    n.setTimestamp(raw.getTimestamp());
                                    n.setType(raw.getType());

                                    list.add(n);
                                }

                            } catch (Exception e) {
                                Log.e("AdminNotifications", "Error converting notification", e);
                            }
                        }
                    }

                    adapter.setNotifications(list);
                });
    }
}