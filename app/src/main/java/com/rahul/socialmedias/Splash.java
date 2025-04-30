package com.rahul.socialmedias;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.rahul.socialmedias.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class Splash extends AppCompatActivity {
    private ImageView ivSplash;
    private TextView tvSplash;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        if(getSupportActionBar()!=null)
            getSupportActionBar().hide();

        initViews();

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    // User is signed in, fetch user data from Firestore
                    Utils.fetchUserInformation(Splash.this, new UserFetchCallback() {
                        @Override
                        public void onUserFetched(User user) {

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String token = task.getResult();
                                    if (token != null) {
                                        Utils.updateDeviceToken(Splash.this, token);
                                    }
                                }
                            });
                            if (user != null) {
                                // Pass user data to MainActivity
                                Intent intent = new Intent(Splash.this, MainActivity.class);
                                intent.putExtra("user", user);
                                startActivity(intent);
                                finish();
                            } else {
                               // Toast.makeText(Splash.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                               // navigateToRegistrationActivity();
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Handle errors during fetch operation
                            Toast.makeText(Splash.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // User is not signed in, navigate to Login activity
                    goToLogin();
                }  // Close the Splash activity
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void initViews() {
        ivSplash = findViewById(R.id.ivSplash);
        tvSplash = findViewById(R.id.tvSplash);
        animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
    }

    private void goToLogin() {
        Intent intent = new Intent(Splash.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRegistrationActivity() {
        Intent intent = new Intent(Splash.this, RegistrationActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        ivSplash.startAnimation(animation);
        tvSplash.startAnimation(animation);
    }
}