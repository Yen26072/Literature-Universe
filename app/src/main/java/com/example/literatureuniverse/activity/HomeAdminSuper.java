package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class HomeAdminSuper extends BaseActivity {
    TextView txtTotalStories, txtAdminCount, txtAuthorCount, totalPendingReport;
    LinearLayout linearCommentReport, linearStoryReport, linearReviewReport, linearAppeal;
    private RecyclerView rvBestStories;
    private StoryAdapter bestStoryAdapter;
    private List<Story> bestStories = new ArrayList<>();
    private DatabaseReference storiesRef, usersRef;

    private long pendingComment = 0;
    private long pendingReview = 0;
    private long pendingStory = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin_super);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        txtTotalStories = findViewById(R.id.txtTotalStories);
        rvBestStories = findViewById(R.id.recyclerFeatured);
        txtAdminCount = findViewById(R.id.txtActiveAdmins);
        txtAuthorCount = findViewById(R.id.txtTotalAuthors);
        totalPendingReport = findViewById(R.id.txtPendingReports);
        linearCommentReport = findViewById(R.id.linearCommentReport);
        linearStoryReport = findViewById(R.id.linearStoryReport);
        linearReviewReport = findViewById(R.id.linearReviewReport);
        linearAppeal = findViewById(R.id.linearAppeal);

        rvBestStories.setLayoutManager(new LinearLayoutManager(this));
        bestStoryAdapter = new StoryAdapter(this, bestStories);
        rvBestStories.setAdapter(bestStoryAdapter);

        storiesRef = FirebaseDatabase.getInstance().getReference("stories");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        linearCommentReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Comment Report");
                Intent intent = new Intent(HomeAdminSuper.this, FullPendingCommentReport.class);
                startActivity(intent);
            }
        });
        linearStoryReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Story Report");
                Intent intent = new Intent(HomeAdminSuper.this, FullPendingStoryReport.class);
                startActivity(intent);
            }
        });
        linearReviewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Review Report");
                Intent intent = new Intent(HomeAdminSuper.this, FullPendingReviewReport.class);
                startActivity(intent);
            }
        });
        linearAppeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Review Appeal");
                Intent intent = new Intent(HomeAdminSuper.this, FullAppeal.class);
                startActivity(intent);
            }
        });

        loadPendingCount("commentReports", "comment");
        loadPendingCount("reviewReports", "review");
        loadPendingCount("storyReports", "story");

        loadNumberStories();
        loadNumberAdmins();
        loadNumberAuthors();
        loadBestStories();
    }

    private void loadPendingCount(String node, String type) {
        FirebaseDatabase.getInstance().getReference(node)
                .orderByChild("status").equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long count = snapshot.getChildrenCount();

                        switch (type) {
                            case "comment":
                                pendingComment = count;
                                break;
                            case "review":
                                pendingReview = count;
                                break;
                            case "story":
                                pendingStory = count;
                                break;
                        }

                        updateTotalPending();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateTotalPending() {
        long total = pendingComment + pendingReview + pendingStory;
        totalPendingReport.setText(String.valueOf(total));
    }

    private void loadNumberAuthors() {
        usersRef.orderByChild("role").equalTo("author")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long authorCount = snapshot.getChildrenCount(); // 🔥 số lượng tác giả

                        txtAuthorCount.setText(String.valueOf(authorCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadNumberAdmins() {
        usersRef.orderByChild("role").equalTo("admin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long adminCount = snapshot.getChildrenCount(); // 🔥 số lượng admin

                        txtAdminCount.setText(String.valueOf(adminCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadNumberStories() {
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long totalStories = snapshot.getChildrenCount(); // 🔥 tổng số truyện

                txtTotalStories.setText(String.valueOf(totalStories));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadBestStories() {
        storiesRef.orderByChild("views").limitToLast(3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<Story> topStories = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Story story = snap.getValue(Story.class);
                            if (story != null) {
                                story.setStoryId(snap.getKey()); // nếu cần dùng id
                                topStories.add(story);
                            }
                        }

                        // Firebase trả từ thấp → cao → phải đảo ngược
                        Collections.reverse(topStories);

                        // Gán vào adapter
                        bestStoryAdapter.setData(topStories);
                        bestStoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }



//    @Override
//    protected void onRoleLoaded(String role) {
//        if ("reader".equals(role) || "author".equals(role)) {
//            startActivity(new Intent(this, MainActivity.class));
//            finishAffinity();
//        }
//    }
}