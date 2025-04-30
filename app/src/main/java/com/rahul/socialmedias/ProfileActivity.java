package com.rahul.socialmedias;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.rahul.socialmedias.Adapter.ProfilePostsAdapter;
import com.rahul.socialmedias.Model.Posts;
import com.rahul.socialmedias.Model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;

public class ProfileActivity extends AppCompatActivity {

    private User currentUser;
    private ImageView userImg;
    private TextView profileName, displayName, description,gender,dob, txtPosts, txtFollowers, txtFollowing;
    private ListView postlist;
    private View progressBar;
    private Button edit_profile;
    private FirebaseFirestore db;
    private ArrayList<Posts> userPosts;
    private LinearLayout linearLayoutfollowing,linearLayoutfollowers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        initViews();
        db = FirebaseFirestore.getInstance();
        userPosts = new ArrayList<>();
        currentUser = getIntent().getParcelableExtra("user");
        String userId = currentUser.getUserId();
        if (userId != null) {
            loadCurrentUserData(userId);
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RegistrationActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });

        linearLayoutfollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowingFollowersActivity.class);
                intent.putExtra("user", currentUser);
                intent.putExtra("type", "following");
                startActivity(intent);
            }
        });

        linearLayoutfollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FollowingFollowersActivity.class);
                intent.putExtra("user", currentUser);
                intent.putExtra("type", "followers");
                startActivity(intent);
            }
        });
    }

    private void loadCurrentUserData(String userId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            loadUserData();
                            loadUserPosts();
                        }
                    } else {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load user data!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        if (currentUser != null){
            String profilePicUrl = currentUser.getProfilePic();
            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                Glide.with(this)
                        .load(profilePicUrl)
                        .placeholder(R.drawable.userprofile)
                        .error(R.drawable.userprofile)
                        .into(userImg);
            } else {
                userImg.setImageResource(R.drawable.userprofile);
            }
            profileName.setText(currentUser.getName());
            displayName.setText(currentUser.getName());
            description.setText(currentUser.getBio());
            String userGender = currentUser.getGender();
            String userDOB = currentUser.getDob();
            if (userGender != null && !userGender.trim().isEmpty()) {
                gender.setVisibility(View.VISIBLE);
                gender.setText(userGender);
            } else {
                gender.setVisibility(View.GONE);
            }
            dob.setVisibility((userDOB != null && !userDOB.trim().isEmpty()) ? View.VISIBLE : View.GONE);
            dob.setText(userDOB);
            txtPosts.setText(String.valueOf(userPosts.size()));
            txtFollowers.setText(String.valueOf(currentUser.getFollowers().size()));
            txtFollowing.setText(String.valueOf(currentUser.getFollowing().size()));

        }
    }


    private void loadUserPosts() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("posts")
                .whereEqualTo("userId", currentUser.getUserId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        userPosts.clear();
                        userPosts.addAll(querySnapshot.toObjects(Posts.class));

                        Collections.sort(userPosts, (post1, post2) ->
                                Long.compare(post2.getCreatedAt(), post1.getCreatedAt()));


                        ProfilePostsAdapter adapter = new ProfilePostsAdapter(this, userPosts,true);
                        postlist.setAdapter(adapter);

                        txtPosts.setText(String.valueOf(userPosts.size()));
                    } else {
                        Toast.makeText(this, "No posts found!", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void initViews() {
        userImg = findViewById(R.id.user_img);
        profileName = findViewById(R.id.profileName);
        displayName = findViewById(R.id.display_name);
        description = findViewById(R.id.description);
        gender = findViewById(R.id.gender);
        dob = findViewById(R.id.dob);
        txtPosts = findViewById(R.id.txtPosts);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        postlist = findViewById(R.id.gridview1);
        progressBar = findViewById(R.id.profileProgressBar);
        db = FirebaseFirestore.getInstance();
        userPosts = new ArrayList<>();
        edit_profile =  findViewById(R.id.edit_profile);
        linearLayoutfollowing = findViewById(R.id.FragmentProfile_followingLinearLayout);
        linearLayoutfollowers = findViewById(R.id.FragmentProfile_followerLinearLayout);

    }

    /*@Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            Log.d("EventBus", "EventBus Registered in ProfileActivity");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            Log.d("EventBus", "EventBus Unregistered in ProfileActivity");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowUpdate(FollowUpdateEvent event) {
        txtFollowing.setText(String.valueOf(event.getFollowingCount()));
        Log.d("FollowUpdate", "Updated following count: " + event.getFollowingCount());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
*/
  /*  @Override
    protected void onResume() {
        super.onResume();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId != null) {
          //  loadCurrentUserData(currentUserId);
        }
    }*/


}