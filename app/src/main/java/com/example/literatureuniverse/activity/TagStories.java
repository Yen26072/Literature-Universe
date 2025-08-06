package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
    private List<Story> storyList;
    private StoryAdapter storyAdapter;
    private int storiesToLoad = 0;
    private int storiesLoaded = 0;

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

        // Nhận tagId và label từ Intent
        tagId = getIntent().getStringExtra("tagId");
        tagLabel = getIntent().getStringExtra("tagLabel");

        tvTitle.setText("Thể loại: " + tagLabel);

        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this, storyList);
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
                storyList.clear(); // xóa danh sách cũ
                storiesToLoad = (int) snapshot.getChildrenCount(); // tổng số story cần load
                storiesLoaded = 0;

                if (storiesToLoad == 0) {
                    // Không có story nào
                    storyAdapter.setData(storyList);
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
                        storyList.add(story);
                        Log.d("TagStoryDEBUG", story.getStoryId());
                    }
                }

                storiesLoaded++;
                if (storiesLoaded == storiesToLoad) {
                    // ✅ Chỉ khi đã load hết tất cả thì mới set adapter
                    Log.d("TagStoryDEBUG", "All stories loaded. Total: " + storyList.size());
                    List<Story> result = storyList;
                    storyAdapter.setData(new ArrayList<>(result));
//                    storyAdapter.setData(storyList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}