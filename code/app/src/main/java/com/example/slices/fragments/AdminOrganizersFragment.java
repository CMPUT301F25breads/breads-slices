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
import com.example.slices.adapters.ProfileAdapter;
import com.example.slices.controllers.ProfileController;
import com.example.slices.interfaces.ProfileListCallback;
import com.example.slices.models.Profile;

import java.util.List;

public class AdminOrganizersFragment extends Fragment {

    private ProfileAdapter organizerAdapter;
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.admin_organizers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.title_organizers);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity())
                    .getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(
                    requireActivity(), R.id.nav_host_fragment_content_main);
            if (!navController.popBackStack()) requireActivity().onBackPressed();
        });

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewOrganizers);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        organizerAdapter = new ProfileAdapter(requireContext());
        organizerAdapter.setOrganizerMode(true);
        recyclerView.setAdapter(organizerAdapter);

        // Search bar
        searchBar = view.findViewById(R.id.searchBarOrganizers);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                organizerAdapter.filter(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // Load organizers from Firestore
        loadOrganizers();
    }

    private void loadOrganizers() {
        ProfileController.getAllOrganizers(new ProfileListCallback() {
            @Override
            public void onSuccess(List<Profile> organizers) {
                organizerAdapter.updateList(organizers);

                if (organizers.isEmpty()) {
                    Toast.makeText(requireContext(), "No organizers found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to load organizers: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}