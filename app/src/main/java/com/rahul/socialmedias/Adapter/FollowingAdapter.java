package com.rahul.socialmedias.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rahul.socialmedias.Model.User;
import com.rahul.socialmedias.OnUnFollowClickListener;
import com.rahul.socialmedias.R;

import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder> {
    private final List<User> userList;
    private final Context context;
    private final OnUnFollowClickListener unfollowClickListener;

    public FollowingAdapter(List<User> userList, Context context, OnUnFollowClickListener unfollowClickListener) {
        this.userList = userList;
        this.context = context;
        this.unfollowClickListener = unfollowClickListener;
    }

    @NonNull
    @Override
    public FollowingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =  LayoutInflater.from(context).inflate(R.layout.item_following,viewGroup,false);
        return new FollowingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingViewHolder holder, @SuppressLint("RecyclerView") int position) {

        User user = userList.get(position);
        holder.userName.setText(user.getName());

        // Load profile image directly from Firestore stored URL
        if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePic())
                    .placeholder(R.drawable.userprofile)
                    .error(R.drawable.userprofile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.userprofile);
        }

        holder.unfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfollowClickListener.onUnfollowClick(user);
                holder.unfollowButton.setText("UnFollow");
                holder.unfollowButton.setEnabled(false);
                notifyItemChanged(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class FollowingViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName;
        Button unfollowButton;
        public FollowingViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.user_img);
            userName = itemView.findViewById(R.id.userName);
            unfollowButton = itemView.findViewById(R.id.unfollow_button);
        }
    }
}
