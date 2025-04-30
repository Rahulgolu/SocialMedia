package com.rahul.socialmedias;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {
    private ImageView back_from_post, post_now, added_image;
    private EditText added_caption;
    int PICK_IMAGE_REQUEST=1;
    Uri imageUri;
    String postId,userId,existingImageUrl;
    StorageReference storageRef;
    Spinner privacy_spinner;
    private View progressBar;
    boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);
        back_from_post = findViewById(R.id.back_from_post);
        post_now = findViewById(R.id.post_now);
        added_image = findViewById(R.id.added_image);
        added_caption = findViewById(R.id.added_caption);
        privacy_spinner = findViewById(R.id.privacy_spinner);
        progressBar = findViewById(R.id.progressBar);


        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                R.id.spinner_text,
                getResources().getStringArray(R.array.privacy_options)
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        privacy_spinner.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent.hasExtra("postId") && intent.hasExtra("userId")) {
            isEditing = true;
            postId = intent.getStringExtra("postId");
            userId = intent.getStringExtra("userId");
            loadPostData();
        } else {
            postId = UUID.randomUUID().toString();
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        added_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        post_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    updatePost();
                } else {
                    uploadPost();
                }
            }
        });

        back_from_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadPostData() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(postId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                added_caption.setText(documentSnapshot.getString("content"));
                existingImageUrl = documentSnapshot.getString("imageUrl");

                // Load existing image into ImageView using Glide
                Glide.with(PostActivity.this).load(existingImageUrl).into(added_image);

                // Set privacy option
                boolean isPrivate = documentSnapshot.getBoolean("isPrivate");
                privacy_spinner.setSelection(isPrivate ? 1 : 0);
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(PostActivity.this, "Failed to load post data", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void uploadPost() {

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (added_caption.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please add a caption.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        storageRef = FirebaseStorage.getInstance().getReference("post_images/" + postId);

        // Upload image to Firebase Storage
        storageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the download URL after successful upload
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    savePostToFirestore(uri.toString());  // Save the post data to Firestore with the image URL
                }).addOnFailureListener(e -> {
                    Toast.makeText(PostActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            } else {
                Toast.makeText(PostActivity.this, "Post upload failed.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updatePost() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (imageUri != null) {
            // User selected a new image, upload it
            storageRef = FirebaseStorage.getInstance().getReference("post_images/" + postId);
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> savePostToFirestore(uri.toString()))
            ).addOnFailureListener(e -> {
                Toast.makeText(PostActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });
        } else {
            // No new image selected, update post with existing image
            savePostToFirestore(existingImageUrl);
        }
    }

    private void savePostToFirestore(String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String selectedPrivacy = privacy_spinner.getSelectedItem() != null ? privacy_spinner.getSelectedItem().toString() : "Public";
        boolean isPrivate = "Private".equals(selectedPrivacy);

        Map<String, Object> post = new HashMap<>();
        post.put("postId", postId);
        post.put("userId", userId);
        post.put("content", added_caption.getText().toString().trim());
        post.put("imageUrl", imageUrl);
        post.put("createdAt", System.currentTimeMillis());
        post.put("updatedAt", System.currentTimeMillis());
        post.put("isPrivate", isPrivate);


        db.collection("posts").document(postId).set(post)
                .addOnSuccessListener(aVoid -> {
                   // Toast.makeText(PostActivity.this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    //todo handle refresh ui with callback or eventbus on mainactivity
                    /*Intent intent = new Intent(PostActivity.this, MainActivity.class);
                    startActivity(intent);*/
                    EventBus.getDefault().postSticky(new PostUploadEvent("New post added!"));
                    Log.d("PostActivity", "Event posted successfully");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("PostActivity", "Post upload failed: " + e.getMessage());
                    Toast.makeText(PostActivity.this, "Post upload failed.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK &&
                data!=null && data.getData()!=null){
            imageUri = data.getData();
            added_image.setImageURI(imageUri);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}