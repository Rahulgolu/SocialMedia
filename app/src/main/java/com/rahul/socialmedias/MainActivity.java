package com.rahul.socialmedias;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.rahul.socialmedias.Adapter.ProfilePostsAdapter;
import com.rahul.socialmedias.Adapter.UserSuggestionAdapter;
import com.rahul.socialmedias.Model.Posts;
import com.rahul.socialmedias.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView profile_image, addpost;
    private User currentUser;
    private RecyclerView suggestionRecyclerView;
    private ListView postListView;
    private UserSuggestionAdapter suggestionAdapter;
    private List<User> suggestionsList;
    private List<String> currentUserFollowingList;
    private ProfilePostsAdapter postAdapter;
    private List<Posts> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        profile_image = findViewById(R.id.profile_image);
        postListView = findViewById(R.id.postfeed);
        addpost = findViewById(R.id.addpost);
        suggestionRecyclerView = findViewById(R.id.suggestionrecylerview);
        currentUser = getIntent().getParcelableExtra("user");
        suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        suggestionsList = new ArrayList<>();
        currentUserFollowingList = new ArrayList<>();
        suggestionAdapter = new UserSuggestionAdapter(suggestionsList, this, this::onFollowClick);
        suggestionRecyclerView.setAdapter(suggestionAdapter);

        postList = new ArrayList<>();
        postAdapter = new ProfilePostsAdapter(this, postList,false);
        postListView.setAdapter(postAdapter);
        loadCurrentUserFollowingList();
        loadProfileImage();
        loadPosts();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("user",currentUser);
                startActivity(intent);
            }
        });

        addpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = currentUser.getUserId();
        long currentTime = System.currentTimeMillis() / 1000;

        if (currentUserFollowingList == null || currentUserFollowingList.isEmpty()) {
            db.collection("posts")
                    .whereEqualTo("isPrivate", false)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        postList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Posts post = document.toObject(Posts.class);
                            if (post != null) {
                                post = fixLikesField(document, post);


                                if (post.getHiddenBy() != null && post.getHiddenBy().containsKey(currentUserId)) {
                                    continue;
                                }


                                if (post.getSnoozedBy() != null && post.getSnoozedBy().containsKey(currentUserId)) {
                                    long snoozeUntil = post.getSnoozedBy().get(currentUserId);
                                    if (currentTime < snoozeUntil) {
                                        continue;
                                    }
                                }

                                postList.add(post);
                            }

                            if (post != null && post.getLikes() == null) {
                                post.setLikes(new ArrayList<>());
                            }
                        }


                        Collections.sort(postList, (post1, post2) ->
                                Long.compare(post2.getCreatedAt(), post1.getCreatedAt()));

                        postAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error loading posts", e));
        } else {
            db.collection("posts")
                    .whereIn("userId", currentUserFollowingList)
                    .whereEqualTo("isPrivate", false)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        postList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Posts post = document.toObject(Posts.class);
                            if (post != null) {
                                post = fixLikesField(document, post);


                                if (post.getHiddenBy() != null && post.getHiddenBy().containsKey(currentUserId)) {
                                    continue;
                                }


                                if (post.getSnoozedBy() != null && post.getSnoozedBy().containsKey(currentUserId)) {
                                    long snoozeUntil = post.getSnoozedBy().get(currentUserId);
                                    if (currentTime < snoozeUntil) {
                                        continue;
                                    }
                                }

                                postList.add(post);
                            }

                            if (post != null && post.getLikes() == null) {
                                post.setLikes(new ArrayList<>());
                            }
                        }


                        Collections.sort(postList, (post1, post2) ->
                                Long.compare(post2.getCreatedAt(), post1.getCreatedAt()));

                        postAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error loading posts", e));
        }
    }

    private Posts fixLikesField(DocumentSnapshot document, Posts post) {
        if (document.contains("likes")) {
            Object likesField = document.get("likes");

            if (likesField instanceof HashMap) {
                HashMap<String, Object> likesMap = (HashMap<String, Object>) likesField;
                ArrayList<String> likesList = new ArrayList<>(likesMap.keySet());
                post.setLikes(likesList);
            } else if (likesField instanceof List) {
                post.setLikes((ArrayList<String>) likesField);
            }
        }
        return post;
    }


    private void loadCurrentUserFollowingList() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("following")) {
                        currentUserFollowingList.clear();
                        currentUserFollowingList = (List<String>) documentSnapshot.get("following");
                    }
                    loadUserSuggestions();
                });
    }


    private void loadUserSuggestions() {
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    suggestionsList.clear();
                    for (User user : querySnapshot.toObjects(User.class)) {
                        if (!currentUserFollowingList.contains(user.getUserId())) {
                            suggestionsList.add(user);
                        }
                    }
                    suggestionAdapter.notifyDataSetChanged();
                });
    }


    private void onFollowClick(User clickedUser) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).update("following", FieldValue.arrayUnion(clickedUser.getUserId()))
                .addOnSuccessListener(unused -> {
                    currentUserFollowingList.add(clickedUser.getUserId());
                    suggestionsList.remove(clickedUser);
                    suggestionAdapter.notifyDataSetChanged();
                });

        db.collection("users").document(clickedUser.getUserId()).update("followers", FieldValue.arrayUnion(currentUserId));
    }

    private void loadProfileImage() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profilePic")) {
                        String profilePicUrl = documentSnapshot.getString("profilePic");

                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profilePicUrl)
                                    .placeholder(R.drawable.userprofile)
                                    .error(R.drawable.userprofile)
                                    .into(profile_image);
                        } else {
                            profile_image.setImageResource(R.drawable.userprofile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading profile image", e);
                    profile_image.setImageResource(R.drawable.userprofile);
                });
    }

    private void onPostClick(User clickedUser) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("user", clickedUser);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            Log.d("MainActivity", "EventBus Registered");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            Log.d("MainActivity", "EventBus Unregistered");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onPostUploadEvent(PostUploadEvent event) {
        Log.d("MainActivity", "Event received: " + event.getMessage());
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
        loadPosts();
        EventBus.getDefault().removeStickyEvent(PostUploadEvent.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserFollowingList();
        loadUserSuggestions();
    }
}