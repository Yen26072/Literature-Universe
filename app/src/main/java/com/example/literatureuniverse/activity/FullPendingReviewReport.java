package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
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
import com.example.literatureuniverse.adapter.ReportCommentAdapter;
import com.example.literatureuniverse.adapter.ReportReviewAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.ReviewReport;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullPendingReviewReport extends BaseActivity {
    DatabaseReference reviewReportRef;
    ReportReviewAdapter reportReviewAdapter;
    RecyclerView recyclerView;
    List<ReviewReport> reviewReportList;

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
        setContentView(R.layout.activity_full_pending_review_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        recyclerView = findViewById(R.id.recyclerReviewReport);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        reviewReportList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        reportCommentAdapter = new ReportCommentAdapter(this, new ArrayList<>());

        reportReviewAdapter = new ReportReviewAdapter(this, new ArrayList<>(), report -> {
            Intent intent = new Intent(FullPendingReviewReport.this, DetailReviewReport.class);
            intent.putExtra("reportId", report.getReportId());
            startActivity(intent);
        });
        recyclerView.setAdapter(reportReviewAdapter);
        recyclerView.setAdapter(reportReviewAdapter);

        reviewReportRef = FirebaseDatabase.getInstance().getReference("reviewReports");

//        showListPendingReviewReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showListPendingReviewReport();   // load lại danh sách mỗi lần quay lại
    }

    private void showListPendingReviewReport() {
        reviewReportRef.orderByChild("status").equalTo("pending")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        reviewReportList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ReviewReport report = ds.getValue(ReviewReport.class);
                            if (report != null) {
                                reviewReportList.add(report);
                            }
                        }

                        Collections.reverse(reviewReportList);
                        currentPage = 1;
                        updatePagination();

                        if (reviewReportList.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Không có báo cáo nào đang chờ xử lý", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) reviewReportList.size() / itemsPerPage);
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

        displayCurrentPage(); // chỉ gọi ở đây
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, reviewReportList.size());

        Log.d("MyStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<ReviewReport> subList = reviewReportList.subList(start, end);
        reportReviewAdapter.setData(subList);
    }
}