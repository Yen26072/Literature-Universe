package com.example.literatureuniverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyStory extends BaseActivity {
    Button btnStart;
    ImageView imgAdd;
    TextView textView7;
    RecyclerView recyclerView;

    private StoryAdapter storyAdapter;
    private List<Story> fullStoryList;

    private String currentUserId;
    private DatabaseReference storyRef, userRef;

    private int itemsPerPage = 1;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    private boolean isStoriesLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        btnStart = findViewById(R.id.btnStart);
        imgAdd = findViewById(R.id.imgAdd);
        textView7 = findViewById(R.id.textView7);
        recyclerView = findViewById(R.id.recyclerMyStory);
        pageTabsLayout = findViewById(R.id.tabContainer);
        paginationScroll = findViewById(R.id.tabScroll);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fullStoryList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(storyAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();
        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
    }

    @Override
    protected void onRoleLoaded(String role) {
        // Ngăn gọi 2 lần
        if (isStoriesLoaded) return;
        isStoriesLoaded = true;
        Log.d("MyStoryDebug", "onRoleLoaded được gọi với role: " + role);

        if ("reader".equals(role)) {
            btnStart.setVisibility(View.VISIBLE);
            textView7.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyStory.this, CreateStory.class);
                    startActivity(intent);
                }
            });

        }
        else if("author".equals(role)){
            btnStart.setVisibility(View.GONE);
            textView7.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            checkPostBanStatus();
            loadMyStories();

            imgAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyStory.this, CreateStory.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void checkPostBanStatus() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean canPost = snapshot.child("canPost").getValue(Boolean.class);
                Long banUntil = snapshot.child("postBanUntil").getValue(Long.class);

                long now = System.currentTimeMillis();

                if (canPost != null && !canPost && banUntil != null && banUntil > now) {
                    imgAdd.setEnabled(false);
                    imgAdd.setAlpha(0.5f);
                    imgAdd.setOnClickListener(null);
                } else {
                    imgAdd.setEnabled(true);
                    imgAdd.setAlpha(1f);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadMyStories() {
        storyRef.orderByChild("authorId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        fullStoryList.clear();

                        for (DataSnapshot storySnap : snapshot.getChildren()) {
                            Story story = storySnap.getValue(Story.class);
                            if (story != null) {
                                fullStoryList.add(story);
                            }
                        }

                        Log.d("MyStoryDebug", "Tổng số truyện: " + fullStoryList.size());

                        Collections.sort(fullStoryList, (s1, s2) -> Long.compare(s2.getUpdatedAt(), s1.getUpdatedAt()));
                        currentPage = 1;
                        updatePagination(); // GỌI 1 CHỖ DUY NHẤT
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) fullStoryList.size() / itemsPerPage);
        Log.d("MyStoryDebug", "Tổng số trang: " + totalPages);
        Log.d("MyStoryDebug", "Tổng số trang: " + totalPages);
        pageTabsLayout.removeAllViews();

//        if (totalPages <= 1) {
//            paginationScroll.setVisibility(View.GONE);
//        } else {
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
        int end = Math.min(start + itemsPerPage, fullStoryList.size());

        Log.d("MyStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<Story> subList = fullStoryList.subList(start, end);
        storyAdapter.setData(subList);
    }

}