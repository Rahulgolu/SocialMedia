package com.rahul.socialmedias;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.rahul.socialmedias.Model.Posts;
import com.rahul.socialmedias.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Utils {

    public  static  boolean connectionAvailable(Context context){
        ConnectivityManager connectivityManager  = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager !=null && connectivityManager.getActiveNetworkInfo()!=null)
        {
            return  connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else {
            return  false;
        }
    }

    public static  void updateDeviceToken(final Context context, String token){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // Get reference to the 'Tokens' node in Firebase Realtime Database
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference tokenRef = rootRef.child(NodeNames.TOKENS).child(currentUser.getUid());

            // Prepare the token data to store in the database
            HashMap<String, String> tokenData = new HashMap<>();
            tokenData.put(NodeNames.DEVICE_TOKEN, token);

            // Set the token value in Firebase Realtime Database
            tokenRef.setValue(tokenData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Token update was successful, you can notify the user
                        Toast.makeText(context, "Device token updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // If the task failed, show an error message
                       // Toast.makeText(context, "Failed to update device token", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {

            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
        }

    }

    public static void storeUserInformation(Context context, User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Use the `userId` as the document ID
        db.collection("users").document(user.getUserId()).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "User information stored successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to store user information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void fetchUserInformation(Context context, UserFetchCallback callback) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();


        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult().toObject(User.class);
                    if (user != null) {
                        callback.onUserFetched(user);
                    } else {
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    callback.onError("Failed to fetch user information");
                }
            });
        } else {
            callback.onError("Please signUp first");
        }
    }


    public static void updatePost(Context context, String postId, HashMap<String, Object> updates) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts").document(postId).update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
