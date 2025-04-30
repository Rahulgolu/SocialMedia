package com.rahul.socialmedias;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahul.socialmedias.Model.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputLayout fullName,editUsername, editTextEmail,editMobileNo, editBio, editGendar;
    String fname,username,email,mobileno,gender,bio,dob,profileImageString;
    private EditText birth;
    private ImageView profileImage;
    private Button signup_button;
    private View progressBar;
    int year,month,day;
    private static final int GALLERY_REQUEST_CODE = 1;
    private Uri selectedImageUri;
    FirebaseUser firebaseUser;
    String[] genderOptions = {"Male", "Female", "Other"};
    AutoCompleteTextView genderDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        profileImage = findViewById(R.id.profile_image);
        fullName = findViewById(R.id.fullname);
        editUsername = findViewById(R.id.Username);
        editTextEmail = findViewById(R.id.signup_email);
        editMobileNo = findViewById(R.id.mobilenoo);
        editBio = findViewById(R.id.bio);
        editGendar = findViewById(R.id.gender);
        birth = findViewById(R.id.birthdate);
        signup_button = findViewById(R.id.signup_button);
        progressBar = findViewById(R.id.progressBar);
        genderDropdown = findViewById(R.id.genderDropdown);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        genderDropdown.setAdapter(adapter);

        genderDropdown.setOnClickListener(v -> genderDropdown.showDropDown());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            loadUserData(userId);
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });
        birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegistrationActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        birth.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fname = fullName.getEditText().getText().toString().trim();
                username = editUsername.getEditText().getText().toString().trim();
                email = editTextEmail.getEditText().getText().toString().trim();
                mobileno = editMobileNo.getEditText().getText().toString().trim();
                gender = genderDropdown.getText().toString().trim();
                bio = editBio.getEditText().getText().toString().trim();
                dob = birth.getText().toString().trim();


                if (isFormValid()) {
                    progressBar.setVisibility(View.VISIBLE);
                    updateUserData();

                   /*firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        String phoneNumber = firebaseUser.getPhoneNumber();
                        ArrayList<String> followers = new ArrayList<>();
                        ArrayList<String> following = new ArrayList<>();

                        if (selectedImageUri != null) {
                            uploadImageToFirebase(userId, selectedImageUri);
                          //  profileImageString = selectedImageUri.toString();
                        } else {
                            saveUserToFirestore(userId, "");
                           // profileImageString = "";
                        }

                        // Create a User object
                        User user = new User(userId, fname, email, profileImageString, bio, followers, following);

                        // Use the Utils method to store user information
                        Utils.storeUserInformation(RegistrationActivity.this, user);

                        // Redirect to MainActivity after successful registration
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        intent.putExtra("user",(Parcelable)user);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        signup_button.postDelayed(() -> {
                            progressBar.setVisibility(View.GONE);
                            startActivity(intent);
                        }, 2000);
                    } else {
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        Toast.makeText(RegistrationActivity.this, "Failed to fetch user information. Please login again.", Toast.LENGTH_SHORT).show();
                    }*/
                }

            }
        });
    }

    private void updateUserData() {
       // firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            if (selectedImageUri != null) {
                // If New Image Is Selected, Upload to Firebase Storage First
                uploadImageToFirebase(userId, selectedImageUri);
            } else {
                // Otherwise, Just Update Firestore Without Image Change
                saveUserToFirestore(userId, null);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData(String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Set Data to Fields
                            fullName.getEditText().setText(user.getName());
                            editUsername.getEditText().setText(user.getName());
                            editTextEmail.getEditText().setText(user.getEmail());
                            editMobileNo.getEditText().setText(user.getMobileNo());
                            genderDropdown.setText(user.getGender(),false);
                            editBio.getEditText().setText(user.getBio());
                            birth.setText(user.getDob());

                            if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                                Glide.with(this).load(user.getProfilePic()).into(profileImage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
    }

    private void uploadImageToFirebase(String userId, Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images/" + userId + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener((Uri uri) -> {
                        saveUserToFirestore(userId, uri.toString());
                    });
                }
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(RegistrationActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserToFirestore(String userId, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Load existing user data
                User existingUser = documentSnapshot.toObject(User.class);

                // Preserve existing followers & following lists
                ArrayList<String> followers = existingUser.getFollowers();
                ArrayList<String> following = existingUser.getFollowing();

                // Create updated User object
                User updatedUser = new User(userId, fname, email, imageUrl, bio,mobileno,gender,dob, followers, following);

                // Update Firestore
                db.collection("users").document(userId)
                        .set(updatedUser)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                            intent.putExtra("user", (Parcelable) updatedUser);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegistrationActivity.this, "User update failed", Toast.LENGTH_SHORT).show();
                        });

            } else {
                // User does not exist, create a new one
                saveNewUser(userId, imageUrl);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(RegistrationActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
        });
    }

    // Method to create a new user if they don't exist
    private void saveNewUser(String userId, String imageUrl) {
        ArrayList<String> followers = new ArrayList<>();
        ArrayList<String> following = new ArrayList<>();

        User newUser = new User(userId, fname, email, imageUrl, bio,mobileno, gender,dob,followers, following);

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    intent.putExtra("user", (Parcelable) newUser);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegistrationActivity.this, "User registration failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                // Use ContentResolver to decode the image stream
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Set the selected image in the ImageView
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isFormValid() {
        boolean isValid = true;

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
       if (fname.isEmpty()) {
           fullName.setError(getString(R.string.enter_fullname));
           isValid = false;
       }
       if (username.isEmpty()) {
           editUsername.setError(getString(R.string.enter_username));
           isValid = false;
       }
       if (email.isEmpty()) {
           editTextEmail.setError(getString(R.string.enter_email));
           isValid = false;
       }
        if (mobileno.isEmpty()) {
            editMobileNo.setError(getString(R.string.enter_mobileno));
            isValid = false;
        }  else if (!mobileno.matches("[6-9]\\d{9}")) {
            editMobileNo.setError(getString(R.string.invalid_mobNo));
            isValid = false;
        }
        if (gender.isEmpty()) {
            editGendar.setError(getString(R.string.enter_gender));
            isValid = false;
        }
        if (bio.isEmpty()) {
            editBio.setError(getString(R.string.enter_bio));
            isValid = false;
        }


       return isValid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}