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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.SharedViewModel;
import com.example.slices.adapters.NotificationAdapter;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.NotifFragmentBinding;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Event;
import com.example.slices.models.Invitation;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of notifications and invitations for the
 * current user.
 * Invitations are always displayed before general notifications.
 * @author Bhupinder Singh
 */
public class NotifFragment extends Fragment {
    private NotifFragmentBinding binding;
    private DBConnector db;
    private SharedViewModel vm;
    private NotificationAdapter notificationAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = NotifFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        db = new DBConnector();
        // Sets up the recycler view for the notifications
        recyclerView = binding.notificationRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationAdapter = new NotificationAdapter(requireContext());
        recyclerView.setAdapter(notificationAdapter);

        List<Notification> recyclerNotifications = new ArrayList<>();

        // Fetches the notifications and invitations from the database
        // and adds them to the recycler view
        db.getInvitationByRecipientId(vm.getUser().getId(), new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> invitations) {
                for (Notification invitation : invitations) {
                    if (!((Invitation) invitation).isAccepted() && !((Invitation) invitation).isDeclined()) {
                        recyclerNotifications.add(invitation);
                    }
                }
                db.getNotificationByRecipientId(vm.getUser().getId(), new NotificationListCallback() {
                    // Adds the notifications to the recycler view,
                    // This is inside since we want invitations to come before notifications
                    @Override
                    public void onSuccess(List<Notification> notifications) {
                        recyclerNotifications.addAll(notifications);
                        notificationAdapter.setNotifications(recyclerNotifications);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("NotifFragment", "Error fetching notifications", e);
                        Toast.makeText(requireContext(), "Error: Couldn't load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("NotifFragment", "Error fetching invitations", e);
                Toast.makeText(requireContext(), "Error: Couldn't load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

