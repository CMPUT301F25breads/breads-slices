package com.example.slices.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.SharedViewModel;
import com.example.slices.adapters.NotificationAdapter;

import com.example.slices.controllers.EventController;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.databinding.NotifFragmentBinding;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.NotificationListCallback;
import com.example.slices.models.Event;
import com.example.slices.models.Invitation;
import com.example.slices.models.NotSelected;
import com.example.slices.models.Notification;

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

        // Sets up the recycler view for the notifications
        recyclerView = binding.notificationRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationAdapter = new NotificationAdapter(requireContext());
        recyclerView.setAdapter(notificationAdapter);

        binding.clearNotificationsButton.setOnClickListener(v -> {
            NotifFragmentBinding b = binding;
            if (!isAdded() || b == null) return;
            b.clearNotificationsButton.setEnabled(false);
            NotificationManager.clearNotificationsForRecipient(vm.getUser().getId(), new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    b.clearNotificationsButton.setEnabled(true);
                    notificationAdapter.setNotifications(new ArrayList<>());
                    b.noNotifText.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Notifications cleared", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    if (!isAdded()) return;
                    b.clearNotificationsButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                }
            });
        });

        List<Notification> recyclerNotifications = new ArrayList<>();

        // Fetches the notifications and invitations from the database
        // and adds them to the recycler view
        NotificationManager.getInvitationByRecipientId(vm.getUser().getId(), new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> invitations) {
                for (Notification invitation : invitations) {
                    if (!((Invitation) invitation).isAccepted() && !((Invitation) invitation).isDeclined()) {
                        recyclerNotifications.add(invitation);
                    }
                }
                NotificationManager.getNotSelectedByRecipientId(vm.getUser().getId(), new NotificationListCallback() {
                    @Override
                    public void onSuccess(List<Notification> notSelected) {
                        for (Notification notSelectedNotification : notSelected) {
                            if (!((NotSelected) notSelectedNotification).isStayed() && !((NotSelected) notSelectedNotification).isDeclined()) {
                                recyclerNotifications.add(notSelectedNotification);
                            }
                        }
                        if (vm.getUser().getProfile().getSendNotifications()) {
                            NotificationManager.getNotificationsByRecipientId(vm.getUser().getId(), new NotificationListCallback() {
                                // Adds the notifications to the recycler view,
                                @Override
                                public void onSuccess(List<Notification> notifications) {
                                    NotifFragmentBinding b = binding;
                                    if (!isAdded() || b == null) return; // view destroyed; ignore callback
                                    for (Notification notification : notifications) {
                                        if (!notification.getRead()) {
                                            recyclerNotifications.add(notification);
                                        }
                                    }

                                    // Sort by timestamp in descending order (most recent first)
                                    recyclerNotifications.sort((n1, n2) -> {
                                        if (n1.getTimestamp() == null && n2.getTimestamp() == null) return 0;
                                        if (n1.getTimestamp() == null) return 1;
                                        if (n2.getTimestamp() == null) return -1;
                                        return n2.getTimestamp().compareTo(n1.getTimestamp());
                                    });

                                    notificationAdapter.setNotifications(recyclerNotifications);
                                    if (recyclerNotifications.isEmpty()) {
                                        b.noNotifText.setVisibility(View.VISIBLE);
                                    } else {
                                        b.noNotifText.setVisibility(View.GONE);
                                    }
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    if (!isAdded()) return;
                                    Log.e("NotifFragment", "Error fetching notifications", e);
                                    Toast.makeText(requireContext(), "Error: Couldn't load notifications", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            NotifFragmentBinding b = binding;
                            if (!isAdded() || b == null) return;
                            recyclerNotifications.sort((n1, n2) -> {
                                if (n1.getTimestamp() == null && n2.getTimestamp() == null) return 0;
                                if (n1.getTimestamp() == null) return 1;
                                if (n2.getTimestamp() == null) return -1;
                                return n2.getTimestamp().compareTo(n1.getTimestamp());
                            });

                            notificationAdapter.setNotifications(recyclerNotifications);
                            if (recyclerNotifications.isEmpty()) {
                                b.noNotifText.setVisibility(View.VISIBLE);
                            } else {
                                b.noNotifText.setVisibility(View.GONE);
                            }
                        }

                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (!isAdded()) return;
                        Log.e("NotifFragment", "Error fetching NotSelected", e);
                        Toast.makeText(requireContext(), "Error: Couldn't load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
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

