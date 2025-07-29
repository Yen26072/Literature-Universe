package com.example.literatureuniverse.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.ViewPagerAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MailBox extends BaseActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mail_box);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupHeader(); // bắt buộc gọi sau setContentView

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(MailBox.this);
        viewPager.setAdapter(adapter);

        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.blue));
        tabLayout.setTabIconTint(ContextCompat.getColorStateList(this, R.color.tab_icon_color));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.ic_inbox);     // hộp thư
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_comment);   // bình luận
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_notification);
            }
        }).attach();
    }
}