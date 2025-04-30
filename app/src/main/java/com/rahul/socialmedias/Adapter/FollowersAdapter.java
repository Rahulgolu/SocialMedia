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
import com.rahul.socialmedias.OnRemoveFollowers;
import com.rahul.socialmedias.R;

import java.util.List;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.FollowersViewHolder> {

    private final List<User> userList;
    private final Context context;
    private final OnRemoveFollowers removeFollowers;

    public FollowersAdapter(List<User> userList, Context context, OnRemoveFollowers removeFollowers) {
        this.userList = userList;
        this.context = context;
        this.removeFollowers = removeFollowers;
    }

    @NonNull
    @Override
    public FollowersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =  LayoutInflater.from(context).inflate(R.layout.item_followers,viewGroup,false);
        return new FollowersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowersViewHolder holder, @SuppressLint("RecyclerView") int position) {

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

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFollowers.removeFollowers(user);
                holder.removeButton.setText("UnFollow");
                holder.removeButton.setEnabled(false);
                notifyItemChanged(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class FollowersViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName;
        Button removeButton;
        public FollowersViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.user_img);
            userName = itemView.findViewById(R.id.userName);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}
