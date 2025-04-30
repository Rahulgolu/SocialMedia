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
import com.rahul.socialmedias.OnFollowClickListener;
import com.rahul.socialmedias.R;

import java.util.List;

public class UserSuggestionAdapter extends RecyclerView.Adapter<UserSuggestionAdapter.SuggestionViewHolder> {
    private final List<User> userList;
    private final Context context;
    private final OnFollowClickListener followClickListener;

    public UserSuggestionAdapter(List<User> userList, Context context, OnFollowClickListener followClickListener) {
        this.userList = userList;
        this.context = context;
        this.followClickListener = followClickListener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_suggestion,viewGroup,false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, @SuppressLint("RecyclerView") int position) {

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

        // Set click listener for the follow button
        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followClickListener.onFollowClick(user);
                holder.followButton.setText("Following");
                holder.followButton.setEnabled(false);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder{
        ImageView profileImage;
        TextView userName;
        Button followButton;
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.user_img);
            userName = itemView.findViewById(R.id.userName);
            followButton = itemView.findViewById(R.id.follow_button);
        }
    }
}
