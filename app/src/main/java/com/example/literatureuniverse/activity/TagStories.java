package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.StoryAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TagStories extends BaseActivity {
    private String tagId, tagLabel;
    private TextView tvTitle;
    private RecyclerView rvStories;
    private List<Story> fullStoryList;
    private StoryAdapter storyAdapter;
    private int storiesToLoad = 0;
    private int storiesLoaded = 0;
    private int itemsPerPage = 2;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tag_stories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        tvTitle = findViewById(R.id.tvTagTitle);
        rvStories = findViewById(R.id.rvTagStories);
        pageTabsLayout = findViewById(R.id.tabContainer);
        paginationScroll = findViewById(R.id.tabScroll);

        // Nhận tagId và label từ Intent
        tagId = getIntent().getStringExtra("tagId");
        tagLabel = getIntent().getStringExtra("tagLabel");

        tvTitle.setText("Thể loại: " + tagLabel);

        fullStoryList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this, new ArrayList<>());
        rvStories.setLayoutManager(new LinearLayoutManager(this));
        rvStories.setAdapter(storyAdapter);

        loadStoriesByTag(tagId);
    }

    private void loadStoriesByTag(String tagId) {
        DatabaseReference tagRef = FirebaseDatabase.getInstance()
                .getReference("storyTags")
                .child(tagId);
        Log.d("TagStoryDEBUG", "Load story");

        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullStoryList.clear(); // xóa danh sách cũ
                storiesToLoad = (int) snapshot.getChildrenCount(); // tổng số story cần load
                storiesLoaded = 0;

                if (storiesToLoad == 0) {
                    // Không có story nào
                    storyAdapter.setData(fullStoryList);
                    return;
                }

                for (DataSnapshot storySnap : snapshot.getChildren()) {
                    String storyId = storySnap.getKey();
                    Log.d("TagStoryDEBUG", storyId);
                    loadStoryDetail(storyId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadStoryDetail(String storyId) {
        DatabaseReference storyRef = FirebaseDatabase.getInstance()
                .getReference("stories")
                .child(storyId);

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TagStoryDEBUG", "Snapshot exists: " + snapshot.exists());
                Log.d("TagStoryDEBUG", "Snapshot value: " + snapshot.getValue());

                if (snapshot.exists()) {
                    Story story = snapshot.getValue(Story.class);
                    if (story != null) {
                        story.setStoryId(snapshot.getKey());
                        fullStoryList.add(story);
                        Log.d("TagStoryDEBUG", story.getStoryId());
                    }
                }

                storiesLoaded++;
                if (storiesLoaded == storiesToLoad) {
                    // ✅ Chỉ khi đã load hết tất cả thì mới set adapter
                    Log.d("TagStoryDEBUG", "All stories loaded. Total: " + new ArrayList<>().size());
                    storyAdapter.setData(fullStoryList);
//                    storyAdapter.setData(storyList);
                    currentPage = 1;
                    updatePagination();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) fullStoryList.size() / itemsPerPage);
        pageTabsLayout.removeAllViews();
        paginationScroll.setVisibility(View.VISIBLE);
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
            pageTabsLayout.addView(tab);
        }
        displayCurrentPage(); // chỉ gọi ở đây
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, fullStoryList.size());

        List<Story> subList = fullStoryList.subList(start, end);
        storyAdapter.setData(subList);
    }
}