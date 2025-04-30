package com.rahul.socialmedias;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.rahul.socialmedias.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    private TextInputEditText etmobNo;
    private SignInButton googleSignBtn;
    private View progressBar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;
    private static final int RC_SIGN_IN = 1;
    private boolean isBackPressedOnce = false;
    private String mobNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        etmobNo = findViewById(R.id.etmobNo);
        googleSignBtn = findViewById(R.id.googlesignBtn);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        googleSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

    }

    /*@Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // User is already signed in, fetch user data from Firestore
            Utils.fetchUserInformation(this, new UserFetchCallback() {
                @Override
                public void onUserFetched(User user) {
                    // Successfully fetched user data

                    // Optionally, update the device token
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            if (token != null) {
                                Utils.updateDeviceToken(Login.this, token);
                            }
                        }
                    });

                    progressBar.setVisibility(View.GONE);

                    // Pass user data to MainActivity
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra("user", user);  // Pass the User object to MainActivity
                    startActivity(intent);
                    finish();  // Finish the Login activity so the user can't go back to it
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle error (e.g., show a toast)
                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

            // Show ProgressBar while waiting for the data to be fetched
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            // User is not logged in, stay on the Login screen or trigger Google sign-in
            // No need to do anything here, as the login screen will be shown by default
        }

        if (firebaseUser != null) {
            // User is logged in, proceed with updating the device token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Successfully retrieved the token
                    String token = task.getResult();
                    if (token != null) {
                        // Update the device token in the Firebase database
                        Utils.updateDeviceToken(Login.this, token);
                    }
                } else {
                    // Handle failure to retrieve token
                    Toast.makeText(Login.this, "Failed to retrieve device token", Toast.LENGTH_SHORT).show();
                }
            });

            // Redirect to the MainActivity if the user is logged in
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        } else {

        }

    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (data != null) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {

                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                        mAuth.signInWithCredential(credential).addOnCompleteListener(this, (Task<AuthResult> task1) -> {
                            progressBar.setVisibility(View.GONE);
                            if (task1.isSuccessful()) {
                                firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {

                                    String userId = firebaseUser.getUid();

                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    firestore.collection("users").document(userId).get()
                                            .addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {

                                                if (documentSnapshot.exists()){

                                                    User user = documentSnapshot.toObject(User.class);
                                                    Intent intent = new Intent(Login.this,MainActivity.class);
                                                    intent.putExtra("user", (Parcelable) user);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                                else {

                                                    String name = account.getDisplayName();
                                                    String email = account.getEmail();
                                                    String profilePic = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                                                    String bio = "New user";
                                                    String phoneNumber = "";
                                                    String gender = "";
                                                    String dob = "";
                                                    ArrayList<String> followers = new ArrayList<>();
                                                    ArrayList<String> following = new ArrayList<>();

                                                    User user = new User(userId, name, email, profilePic, bio,phoneNumber,gender,dob, followers, following);

                                                    Utils.storeUserInformation(Login.this, user);

                                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                                    intent.putExtra("user", (Parcelable) user);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            })
                                            .addOnFailureListener(e ->
                                            {
                                                Toast.makeText(Login.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                            });


                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            if (token != null) {
                                                Utils.updateDeviceToken(Login.this, token);
                                            }
                                        }
                                    });
                                }

                            } else {
                                // Sign-in failed
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (ApiException e) {
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(Login.this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Google sign-in data is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signInWithGoogle() {
        progressBar.setVisibility(View.VISIBLE);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void btnLoginClick(View view) {
        mobNo = etmobNo.getText().toString().trim();

        if (mobNo.isEmpty()) {
            etmobNo.setError(getString(R.string.enter_mobNo));
        }  else if (!mobNo.matches("[6-9]\\d{9}")) {
            etmobNo.setError(getString(R.string.invalid_mobNo));
        }
        else {
            if (Utils.connectionAvailable(this)){
                progressBar.setVisibility(View.VISIBLE);

                String formattedMobNo = "+91" + mobNo;

                // Start phone number authentication
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(formattedMobNo)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {

                                progressBar.setVisibility(View.GONE);
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {

                                progressBar.setVisibility(View.GONE);
                                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(Login.this, "Invalid phone number.", Toast.LENGTH_SHORT).show();
                                } else if (e instanceof FirebaseTooManyRequestsException) {
                                    Toast.makeText(Login.this, "Quota exceeded. Try again later.", Toast.LENGTH_SHORT).show();
                                } else {
                                    String errorMessage = e.getMessage();
                                    Log.e("PhoneAuth", "Verification failed", e);
                                    Toast.makeText(Login.this, "Verification failed: " +errorMessage, Toast.LENGTH_SHORT).show();
                                }

                                Log.e("PhoneAuth", "Verification failed", e);
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Code sent to " + mobNo, Toast.LENGTH_SHORT).show();


                                Intent intent = new Intent(Login.this, OtpVerificationActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phoneNumber", mobNo);
                                startActivity(intent);
                            }
                        })
                        .build();

                PhoneAuthProvider.verifyPhoneNumber(options);

            }
            else
            {
                startActivity(new Intent(Login.this, MessageActivity.class));
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                            // Check if the user's data already exists in Firestore
                            firestore.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // User data exists, proceed to MainActivity
                                            User existingUser = documentSnapshot.toObject(User.class);
                                            Intent intent = new Intent(Login.this, MainActivity.class);
                                            intent.putExtra("user", existingUser);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // User data doesn't exist, redirect to RegistrationActivity
                                            Intent intent = new Intent(Login.this, RegistrationActivity.class);
                                            intent.putExtra("phoneNumber", firebaseUser.getPhoneNumber());
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Login.this, "Failed to check user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String token = tokenTask.getResult();
                                    if (token != null) {
                                        Utils.updateDeviceToken(Login.this, token);
                                    }
                                }
                            });
                        }
                    } else {

                        Toast.makeText(Login.this, "Sign-in failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onBackPressed() {

        if (isBackPressedOnce) {
            super.onBackPressed();
            return;
        }

        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        isBackPressedOnce = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackPressedOnce = false;
            }
        }, 2000);

    }
}