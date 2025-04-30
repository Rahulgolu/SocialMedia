package com.rahul.socialmedias.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rahul.socialmedias.FollowersFragment;
import com.rahul.socialmedias.FollowingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FollowingFragment();
            case 1:
                return new FollowersFragment();
            default:
                return new FollowingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
