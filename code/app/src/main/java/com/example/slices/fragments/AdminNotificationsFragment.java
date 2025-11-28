package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.slices.adapters.NotificationAdapter;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Notification;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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

        // Recycler
        recyclerView = view.findViewById(R.id.notification_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(requireContext());
        recyclerView.setAdapter(adapter);

    }


}