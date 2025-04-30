package com.rahul.socialmedias.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahul.socialmedias.CommentBottomSheetDialog;
import com.rahul.socialmedias.Model.Posts;
import com.rahul.socialmedias.Model.User;
import com.rahul.socialmedias.PostActivity;
import com.rahul.socialmedias.ProfileActivity;
import com.rahul.socialmedias.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostsAdapter extends android.widget.BaseAdapter {
    private Context context;
    private List<Posts> postsList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isProfileActivity;
    private User user;

    public ProfilePostsAdapter(Context context, List<Posts> postsList,boolean isProfileActivity) {
        this.context = context;
        this.postsList = postsList;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.isProfileActivity = isProfileActivity;

    }

    @Override
    public int getCount() {
        return postsList.size();
    }

    @Override
    public Object getItem(int position) {
        return postsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.userImage = convertView.findViewById(R.id.user_img);
            viewHolder.username = convertView.findViewById(R.id.username);
            viewHolder.likes = convertView.findViewById(R.id.likes);
            viewHolder.caption = convertView.findViewById(R.id.caption);
            viewHolder.countComment = convertView.findViewById(R.id.comment_count);
           // viewHolder.tags = convertView.findViewById(R.id.tags);
           // viewHolder.comments = convertView.findViewById(R.id.commments);
            viewHolder.timePosted = convertView.findViewById(R.id.timePosted);
            viewHolder.postImage = convertView.findViewById(R.id.post_image);
            viewHolder.heart = convertView.findViewById(R.id.fragment_home_post_viewer_img_heart);
            viewHolder.heartRed = convertView.findViewById(R.id.fragment_home_post_viewer_img_heart_red);
            viewHolder.commentsOpen = convertView.findViewById(R.id.fragment_home_post_viewer_img_comments);
            viewHolder.moreOption = convertView.findViewById(R.id.fragment_home_post_viewer_option);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Posts post = postsList.get(position);
        String userId = post.getUserId();
        String currentUserId = auth.getCurrentUser().getUid();
        DocumentReference postRef = db.collection("posts").document(post.getPostId());


        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.image)
                .into(viewHolder.postImage);

        viewHolder.caption.setText(post.getContent());
        viewHolder.timePosted.setText(formatTimestamp(post.getCreatedAt()));


        if (post.getLikes() != null) {
            viewHolder.likes.setText(String.valueOf(post.getLikes().size()));
        } else {
            viewHolder.likes.setText("0");
        }


        db.collection("posts").document(post.getPostId())
                .collection("comments")
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null) {
                        int newCommentCount = snapshot.size();
                        viewHolder.countComment.setText(String.valueOf(newCommentCount));
                    } else {
                        viewHolder.countComment.setText("0");
                    }
                });

      /*  viewHolder.commentsOpen.setOnClickListener(v -> {
            CommentBottomSheetDialog bottomSheetDialog = new CommentBottomSheetDialog(post.getPostId());
            bottomSheetDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetDialog.getTag());
        });*/


        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            viewHolder.username.setText(user.getName());
                            Glide.with(context)
                                    .load(user.getProfilePic())
                                    .placeholder(R.drawable.userprofile)
                                    .into(viewHolder.userImage);


                            viewHolder.userImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openUserProfile(user);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    viewHolder.username.setText("Unknown User");
                    viewHolder.userImage.setImageResource(R.drawable.userprofile);
                });


       // viewHolder.tags.setText("Tags Placeholder");

        // Check if the user has already liked the post
        boolean isLiked = post.getLikes() != null && post.getLikes().contains(currentUserId);
        updateLikeUI(viewHolder, isLiked);


        viewHolder.heart.setOnClickListener(v -> {
            postRef.update("likes", FieldValue.arrayUnion(currentUserId))
                    .addOnSuccessListener(aVoid -> {
                        if (post.getLikes() == null) {
                            post.setLikes(new ArrayList<>());
                        }
                        post.getLikes().add(currentUserId);
                        updateLikeUI(viewHolder, true);
                        viewHolder.likes.setText(String.valueOf(post.getLikes().size()));
                    });
        });


        viewHolder.heartRed.setOnClickListener(v -> {
            postRef.update("likes", FieldValue.arrayRemove(currentUserId))
                    .addOnSuccessListener(aVoid -> {
                        if (post.getLikes() != null) {
                            post.getLikes().remove(currentUserId);
                        }
                        updateLikeUI(viewHolder, false);
                        viewHolder.likes.setText(String.valueOf(post.getLikes().size()));
                    });
        });

        viewHolder.commentsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CommentBottomSheetDialog bottomSheetDialog = new CommentBottomSheetDialog(post.getPostId());
                bottomSheetDialog.show(((AppCompatActivity) context)
                        .getSupportFragmentManager(), bottomSheetDialog.getTag());
            }
        });

        viewHolder.moreOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.post_options);

                if (!isProfileActivity) {
                    popupMenu.getMenu().removeItem(R.id.action_edit_post);
                }else {
                    popupMenu.getMenu().removeItem(R.id.hide_post);
                    popupMenu.getMenu().removeItem(R.id.ction_snoozepost);
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit_post) {

                        Intent intent = new Intent(context, PostActivity.class);
                        intent.putExtra("postId", post.getPostId());
                        intent.putExtra("userId", post.getUserId());
                        intent.putExtra("content", post.getContent());
                        intent.putExtra("imageUrl", post.getImageUrl());
                        intent.putExtra("createdAt", post.getCreatedAt());

                        context.startActivity(intent);
                        return true;
                    } else if (item.getItemId() == R.id.hide_post) {
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DocumentReference postDocRef = FirebaseFirestore.getInstance().collection("posts").document(post.getPostId());

                        postDocRef.update("hiddenBy." + currentUserId, true)
                                .addOnSuccessListener(aVoid -> {
                                    postsList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Post hidden", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to hide post", Toast.LENGTH_SHORT).show();
                                });

                        return true;
                    } else if (item.getItemId() == R.id.ction_snoozepost) {

                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DocumentReference postDocRef = FirebaseFirestore.getInstance().collection("posts").document(post.getPostId());

                        // Get the current timestamp + 30 days
                        long snoozeUntil = System.currentTimeMillis() / 1000 + (30 * 24 * 60 * 60);

                        postDocRef.update("snoozedBy." + currentUserId, snoozeUntil)
                                .addOnSuccessListener(aVoid -> {
                                    postsList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Post snoozed for 30 days", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to snooze post", Toast.LENGTH_SHORT).show();
                                });

                        return true;

                    }

                    return false;
                });

                popupMenu.show();
            }
        });


        return convertView;
    }

    private void openUserProfile(User user) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("user", user);
        Log.d("loginuser", "user"+ user);
        context.startActivity(intent);
    }

    private void updateLikeUI(ViewHolder viewHolder, boolean isLiked) {
        if (isLiked) {
            viewHolder.heart.setVisibility(View.INVISIBLE);
            viewHolder.heartRed.setVisibility(View.VISIBLE);
        } else {
            viewHolder.heart.setVisibility(View.VISIBLE);
            viewHolder.heartRed.setVisibility(View.INVISIBLE);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private static class ViewHolder {
        CircleImageView userImage;
        TextView username, likes, caption, timePosted,countComment;
        ImageView postImage,heart, heartRed, commentsOpen,moreOption;
    }
}