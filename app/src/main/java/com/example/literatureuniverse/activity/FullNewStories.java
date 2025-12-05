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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullNewStories extends BaseActivity {
    DatabaseReference storyRef;
    StoryAdapter storyAdapter;
    RecyclerView recyclerView;
    List<Story> newStories;

    private int itemsPerPage = 5;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_new_stories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        recyclerView = findViewById(R.id.recyclerNewStory);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        newStories = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storyAdapter = new StoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(storyAdapter);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");

        showListNewStory();
    }

    private void showListNewStory() {
        storyRef.orderByChild("createdAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        newStories.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Story story = ds.getValue(Story.class);
                            if (story != null) {
                                newStories.add(story);
                            }
                        }
                        // Firebase trả ngược (cũ trước, mới sau), cần đảo ngược
                        Collections.reverse(newStories);
                        currentPage = 1;
                        updatePagination();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) newStories.size() / itemsPerPage);
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
        int end = Math.min(start + itemsPerPage, newStories.size());

        Log.d("MyStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<Story> subList = newStories.subList(start, end);
        storyAdapter.setData(subList);
    }
}