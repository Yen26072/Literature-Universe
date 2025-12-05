package com.example.literatureuniverse.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.FollowStoryAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowingStory extends BaseActivity {
    private String currentUserId = FirebaseAuth.getInstance().getUid();
    DatabaseReference followRef, storyRef;
    FollowStoryAdapter followStoryAdapter;
    RecyclerView recyclerView;
    List<Story> followedStories;

    private int itemsPerPage = 2;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_following_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView
        Log.d("FOLLOWINGSTORY", "userId = " + currentUserId);

        recyclerView = findViewById(R.id.recyclerFollowStory);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        followStoryAdapter = new FollowStoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(followStoryAdapter);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        followRef = FirebaseDatabase.getInstance().getReference("follows").child(currentUserId);

        showListFollowStory();
    }

    private void showListFollowStory() {
        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followedStories = new ArrayList<>();

                for (DataSnapshot storySnap : snapshot.getChildren()) {
                    String storyId = storySnap.getKey(); // <-- Lấy key làm storyId
                    Long followedAt = storySnap.child("followedAt").getValue(Long.class);
                    Log.d("FOLLOWINGSTORY", "storyId = " + storyId);


                        storyRef.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot storySnap) {
                                Story story = storySnap.getValue(Story.class);
                                if (story != null) {
                                    // Tạo field tạm để sort
                                    long sortTime = Math.max(followedAt, story.getUpdatedAt());
                                    story.setSortTime(sortTime);
                                    followedStories.add(story);

                                    // Khi đã load đủ số truyện
                                    if (followedStories.size() == snapshot.getChildrenCount()) {
                                        // Sắp xếp
                                        Collections.sort(followedStories, (s1, s2) -> Long.compare(s2.getSortTime(), s1.getSortTime()));
                                        currentPage = 1;
                                        updatePagination();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }

                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) followedStories.size() / itemsPerPage);
        Log.d("MyStoryDebug", "Tổng số trang: " + totalPages);
        Log.d("MyStoryDebug", "Tổng số trang: " + totalPages);
        pageTabsLayout.removeAllViews();

        paginationScroll.setVisibility(View.VISIBLE);
        Log.d("MyStoryDebug", "Đã hiển thị tabScroll");
        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;

            // TẠO TEXTVIEW VỚI MARGIN, PADDING ĐẦY ĐỦ
            TextView tab = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 2, 16, 2);
            tab.setLayoutParams(params);

            tab.setText(String.valueOf(i));
            tab.setTextSize(16);
            tab.setPadding(40, 20, 40, 20); // padding giúp tab dễ bấm và dễ nhìn
            tab.setTextColor(i == currentPage ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
            tab.setBackgroundResource(i == currentPage ? R.drawable.page_selected_bg : R.drawable.page_unselected_bg);

            tab.setOnClickListener(v -> {
                currentPage = pageNum;
                updatePagination(); // Cập nhật tab và trang hiện tại
            });
            Log.d("MyStoryDebug", "Tạo tab trang " + i);
            pageTabsLayout.addView(tab);
            Log.d("MyStoryDebug", "Tổng số tab con: " + pageTabsLayout.getChildCount());
        }

//        }

        displayCurrentPage(); // chỉ gọi ở đây
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, followedStories.size());

        Log.d("MyStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<Story> subList = followedStories.subList(start, end);
        followStoryAdapter.setData(subList);
    }
}