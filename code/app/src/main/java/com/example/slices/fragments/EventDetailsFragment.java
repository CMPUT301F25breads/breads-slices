package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Event;
import com.example.slices.R;
import com.example.slices.SharedViewModel;
import com.example.slices.controllers.WaitlistController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.databinding.EventDetailsFragmentBinding;
import com.example.slices.models.EventInfo;

/** EventDetailsFragment
 * A fragment for displaying the details of a tapped-on event in the Browse window
 *
 * @Author Raj Prasad
 */
public class EventDetailsFragment extends Fragment {
    private EventDetailsFragmentBinding binding;
    private SharedViewModel vm;

    private boolean isWaitlisted = false;
    private Event e;

    /**
     * updateWaitlistButton
     *     updates the waitlist button text color and background based on waitlist status of the event
     * @param isOn
     *     true if the current user is waitlisted for the event, false if not on waitlist for event
     */
    /* TODO: Make the button updates less ugly in EntrantEventAdapter (shouldn't have
        all that code inside an adapter) - Raj
    */
    private void updateWaitlistButton(boolean isOn) {
        if (isOn) {
            binding.btnJoinWaitlist.setText("Leave Waitlist");
            binding.btnJoinWaitlist.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.red)
            );
            binding.btnJoinWaitlist.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        } else {
            binding.btnJoinWaitlist.setText("Join Waitlist");
            binding.btnJoinWaitlist.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.button_purple)
            );
            binding.btnJoinWaitlist.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        }
    }

    // Inflate binding
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = EventDetailsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the fragment's view has been created.
     * Initialize event details, set up waitlist button behaviours and bind event date to the UI
     * @param view the fragment's root view
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Get args and use them if there are any
        // Or just use the SharedViewModel - Brad
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventID")) {
            int eventId = -1;
            try {
                eventId = Integer.parseInt(args.getString("eventID"));
            } catch (NumberFormatException ex) {
                return;
            }


            EventController.getEvent(eventId, new EventCallback() {
                @Override
                public void onSuccess(Event event) {
                    e = event;
                    setupUI();
                }

                @Override
                public void onFailure(Exception e) {
                }
            });

        } else {
            if (vm.getSelectedEvent() != null) {
                e = vm.getSelectedEvent();
                setupUI();
            }
        }
    }

    // nullify binding, go back to previous view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        vm = null;
    }

    private void setupUI() {
        if(e == null || vm.getUser() == null)
            return;

        final String entrantId = String.valueOf(vm.getUser().getId());
        int eventId = e.getId(); // reserved for future join/leave event call usage

        // initial waitlist state based on event data
        isWaitlisted = vm.isWaitlisted(String.valueOf(eventId));

        // update initial button appearance based on waitlist status
        updateWaitlistButton(isWaitlisted);

        // Waitlist button toggling
        binding.btnJoinWaitlist.setOnClickListener(v -> {
            final String eventIdStr = String.valueOf(eventId);

            // checks to see if waitlisted and communicating with DB for join/leave functions
            if (isWaitlisted) {
                isWaitlisted = false;
                vm.removeWaitlistedId(eventIdStr);
                updateWaitlistButton(isWaitlisted);
                WaitlistController.leave(eventIdStr, entrantId, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        // success means do nothing else
                    }

                    @Override
                    public void onFailure(Exception e1) {
                        // revert on exception
                        isWaitlisted = true;
                        vm.addWaitlistedId(eventIdStr);
                        updateWaitlistButton(isWaitlisted);
                        Toast.makeText(requireContext(),
                                "Failed to leave waitlist. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                isWaitlisted = true;
                vm.addWaitlistedId(eventIdStr);
                updateWaitlistButton(isWaitlisted);

                WaitlistController.join(eventIdStr, entrantId, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        // success means do nothing else
                    }

                    @Override
                    public void onFailure(Exception e1) {
                        isWaitlisted = false;
                        vm.removeWaitlistedId(eventIdStr);
                        updateWaitlistButton(isWaitlisted);
                        Toast.makeText(requireContext(),
                                "Failed to join waitlist. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        EventInfo eventInfo = e.getEventInfo();

        binding.eventTitle.setText(eventInfo.getName());
        binding.eventToolbar.setTitle(eventInfo.getName());
        binding.eventDescription.setText(eventInfo.getDescription());

        // Date/time formatting
        java.util.Date when = (eventInfo.getEventDate() != null) ? eventInfo.getEventDate().toDate() : null;
        String whenText;
        if (when != null) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
                    "h a  |  MMM dd yyyy", java.util.Locale.getDefault());
            whenText = fmt.format(when);
        } else {
            whenText = "Date/time TBD"; // in case of any errors in date/time, failsafe!
        }
        binding.eventDatetime.setText(whenText);
        Glide.with(this.getContext()).load(eventInfo.getImageUrl()).into(binding.eventImage);

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
