package com.rahul.socialmedias;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rahul.socialmedias.Adapter.ViewPagerAdapter;
import com.rahul.socialmedias.Model.User;

public class FollowingFollowersActivity extends AppCompatActivity {
    private User currentUser;
    private TextView userName;
    private ImageView back_arrow;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_following_followers);
        initViews();

        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        currentUser = getIntent().getParcelableExtra("user");
        if (currentUser != null) {
            userName.setText(currentUser.getName());
        }

        setupViewPager();
        String type = getIntent().getStringExtra("type");
        if (type != null && type.equals("following")) {
            viewPager.setCurrentItem(0);
        } else if (type != null && type.equals("followers")) {
            viewPager.setCurrentItem(1);
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Following");
                    break;
                case 1:
                    tab.setText("Followers");
                    break;
            }
        }).attach();
    }

    private void initViews() {
        userName = findViewById(R.id.profileName);
        back_arrow = findViewById(R.id.back_arrow);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}