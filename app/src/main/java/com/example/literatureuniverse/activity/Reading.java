package com.example.literatureuniverse.activity;

import android.os.Bundle;
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
import com.example.literatureuniverse.adapter.ReadingStoryAdapter;
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
import java.util.concurrent.atomic.AtomicInteger;

public class Reading extends BaseActivity {
    private String currentUserId = FirebaseAuth.getInstance().getUid();
    DatabaseReference bookmarkRef, storyRef, userRef, userRef2;
    private List<Story> storyList = new ArrayList<>();
    ReadingStoryAdapter readingStoryAdapter;
    RecyclerView recyclerView;
    private int itemsPerPage = 2;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        recyclerView = findViewById(R.id.recyclerReadingStory);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        readingStoryAdapter = new ReadingStoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(readingStoryAdapter);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef2 = FirebaseDatabase.getInstance().getReference("users");
        bookmarkRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(currentUserId);

        showListReadingStory();
    }

    private void showListReadingStory() {
        bookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                if (!snapshot.exists()) {
                    readingStoryAdapter.setData(storyList);
                    return;
                }

                // 1. Lấy bookmark vào list tạm
                List<BookmarkItem> bookmarkItems = new ArrayList<>();
                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    String storyId = storySnapshot.getKey();
                    String chapterId = storySnapshot.child("chapterId").getValue(String.class);
                    Long timestamp = storySnapshot.child("timestamp").getValue(Long.class);

                    if (storyId != null && chapterId != null && timestamp != null) {
                        bookmarkItems.add(new BookmarkItem(storyId, chapterId, timestamp));
                    }
                }

                // 2. Sort theo timestamp giảm dần
                Collections.sort(bookmarkItems, (a, b) -> Long.compare(b.timestamp, a.timestamp));

                // 3. Load tất cả stories rồi hiển thị một lần
                List<Story> tempStories = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger(0);
                int total = bookmarkItems.size();

                for (BookmarkItem item : bookmarkItems) {
                    FirebaseDatabase.getInstance().getReference("stories").child(item.storyId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot storyData) {
                                    Story story = storyData.getValue(Story.class);
                                    if (story != null) {
                                        tempStories.add(story);
                                    }

                                    if (counter.incrementAndGet() == total) {
                                        // Khi đã load hết → gán vào storyList và hiển thị
                                        storyList.clear();
                                        storyList.addAll(tempStories);
                                        currentPage = 1;
                                        updatePagination();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    if (counter.incrementAndGet() == total) {
                                        storyList.clear();
                                        storyList.addAll(tempStories);
                                        readingStoryAdapter.setData(storyList);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) storyList.size() / itemsPerPage);
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
        int end = Math.min(start + itemsPerPage, storyList.size());

        List<Story> subList = storyList.subList(start, end);
        readingStoryAdapter.setData(subList);
    }

    // Class tạm lưu dữ liệu bookmark
    private static class BookmarkItem {
        String storyId;
        String chapterId;
        long timestamp;

        BookmarkItem(String storyId, String chapterId, long timestamp) {
            this.storyId = storyId;
            this.chapterId = chapterId;
            this.timestamp = timestamp;
        }
    }

}