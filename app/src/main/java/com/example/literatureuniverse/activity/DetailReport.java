package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.ReviewReport;
import com.example.literatureuniverse.model.StoryReport;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DetailReport extends BaseActivity {
    TextView tvMessage, txtTitleReason, tvReason, tvTime, txtTitleStory, tvStoryName, tvTitleChapter, tvChapterContent, txtTitleComment, tvCommentAuthor;
    ImageView imgCoverStory, imgCommentAuthor;
    LinearLayout linearComment, linearStory;
    Button btnAppeal;
    String reportId, reportedId, reportType, storyId, chapterId, commentId, reviewId, message;
    Boolean isAppealOK;
    DatabaseReference commentsRef, reviewsRef, storiesRef, reportsRef, usersRef, chaptersRef;
    CommentReport commentReport;
    ReviewReport reviewReport;
    StoryReport storyReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        tvMessage = findViewById(R.id.tvMessage);
        txtTitleReason = findViewById(R.id.txtTitleReason);
        tvReason = findViewById(R.id.tvReason);
        tvTime = findViewById(R.id.tvTime);
        txtTitleStory = findViewById(R.id.txtTitleStory);
        tvStoryName = findViewById(R.id.tvStoryName);
        tvTitleChapter = findViewById(R.id.tvTitleChapter);
        tvChapterContent = findViewById(R.id.tvChapterContent);
        tvCommentAuthor = findViewById(R.id.tvCommentAuthor);
        txtTitleComment = findViewById(R.id.txtTitleComment);
        imgCoverStory = findViewById(R.id.imgCoverStory);
        imgCommentAuthor = findViewById(R.id.imgCommentAuthor);
        linearComment = findViewById(R.id.linearComment);
        linearStory = findViewById(R.id.linearStory);
        btnAppeal = findViewById(R.id.btnAppeal);

        reportId = getIntent().getStringExtra("reportId");
        reportedId = getIntent().getStringExtra("reportedId");
        reportType = getIntent().getStringExtra("reportType");
        storyId = getIntent().getStringExtra("storyId");
        chapterId = getIntent().getStringExtra("chapterId");
        commentId = getIntent().getStringExtra("commentId");
        reviewId = getIntent().getStringExtra("reviewId");
        message = getIntent().getStringExtra("message");
        isAppealOK = getIntent().getBooleanExtra("isAppealOK", false);

        if(isAppealOK){
            txtTitleReason.setVisibility(View.GONE);
            tvReason.setVisibility(View.GONE);
            tvTime.setVisibility(View.GONE);
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        if(commentId != null)
            commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(commentId);
        if(storyId != null)
            storiesRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
        if(reviewId != null)
            reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(reviewId);
        if(storyId != null && chapterId != null)
            chaptersRef = FirebaseDatabase.getInstance().getReference("chapters").child(storyId).child(chapterId);

        tvMessage.setText(message);
        if(message.equals("Bạn đã bị xử phạt do vi phạm quy định của hệ thống."))
            btnAppeal.setVisibility(View.VISIBLE);
        else btnAppeal.setVisibility(View.GONE);

        if(reportType.equals("comment"))
            loadUIComment();
        else if(reportType.equals("review"))
            loadUIReview();
        else if(reportType.equals("story"))
            loadUIStory();
    }

    private void showAppealPopup(String reportId, int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_appeal, null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText edtReason = view.findViewById(R.id.edtAppealReason);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnSend = view.findViewById(R.id.btnSendAppeal);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSend.setOnClickListener(v -> {
            String reason = edtReason.getText().toString().trim();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (reason.isEmpty()) {
                edtReason.setError("Hãy nhập lý do khiếu nại");
                return;
            }

            if(type == 1){
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("commentReports")
                        .child(reportId)
                        .child("appeal");
                HashMap<String, Object> map = new HashMap<>();
                map.put("hasAppeal", true);
                map.put("appealReason", reason);
                map.put("appealTime", System.currentTimeMillis());
                map.put("appealStatus", "pending");

                ref.setValue(map).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã gửi khiếu nại", Toast.LENGTH_SHORT).show();
                    btnAppeal.setEnabled(false);
                    btnAppeal.setAlpha(0.4f);
                    dialog.dismiss();
                });
            }
            else if(type == 2){
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("reviewReports")
                        .child(reportId)
                        .child("appeal");
                HashMap<String, Object> map = new HashMap<>();
                map.put("hasAppeal", true);
                map.put("appealReason", reason);
                map.put("appealTime", System.currentTimeMillis());
                map.put("appealStatus", "pending");

                ref.setValue(map).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã gửi khiếu nại", Toast.LENGTH_SHORT).show();
                    btnAppeal.setEnabled(false);
                    btnAppeal.setAlpha(0.4f);
                    dialog.dismiss();
                });
            }
            else if(type == 3){
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("storyReports")
                        .child(reportId)
                        .child("appeal");
                HashMap<String, Object> map = new HashMap<>();
                map.put("hasAppeal", true);
                map.put("appealReason", reason);
                map.put("appealTime", System.currentTimeMillis());
                map.put("appealStatus", "pending");

                ref.setValue(map).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã gửi khiếu nại", Toast.LENGTH_SHORT).show();
                    btnAppeal.setEnabled(false);
                    btnAppeal.setAlpha(0.4f);
                    dialog.dismiss();
                });
            }
        });
    }

    private void loadUIComment() {
        linearStory.setVisibility(View.GONE);
        linearComment.setVisibility(View.VISIBLE);
        txtTitleStory.setVisibility(View.GONE);
        tvTitleChapter.setText("Nội dung bình luận:");
        reportsRef = FirebaseDatabase.getInstance().getReference("commentReports");

        reportsRef.child(reportId).get().addOnSuccessListener(snapshot -> {
            commentReport = snapshot.getValue(CommentReport.class);
            if (commentReport == null) {
                Toast.makeText(this, "Báo cáo không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            tvReason.setText(commentReport.getReason());
            tvTime.setText(getTimeAgo(commentReport.getTimestamp()));

            // Load comment info
            commentsRef.get().addOnSuccessListener(commentSnap -> {
                if (commentSnap.exists()) {
                    String commentContent = commentSnap.child("content").getValue(String.class);
                    String userId = commentSnap.child("userId").getValue(String.class);

                    tvChapterContent.setText(commentContent);

                    // Load comment author
                    usersRef.child(userId).get().addOnSuccessListener(authorSnap -> {
                        User author = authorSnap.getValue(User.class);
                        if (author != null) {
                            tvCommentAuthor.setText(author.getUsername());
                            Glide.with(this).load(author.getAvatarUrl()).circleCrop().into(imgCommentAuthor);
                        }
                    });
                } else {
                    tvChapterContent.setText("Comment đã bị xóa");
                }
            });

            boolean hasAppeal = commentReport.getAppeal() != null && commentReport.getAppeal().isHasAppeal();
            if (hasAppeal) {
                btnAppeal.setEnabled(false);
                btnAppeal.setAlpha(0.4f);
            } else {
                btnAppeal.setEnabled(true);
                btnAppeal.setAlpha(1f);
                btnAppeal.setOnClickListener(v -> showAppealPopup(reportId, 1));
            }
        });
    }

    private void loadUIReview() {
        linearStory.setVisibility(View.GONE);
        linearComment.setVisibility(View.VISIBLE);
        txtTitleStory.setVisibility(View.GONE);
        tvTitleChapter.setText("Nội dung đánh giá:");
        reportsRef = FirebaseDatabase.getInstance().getReference("reviewReports");

        reportsRef.child(reportId).get().addOnSuccessListener(snapshot -> {
            reviewReport = snapshot.getValue(ReviewReport.class);
            if (reviewReport == null) {
                Toast.makeText(this, "Báo cáo không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            tvReason.setText(reviewReport.getReason());
            tvTime.setText(getTimeAgo(reviewReport.getTimestamp()));

            // Load comment info
            reviewsRef.get().addOnSuccessListener(commentSnap -> {
                if (commentSnap.exists()) {
                    String commentContent = commentSnap.child("content").getValue(String.class);
                    String userId = commentSnap.child("userId").getValue(String.class);

                    tvChapterContent.setText(commentContent);

                    // Load comment author
                    usersRef.child(userId).get().addOnSuccessListener(authorSnap -> {
                        User author = authorSnap.getValue(User.class);
                        if (author != null) {
                            tvCommentAuthor.setText(author.getUsername());
                            Glide.with(this).load(author.getAvatarUrl()).circleCrop().into(imgCommentAuthor);
                        }
                    });
                } else {
                    tvChapterContent.setText("Comment đã bị xóa");
                }
            });

            boolean hasAppeal = reviewReport.getAppeal() != null && reviewReport.getAppeal().isHasAppeal();
            if (hasAppeal) {
                btnAppeal.setEnabled(false);
                btnAppeal.setAlpha(0.4f);
            } else {
                btnAppeal.setEnabled(true);
                btnAppeal.setAlpha(1f);
                btnAppeal.setOnClickListener(v -> showAppealPopup(reportId, 2));
            }
        });
    }

    private void loadUIStory() {
        linearComment.setVisibility(View.GONE);
        linearStory.setVisibility(View.VISIBLE);
        txtTitleComment.setVisibility(View.GONE);
        reportsRef = FirebaseDatabase.getInstance().getReference("storyReports");

        reportsRef.child(reportId).get().addOnSuccessListener(snapshot -> {
            storyReport = snapshot.getValue(StoryReport.class);
            if (storyReport == null) {
                Toast.makeText(this, "Báo cáo không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            tvReason.setText(storyReport.getReason());
            tvTime.setText(getTimeAgo(storyReport.getTimestamp()));

            //Load story
            storiesRef.get().addOnSuccessListener(storySnap ->{
                if(storySnap.exists()){
                    String storyTitle = storySnap.child("title").getValue(String.class);
                    String storyCover = storySnap.child("coverUrl").getValue(String.class);
                    Glide.with(this).load(storyCover).into(imgCoverStory);
                    tvStoryName.setText(storyTitle);
                }
            });

            //Load chapter
            chaptersRef.get().addOnSuccessListener(chapterSnap -> {
                if(chapterSnap.exists()){
                    String chapterTitle = chapterSnap.child("title").getValue(String.class);
                    String chapterContent = chapterSnap.child("content").getValue(String.class);
                    tvTitleChapter.setText(chapterTitle);
                    tvChapterContent.setText(chapterContent);
                }
            });

            boolean hasAppeal = storyReport.getAppeal() != null && storyReport.getAppeal().isHasAppeal();
            if (hasAppeal) {
                btnAppeal.setEnabled(false);
                btnAppeal.setAlpha(0.4f);
            } else {
                btnAppeal.setEnabled(true);
                btnAppeal.setAlpha(1f);
                btnAppeal.setOnClickListener(v -> showAppealPopup(reportId, 3));
            }
        });
    }

    private String getTimeAgo(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "Vừa xong";
        else if (minutes < 60) return minutes + " phút trước";
        else if (hours < 24) return hours + " giờ trước";
        else return days + " ngày trước";
    }
}