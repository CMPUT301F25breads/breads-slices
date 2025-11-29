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

/**
 * Fragment that displays a list of user profiles
 * @author Sasieni
 */
public class AdminProfilesFragment extends Fragment {

    private ProfileAdapter profileAdapter;
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar as back button
        Toolbar toolbar = view.findViewById(R.id.title_profiles);
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
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewProfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        profileAdapter = new ProfileAdapter(requireContext());
        recyclerView.setAdapter(profileAdapter);

        // Search
        searchBar = view.findViewById(R.id.searchBarProfiles);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                profileAdapter.filter(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // Load data
        loadProfiles();
        ProfileController.getAllProfiles(new ProfileListCallback() {
            @Override
            public void onSuccess(java.util.List<Profile> result) {
                if (result.isEmpty()) {
                    System.out.println("No profiles exist in Firestore.");
                } else {
                    System.out.println("Profiles FOUND → Count = " + result.size());
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Did not load profiles " + e.getMessage());
            }
        });
    }

    /**
     * Loads all profiles from firabase
     * @author Sasieni
     */
    private void loadProfiles() {
        ProfileController.getAllProfiles(new ProfileListCallback() {
            @Override
            public void onSuccess(java.util.List<Profile> result) {
                profileAdapter.updateList(result);

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(),
                        "Failed to load profiles: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}