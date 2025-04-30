package com.rahul.socialmedias;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahul.socialmedias.Adapter.FollowingAdapter;
import com.rahul.socialmedias.Model.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    private RecyclerView recyclerView;
    private FollowingAdapter  followingAdapter;
    private List<User> userFollowingList;
    private List<String> currentUserFollowingList;
    private View progressBar;
    String currentUserId;
    private int unfollowCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_following, container, false);
        recyclerView=view.findViewById(R.id.recyclerviewfollowing);
        progressBar=view.findViewById(R.id.profileProgressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userFollowingList = new ArrayList<>();
        currentUserFollowingList = new ArrayList<>();
        followingAdapter=new FollowingAdapter(userFollowingList,getContext(),this::onUnfollowClick);
        recyclerView.setAdapter(followingAdapter);
        loadUserFollowingList();
        return view;
    }

    private void loadUserFollowingList() {
        progressBar.setVisibility(View.VISIBLE);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("following")) {
                        currentUserFollowingList = (List<String>) documentSnapshot.get("following");

                        if (currentUserFollowingList != null && !currentUserFollowingList.isEmpty()) {
                            fetchFollowingUsers();
                        } else {
                            userFollowingList.clear();
                            followingAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }
                    }else {
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void fetchFollowingUsers() {
        FirebaseFirestore.getInstance().collection("users")
                .whereIn("userId", currentUserFollowingList)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userFollowingList.clear();
                    userFollowingList.addAll(querySnapshot.toObjects(User.class));
                    followingAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void onUnfollowClick(User user) {
        // Handle unfollow logic here
        String unfollowUserId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId)
                .update("following", FieldValue.arrayRemove(unfollowUserId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(unfollowUserId)
                            .update("followers", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid2 -> {
                                userFollowingList.remove(user);
                                followingAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                               // updateFollowingCount();
                            })
                            .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
                })
                .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

   /* private void updateFollowingCount() {
        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("following")) {
                        List<String> updatedFollowingList = (List<String>) documentSnapshot.get("following");
                        int updatedFollowingCount = (updatedFollowingList != null) ? updatedFollowingList.size() : 0;

                        EventBus.getDefault().post(new FollowUpdateEvent(updatedFollowingCount));
                        Log.d("FollowUpdate", "Event posted with count: " + updatedFollowingCount);
                    }
                })
                .addOnFailureListener(e -> Log.e("FollowUpdate", "Failed to fetch following count", e));
    }*/


}