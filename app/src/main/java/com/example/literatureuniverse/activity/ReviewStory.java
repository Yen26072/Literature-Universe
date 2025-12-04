package com.example.literatureuniverse.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.CommentAdapter;
import com.example.literatureuniverse.adapter.ReviewAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Comment;
import com.example.literatureuniverse.model.CommentReply;
import com.example.literatureuniverse.model.Review;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewStory extends BaseActivity {
    ImageView imgAvatarReview;
    EditText edtReview;
    Button btnSendReview;
    String storyId, commentnotificationId;
    RecyclerView recyclerReview;
    HorizontalScrollView tabScrollReview;
    LinearLayout tabContainerReview;
    private int currentCommentPage = 1;
    private int totalCommentPages = 1;
    private int commentsPerPage = 3;
    private NestedScrollView nestedScroll;
    private FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserId = null;
    DatabaseReference reviewRef, userRef2, replyRef, userRef;

    private List<Review> reviewList = new ArrayList<>();
    private List<Review> allReviews = new ArrayList<>();
    private List<TextView> allReviewTabs = new ArrayList<>();
    private ReviewAdapter reviewAdapter;
    private HashMap<String, User> userMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        imgAvatarReview = findViewById(R.id.imgAvatarEvaluate);
        edtReview = findViewById(R.id.edtEvaluate);
        btnSendReview = findViewById(R.id.btnSendEvaluate);
        recyclerReview = findViewById(R.id.recyclerEvaluate);
        tabScrollReview = findViewById(R.id.tabScrollEvaluate);
        tabContainerReview = findViewById(R.id.tabContainerEvaluate);
        nestedScroll = findViewById(R.id.main);

        recyclerReview.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, new ArrayList<>(), userMap);
        recyclerReview.setAdapter(reviewAdapter);


        storyId = getIntent().getStringExtra("storyId");
        commentnotificationId = getIntent().getStringExtra("commentnotificationId");
        if(firebaseUser != null){
            currentUserId = firebaseUser.getUid();
        }

        if(currentUserId != null){
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        }

        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");
        userRef2 = FirebaseDatabase.getInstance().getReference("users");
        replyRef = FirebaseDatabase.getInstance().getReference("commentReplies");

        btnSendReview.setOnClickListener(v -> {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean isMuted = snapshot.child("muted").getValue(Boolean.class);
                        Long muteUntil = snapshot.child("muteUntil").getValue(Long.class);
                        long now = System.currentTimeMillis();
                        if (Boolean.TRUE.equals(isMuted) && muteUntil != null && muteUntil > now) {
                            Toast.makeText(ReviewStory.this, "Bạn đang bị chặn bình luận", Toast.LENGTH_LONG).show();
                        } else{
                            String content = edtReview.getText().toString().trim();

                            if (content.isEmpty()) {
                                Toast.makeText(ReviewStory.this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String reviewId = reviewRef.push().getKey();
                            Review review = new Review(reviewId, currentUserId, storyId, content, System.currentTimeMillis(), false, null, null, 0, false, null);

                            reviewRef.child(reviewId).setValue(review)
                                    .addOnSuccessListener(aVoid -> {
                                        // ✅ Thêm review mới vào list gốc rồi cập nhật phân trang
                                        recyclerReview.post(() -> {
                                            allReviews.add(0, review);
                                            totalCommentPages = (int) Math.ceil((double) allReviews.size() / commentsPerPage);

                                            // Nếu đang ở trang 1 thì hiển thị ngay
                                            if (currentCommentPage == 1) {
                                                displayCurrentCommentPage();
                                                recyclerReview.scrollToPosition(0);
                                            } else {
                                                // Nếu không ở trang 1 thì có thể load lại tab hoặc thông báo
                                                updateCommentPagination();
                                            }
                                        });

                                        edtReview.setText("");
                                        Toast.makeText(ReviewStory.this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ReviewStory.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ReviewStory.this, "Lỗi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
                }
            });

        });

        loadReview();
    }

    private void loadReview() {
        reviewRef.orderByChild("createdAt")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Review c = snap.getValue(Review.class);
                            if (c != null && !c.isDeleted() && c.getStoryId().equals(storyId)) {
                                reviewList.add(0, c);
                            }
                        }
                        allReviews.clear();
                        allReviews.addAll(reviewList);
                        currentCommentPage = 1;
                        updateCommentPagination();
                        if(commentnotificationId!=null){
                            scrollToComment();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateCommentPagination() {
        totalCommentPages = (int) Math.ceil((double) allReviews.size() / commentsPerPage);
        tabContainerReview.removeAllViews();
        allReviewTabs.clear();

        if (allReviews.isEmpty()) {
            tabScrollReview.setVisibility(View.GONE); // không có review thì ẩn tab
            reviewAdapter.setData(new ArrayList<>()); // clear RV
            return;
        }
        else {
            tabScrollReview.setVisibility(View.VISIBLE); // ✅ luôn hiện
        }

        for (int i = 1; i <= totalCommentPages; i++) {
            final int pageNum = i;
            TextView tab = createCommentTabTextView(pageNum);
            tabContainerReview.addView(tab);
            allReviewTabs.add(tab);
        }

        displayCurrentCommentPage();
    }

    private TextView createCommentTabTextView(int pageNum) {
        TextView tab = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 2, 16, 2);
        tab.setLayoutParams(params);
        tab.setText(String.valueOf(pageNum));
        tab.setTextSize(16);
        tab.setPadding(40, 20, 40, 20);

        if (pageNum == currentCommentPage) {
            tab.setTextColor(getResources().getColor(R.color.white));
            tab.setBackgroundResource(R.drawable.page_selected_bg);
        } else {
            tab.setTextColor(getResources().getColor(R.color.black));
            tab.setBackgroundResource(R.drawable.page_unselected_bg);
        }

        tab.setOnClickListener(v -> {
            currentCommentPage = pageNum;
            updateCommentTabStyles();
            displayCurrentCommentPage();
        });

        return tab;
    }

    private void updateCommentTabStyles() {
        for (TextView tab : allReviewTabs) {
            int tabNum = Integer.parseInt(tab.getText().toString());
            if (tabNum == currentCommentPage) {
                tab.setTextColor(getResources().getColor(R.color.white));
                tab.setBackgroundResource(R.drawable.page_selected_bg);
            } else {
                tab.setTextColor(getResources().getColor(R.color.black));
                tab.setBackgroundResource(R.drawable.page_unselected_bg);
            }
        }
    }

    private void displayCurrentCommentPage() {
        int start = (currentCommentPage - 1) * commentsPerPage;
        int end = Math.min(start + commentsPerPage, allReviews.size());

        List<Review> subList = allReviews.subList(start, end);
        Log.d("DEBUG", "allReviews size=" + allReviews.size());
        Log.d("DEBUG", "subList size=" + subList.size());
        reviewAdapter.setData(subList);
        recyclerReview.scrollToPosition(0);
    }

    private void scrollToComment() {
        int globalIndex = -1;
        for (int i = 0; i < allReviews.size(); i++) {
            if (allReviews.get(i).getReviewId().equals(commentnotificationId)) {
                globalIndex = i;
                break;
            }
        }

        if (globalIndex != -1) {
            // Tính trang chứa comment đó
            int targetPage = (globalIndex / commentsPerPage) + 1;
            currentCommentPage = targetPage;
            // Chuyển tab & render lại page
            updateCommentTabStyles();
            displayCurrentCommentPage();

            // Sau khi load lại subList thì tìm index trong subList
            int start = (currentCommentPage - 1) * commentsPerPage;
            int localIndex = globalIndex - start;

            // Đợi layout xong rồi mới cuộn cha (NestedScrollView/ScrollView)
            recyclerReview.postDelayed(() -> {
                // thử lấy ViewHolder của item trong page
                RecyclerView.ViewHolder vh = recyclerReview.findViewHolderForAdapterPosition(localIndex);
                if (vh == null) {
                    // ép RV bind item trước
                    recyclerReview.scrollToPosition(localIndex);
                    recyclerReview.postDelayed(() -> {
                        RecyclerView.ViewHolder vh2 = recyclerReview.findViewHolderForAdapterPosition(localIndex);
                        if (vh2 != null) {
                            smoothScrollParentToChild(vh2.itemView);
                        }
                        // highlight
                        reviewAdapter.highlightReview(commentnotificationId);
                        commentnotificationId = null;
                    }, 50);
                } else {
                    smoothScrollParentToChild(vh.itemView);
                    reviewAdapter.highlightReview(commentnotificationId);
                    commentnotificationId = null;
                }
            }, 50);
        }
    }

    private void smoothScrollParentToChild(View child) {
        // vị trí tuyệt đối theo cha cuộn
        int y = (int) (recyclerReview.getTop() + child.getTop());
        if (nestedScroll != null) {
            nestedScroll.smoothScrollTo(0, y);
        }
    }

}