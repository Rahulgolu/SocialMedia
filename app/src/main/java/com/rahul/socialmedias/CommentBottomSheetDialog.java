package com.rahul.socialmedias;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rahul.socialmedias.Adapter.CommentAdapter;
import com.rahul.socialmedias.Model.Comments;

import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheetDialog extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private EditText commentInput;
    private Button sendButton;
    private CommentAdapter commentAdapter;
    private List<Comments> commentList;
    private String postId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;


    public CommentBottomSheetDialog(String postId) {
        this.postId = postId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_comments, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_comments);
        commentInput = view.findViewById(R.id.editText_comment);
        sendButton = view.findViewById(R.id.button_send);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentList);
        recyclerView.setAdapter(commentAdapter);

        loadComments();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        return view;
    }

    private void loadComments() {
        CollectionReference commentsRef = db.collection("posts").document(postId).collection("comments");
        commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comments comment = doc.toObject(Comments.class);
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void postComment() {
        String text = commentInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("name");
                String profilePic = documentSnapshot.getString("profilePic");

                Comments comment = new Comments(userId, username, profilePic, text, System.currentTimeMillis());

                db.collection("posts").document(postId)
                        .collection("comments").add(comment)
                        .addOnSuccessListener(documentReference -> {
                            commentInput.setText("");
                            loadComments();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to post comment!", Toast.LENGTH_SHORT).show());
            }
        });
    }

}
