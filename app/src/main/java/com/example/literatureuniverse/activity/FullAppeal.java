package com.example.literatureuniverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.AppealCommentAdapter;
import com.example.literatureuniverse.adapter.AppealReviewAdapter;
import com.example.literatureuniverse.adapter.AppealStoryAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.ReviewReport;
import com.example.literatureuniverse.model.StoryReport;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FullAppeal extends BaseActivity {
    RecyclerView recyclerCommentAppeal, recyclerStoryAppeal, recyclerReviewAppeal;

    AppealCommentAdapter commentAdapter;
    AppealStoryAdapter storyAdapter;
    AppealReviewAdapter reviewAdapter;

    CommentReport commentReport;
    StoryReport storyReport;
    ReviewStory reviewStory;

    List<CommentReport> commentReportList;
    List<StoryReport> storyReportList;
    List<ReviewReport> reviewReportList;

    DatabaseReference commentReportRef, storyReportRef, reviewReportRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_appeal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        recyclerCommentAppeal = findViewById(R.id.recyclerCommentAppeal);
        recyclerStoryAppeal = findViewById(R.id.recyclerStoryAppeal);
        recyclerReviewAppeal = findViewById(R.id.recyclerReviewAppeal);

        commentReportList = new ArrayList<>();
        recyclerCommentAppeal.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new AppealCommentAdapter(this, new ArrayList<>(), report -> {
            Intent intent = new Intent(FullAppeal.this, DetailCommentReport.class);
            intent.putExtra("reportId", report.getReportId());
            intent.putExtra("reporterId", report.getReporterId());
            intent.putExtra("isAppeal", true);
            startActivity(intent);
        });
        recyclerCommentAppeal.setAdapter(commentAdapter);

        storyReportList = new ArrayList<>();
        recyclerStoryAppeal.setLayoutManager(new LinearLayoutManager(this));
        storyAdapter = new AppealStoryAdapter(this, new ArrayList<>(), report -> {
            Log.d("FullAppeal", report.getReportId() + " " + report.getReporterId() + " " + report.getReporterId());
            Intent intent = new Intent(FullAppeal.this, DetailStoryReport.class);
            intent.putExtra("reportId", report.getReportId());
            intent.putExtra("reporterId", report.getReporterId());
            intent.putExtra("isAppeal", true);
            intent.putExtra("storyId", report.getStoryId());
            intent.putExtra("chapterId", report.getChapterId());
            startActivity(intent);
        });
        recyclerStoryAppeal.setAdapter(storyAdapter);

        reviewReportList = new ArrayList<>();
        recyclerReviewAppeal.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new AppealReviewAdapter(this, new ArrayList<>(), report -> {
            Log.d("FullAppeal", report.getReportId() + " " + report.getReporterId() + " " + report.getReporterId());
            Intent intent = new Intent(FullAppeal.this, DetailReviewReport.class);
            intent.putExtra("reportId", report.getReportId());
            intent.putExtra("reporterId", report.getReporterId());
            intent.putExtra("isAppeal", true);
            startActivity(intent);
        });
        recyclerReviewAppeal.setAdapter(reviewAdapter);

        commentReportRef = FirebaseDatabase.getInstance().getReference("commentReports");
        storyReportRef = FirebaseDatabase.getInstance().getReference("storyReports");
        reviewReportRef = FirebaseDatabase.getInstance().getReference("reviewReports");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCommentAppeal();
        loadStoryAppeal();
        loadReviewAppeal();
    }

    private void loadCommentAppeal() {
        commentReportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentReportList.clear();
                for (DataSnapshot sn : snapshot.getChildren()) {
                    CommentReport report = sn.getValue(CommentReport.class);
                    if (report == null) continue;

                    if ("accepted".equals(report.getStatus()) &&
                            report.getAppeal() != null &&
                            report.getAppeal().isHasAppeal() &&
                            "pending".equals(report.getAppeal().getAppealStatus())) {

                        commentReportList.add(report);
                    }
                }
                commentAdapter.setData(commentReportList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStoryAppeal() {
        storyReportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyReportList.clear();
                for (DataSnapshot sn : snapshot.getChildren()) {
                    StoryReport report = sn.getValue(StoryReport.class);
                    if (report == null) continue;

                    if (!"pending".equals(report.getStatus()) &&
                            report.getAppeal() != null &&
                            report.getAppeal().isHasAppeal() &&
                            "pending".equals(report.getAppeal().getAppealStatus())) {

                        storyReportList.add(report);
                    }
                }
                storyAdapter.setData(storyReportList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadReviewAppeal() {
        reviewReportRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewReportList.clear();
                for (DataSnapshot sn : snapshot.getChildren()) {
                    ReviewReport report = sn.getValue(ReviewReport.class);
                    if (report == null) continue;

                    if ("accepted".equals(report.getStatus()) &&
                            report.getAppeal() != null &&
                            report.getAppeal().isHasAppeal() &&
                            "pending".equals(report.getAppeal().getAppealStatus())) {

                        reviewReportList.add(report);
                    }
                }
                reviewAdapter.setData(reviewReportList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}