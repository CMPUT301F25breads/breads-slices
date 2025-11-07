package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.adapters.WaitlistAdapter;
import com.example.slices.models.Entrant;
import com.example.slices.models.Waitlist;

import java.util.List;

/**
 * Fragment to display the waitlist for a given event.
 * If no waitlist data is provided, shows a message instead.
 */
public class WaitlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView waitlistCountText, waitlistInfoText;
    private WaitlistAdapter adapter;
    private Waitlist waitlist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.waitlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar setup
        Toolbar toolbar = view.findViewById(R.id.toolbar_waitlist);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity())
                    .getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Initialize UI
        recyclerView = view.findViewById(R.id.recyclerViewWaitlist);
        waitlistCountText = view.findViewById(R.id.waitlistCountText);
        waitlistInfoText = view.findViewById(R.id.waitlistInfoText);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // dummy waitlist
        if (getArguments() != null && getArguments().containsKey("waitlist")) {
            waitlist = (Waitlist) getArguments().getSerializable("waitlist");
        } else {
            waitlist = getMockWaitlist(); // fallback for testing
        }

        updateUI();
    }

    /**
     * Updates the fragment UI depending on whether the waitlist has data.
     */
    private void updateUI() {
        List<Entrant> entrants = waitlist.getEntrants();

        if (entrants == null || entrants.isEmpty()) {
            waitlistInfoText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            waitlistCountText.setText("No entrants in waitlist");
        } else {
            waitlistInfoText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            waitlistCountText.setText(entrants.size() + " entrants in waitlist");

            adapter = new WaitlistAdapter(requireContext(), entrants);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Mock waitlist (for preview/testing)
     */
    private Waitlist getMockWaitlist() {
        Waitlist mock = new Waitlist();
        mock.addEntrant(new Entrant("Alice", "alice@example.com", "555-1111", 1));
        mock.addEntrant(new Entrant("Bob", "bob@example.com", "555-2222", 2));
        mock.addEntrant(new Entrant("Charlie", "charlie@example.com", "555-3333", 3));
        return mock;
    }
}