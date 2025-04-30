package com.rahul.socialmedias;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahul.socialmedias.Adapter.FollowersAdapter;
import com.rahul.socialmedias.Adapter.FollowingAdapter;
import com.rahul.socialmedias.Model.User;

import java.util.ArrayList;
import java.util.List;


public class FollowersFragment extends Fragment {

    private RecyclerView recyclerView;
    private FollowersAdapter followersAdapter;
    private List<User> userFollowersList;
    private List<String> currentUserFollowersList;
    private View progressBar;
    String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        recyclerView = view.findViewById(R.id.recyclerviewfollowers);
        progressBar = view.findViewById(R.id.profileProgressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userFollowersList = new ArrayList<>();
        currentUserFollowersList = new ArrayList<>();
        followersAdapter = new FollowersAdapter(userFollowersList, getContext(), this::onremove);
        recyclerView.setAdapter(followersAdapter);
        loadUserFollowersList();


        return view;
    }

    private void loadUserFollowersList() {
        progressBar.setVisibility(View.VISIBLE);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("followers")) {
                        currentUserFollowersList = (List<String>) documentSnapshot.get("followers");

                        if (currentUserFollowersList != null && !currentUserFollowersList.isEmpty()) {
                            fetchFollowersDetails();
                        } else {
                            userFollowersList.clear();
                            followersAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void fetchFollowersDetails() {
        FirebaseFirestore.getInstance().collection("users")
                .whereIn("userId", currentUserFollowersList)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userFollowersList.clear();
                    userFollowersList.addAll(querySnapshot.toObjects(User.class));
                    followersAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void onremove(User user) {

        String followerUserId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Remove the follower's userId from the current user's followers list
        db.collection("users").document(currentUserId)
                .update("followers", FieldValue.arrayRemove(followerUserId))
                .addOnSuccessListener(aVoid -> {
                    // Remove the current user's userId from the follower's following list
                    db.collection("users").document(followerUserId)
                            .update("following", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid2 -> {
                                // Remove user from the displayed followers list
                                userFollowersList.remove(user);
                                followersAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }
}