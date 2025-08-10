package com.example.literatureuniverse.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.CommentAdapter;
import com.example.literatureuniverse.adapter.FollowStoryAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.FollowStory;
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
    DatabaseReference followRef, storyRef, userRef, userRef2;
    private List<Story> storyList = new ArrayList<>();
    FollowStoryAdapter followStoryAdapter;
    RecyclerView recyclerView;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        followStoryAdapter = new FollowStoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(followStoryAdapter);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef2 = FirebaseDatabase.getInstance().getReference("users");
        followRef = FirebaseDatabase.getInstance().getReference("follows").child(currentUserId);

        showListFollowStory();
    }

    private void showListFollowStory() {
        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Story> followedStories = new ArrayList<>();

                for (DataSnapshot storySnap : snapshot.getChildren()) {
                    String storyId = storySnap.getKey(); // <-- Lấy key làm storyId
                    Long followedAt = storySnap.child("followedAt").getValue(Long.class);
                    Log.d("FOLLOWINGSTORY", "storyId = " + storyId);


                        storyRef.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot storySnap) {
                                Story story = storySnap.getValue(Story.class);
                                if (story != null && !story.isDeleted()) {
                                    // Tạo field tạm để sort
                                    long sortTime = Math.max(followedAt, story.getUpdatedAt());
                                    story.setSortTime(sortTime);
                                    followedStories.add(story);

                                    // Khi đã load đủ số truyện
                                    if (followedStories.size() == snapshot.getChildrenCount()) {
                                        // Sắp xếp
                                        Collections.sort(followedStories, (s1, s2) -> Long.compare(s2.getSortTime(), s1.getSortTime()));

                                        // Hiển thị ra RecyclerView
                                        followStoryAdapter.setData(followedStories);
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
}