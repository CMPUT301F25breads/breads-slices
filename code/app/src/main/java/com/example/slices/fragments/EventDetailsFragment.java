package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.Event;
import com.example.slices.SharedViewModel;
import com.example.slices.databinding.EventDetailsFragmentBinding;

/** EventDetailsFragment
 * A fragment for displaying the details of a tapped-on event in the Browse window
 * Includes
 * - R.P.
 */
public class EventDetailsFragment extends Fragment {
    private EventDetailsFragmentBinding binding;
    private SharedViewModel vm;

    // Inflate binding
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = EventDetailsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        if (vm.getSelectedEvent() != null) {
            Event e = vm.getSelectedEvent();

            binding.eventTitle.setText(e.getName());
            binding.eventToolbar.setTitle(e.getName());
            binding.eventDescription.setText(e.getDescription());

            // Date/time formatting
            java.util.Date when = (e.getEventDate() != null) ? e.getEventDate().toDate() : null;
            String whenText;
            if (when != null) {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
                        "h a  |  MMM dd yyyy", java.util.Locale.getDefault());
                whenText = fmt.format(when);
            } else {
                whenText = "Date/time TBD"; // in case of any errors in date/time, failsafe!
            }
            binding.eventDatetime.setText(whenText);

            // counts style reflecting the "Waitlist | Participants" from the xml style
            int wlCount = 0; //waitlist count
            if (e.getWaitlist() != null && e.getWaitlist().getEntrants() != null) {
                wlCount = e.getWaitlist().getEntrants().size();
            }
            int participantCount = 0; // actual participants count
            if (e.getEntrants() != null) {
                participantCount = e.getEntrants().size();
            }
            binding.eventCounts.setText(String.format(java.util.Locale.getDefault(),
                    "%d Waitlisted  |  %d Participating", wlCount, participantCount));
        }
    }

    // nullify binding, go back to previous view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        vm = null;
    }

}
