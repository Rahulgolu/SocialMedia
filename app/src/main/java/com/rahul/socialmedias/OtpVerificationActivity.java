package com.rahul.socialmedias;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationActivity extends AppCompatActivity {
    private EditText inputcode1, inputcode2, inputcode3, inputcode4, inputcode5, inputcode6;
    private String verificationId;
    private TextView textmobile;
    private Button btnverify;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp_verification);
        textmobile = findViewById(R.id.textmobile);
        btnverify = findViewById(R.id.btnverify);
        progressBar = findViewById(R.id.progressBar);
        verificationId = getIntent().getStringExtra("verificationId");

        textmobile.setText(String.format(
                "+91-%s", getIntent().getStringExtra("phoneNumber")
        ));

        inputcode1= findViewById(R.id.inputcode1);
        inputcode2= findViewById(R.id.inputcode2);
        inputcode3= findViewById(R.id.inputcode3);
        inputcode4= findViewById(R.id.inputcode4);
        inputcode5= findViewById(R.id.inputcode5);
        inputcode6= findViewById(R.id.inputcode6);

        setupOtpInput();

        btnverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(inputcode1.getText().toString().isEmpty()
                        || inputcode2.getText().toString().isEmpty()
                        || inputcode3.getText().toString().isEmpty()
                        || inputcode4.getText().toString().isEmpty()
                        || inputcode5.getText().toString().isEmpty()
                        || inputcode6.getText().toString().isEmpty()){

                    Toast.makeText(OtpVerificationActivity.this, "please valid code", Toast.LENGTH_SHORT).show();
                    return;
                }

                String code= inputcode1.getText().toString() +
                        inputcode2.getText().toString() +
                        inputcode3.getText().toString() +
                        inputcode4.getText().toString() +
                        inputcode5.getText().toString() +
                        inputcode6.getText().toString();

                if (verificationId != null){
                    progressBar.setVisibility(View.VISIBLE);
                    btnverify.setVisibility(View.INVISIBLE);

                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationId,
                            code
                    );
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                btnverify.setVisibility(View.VISIBLE);

                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(OtpVerificationActivity.this, RegistrationActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                   /* FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (firebaseUser != null) {
                                        String userId = firebaseUser.getUid();
                                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                        // Check if user data already exists
                                        firestore.collection("users").document(userId).get()
                                                .addOnSuccessListener(documentSnapshot -> {
                                                    if (documentSnapshot.exists()) {
                                                        // User data exists, open MainActivity
                                                        User user = documentSnapshot.toObject(User.class);
                                                        Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                                                        intent.putExtra("user", (Parcelable) user);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    } else {
                                                        // No user data, redirect to RegistrationActivity
                                                        Intent intent = new Intent(OtpVerificationActivity.this, RegistrationActivity.class);
                                                        intent.putExtra("phoneNumber", firebaseUser.getPhoneNumber());
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(OtpVerificationActivity.this, "Failed to check user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                    else {
                                        // No user data, redirect to RegistrationActivity
                                        Intent intent = new Intent(OtpVerificationActivity.this, RegistrationActivity.class);
                                        intent.putExtra("phoneNumber", firebaseUser.getPhoneNumber());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }*/
                                } else {
                                    Toast.makeText(getApplicationContext(), "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    }

    private void setupOtpInput(){
        inputcode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!toString().trim().isEmpty()){
                    inputcode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputcode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!toString().trim().isEmpty()){
                    inputcode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputcode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!toString().trim().isEmpty()){
                    inputcode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputcode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!toString().trim().isEmpty()){
                    inputcode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputcode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!toString().trim().isEmpty()){
                    inputcode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}