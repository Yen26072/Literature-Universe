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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.StoryAdapter;
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

public class Library extends BaseActivity {
    private String currentUserId = FirebaseAuth.getInstance().getUid();
    DatabaseReference libraryRef, storyRef;
    StoryAdapter libraryStoryAdapter;
    RecyclerView recyclerView;
    List<Story> libraryStories = new ArrayList<>();
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
        setContentView(R.layout.activity_library);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        recyclerView = findViewById(R.id.recyclerLibraryStory);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        libraryStoryAdapter = new StoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(libraryStoryAdapter);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        libraryRef = FirebaseDatabase.getInstance().getReference("libraries").child(currentUserId);

        showListLibraryStory();
    }

    private void showListLibraryStory() {
        libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        libraryStories.clear();
                        for (DataSnapshot storySnap : snapshot.getChildren()){
                            String storyId = storySnap.getKey();
                            Long addAt = storySnap.child("addedAt").getValue(Long.class);
                            Log.d("LIBRARY", "storyId = " + storyId + "   addAt = " + addAt);
                            storyRef.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot storysnapshot) {
                                    Story story = storysnapshot.getValue(Story.class);
                                    if (story!=null){
                                        long sortTime = addAt;
                                        story.setSortTime(sortTime);
                                        Log.d("LIBRARY", "sortTime = " + story.getSortTime());
                                        libraryStories.add(story);
                                        // Khi đã load đủ số truyện
                                        if (libraryStories.size() == snapshot.getChildrenCount()){
                                            Collections.sort(libraryStories, (s1, s2) -> Long.compare(s2.getSortTime(), s1.getSortTime()));
                                            currentPage = 1;
                                            updatePagination();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) libraryStories.size() / itemsPerPage);
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

//        }

        displayCurrentPage(); // chỉ gọi ở đây
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, libraryStories.size());
        List<Story> subList = libraryStories.subList(start, end);
        libraryStoryAdapter.setData(subList);
    }
}