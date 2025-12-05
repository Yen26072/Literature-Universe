package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.AppRule;
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.Notification;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailCommentReport extends BaseActivity {
    private ImageView imgReporter, imgCommentAuthor;
    private TextView tvReporter, tvReason, tvTime, tvCommentAuthor, tvCommentContent;
    private Button btnConfirmViolation, btnConfirmNoViolation, btnRejected, btnAccepted;

    private String reportId;
    private Boolean isAppeal;
    private CommentReport report;

    private DatabaseReference reportsRef, usersRef, commentsRef;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_comment_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        imgReporter = findViewById(R.id.imgReporter);
        imgCommentAuthor = findViewById(R.id.imgCommentAuthor);
        tvReporter = findViewById(R.id.tvReporter);
        tvReason = findViewById(R.id.tvReason);
        tvTime = findViewById(R.id.tvTime);
        tvCommentAuthor = findViewById(R.id.tvCommentAuthor);
        tvCommentContent = findViewById(R.id.tvCommentContent);
        btnConfirmViolation = findViewById(R.id.btnConfirmViolation);
        btnConfirmNoViolation = findViewById(R.id.btnConfirmNoViolation);
        btnRejected = findViewById(R.id.btnRejected);
        btnAccepted = findViewById(R.id.btnAccepted);

        reportId = getIntent().getStringExtra("reportId");
        isAppeal = getIntent().getBooleanExtra("isAppeal", false);

        if(isAppeal){
            btnAccepted.setVisibility(View.VISIBLE);
            btnRejected.setVisibility(View.VISIBLE);
            btnConfirmViolation.setVisibility(View.GONE);
            btnConfirmNoViolation.setVisibility(View.GONE);
        }
        else {
            btnAccepted.setVisibility(View.GONE);
            btnRejected.setVisibility(View.GONE);
            btnConfirmViolation.setVisibility(View.VISIBLE);
            btnConfirmNoViolation.setVisibility(View.VISIBLE);
        }
        // Firebase refs
        reportsRef = FirebaseDatabase.getInstance().getReference("commentReports");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        commentsRef = FirebaseDatabase.getInstance().getReference("comments");

        // Lấy reportId từ intent
        if (reportId == null) {
            Toast.makeText(this, "Không có reportId!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadReportData();

        btnConfirmViolation.setOnClickListener(v -> confirmViolation());
        btnConfirmNoViolation.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận không vi phạm")
                    .setMessage("Bạn chắc chắn đánh dấu báo cáo này là KHÔNG VI PHẠM?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> rejectReport())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        btnAccepted.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Chấp nhận khiếu nại?")
                    .setMessage("Nếu chấp nhận thì hệ thống sẽ hủy phạt người dùng.")
                    .setPositiveButton("Đồng ý", (dialog, which) -> acceptAppeal())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        btnRejected.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Từ chối khiếu nại?")
                    .setMessage("Khiếu nại sẽ bị từ chối và giữ nguyên phạt.")
                    .setPositiveButton("Đồng ý", (dialog, which) -> rejectAppeal())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
    private void rejectAppeal(){
        long now = System.currentTimeMillis();
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reportRef = FirebaseDatabase.getInstance()
                .getReference("commentReports")
                .child(reportId);

        DatabaseReference appealRef = reportRef.child("appeal");

        // Lấy thông tin report để biết violationId & punishment
        reportRef.get().addOnSuccessListener(snapshot -> {

            CommentReport report = snapshot.getValue(CommentReport.class);
            if (report == null) return;

            String punishedUserId = report.getCommentOwnerId();  // người bị xử phạt
            String violationId = report.getViolationId();
            String commentId = report.getCommentId();
            String storyId = report.getStoryId();

            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(punishedUserId);

            // 1) GỠ PHẠT USER
            userRef.get().addOnSuccessListener(userSnap -> {

                User user = userSnap.getValue(User.class);
                if (user == null) return;

                // 4) CẬP NHẬT REPORT
                reportRef.child("status").setValue("rejected");

                // 5) CẬP NHẬT APPEAL
                Map<String, Object> appealUpdate = new HashMap<>();
                appealUpdate.put("appealStatus", "rejected");
                appealUpdate.put("appealDecisionTime", now);
                appealUpdate.put("appealAdminId", adminId);

                appealRef.updateChildren(appealUpdate);
                String reportType = "comment";
                String message = "Đã TỪ CHỐI khiếu nại – Giữ nguyên toàn bộ hình phạt!";

                // 6) Gửi thông báo kết quả khiếu nại
                sendPunishNotification(punishedUserId, reportId, commentId, storyId, reportType, message);

                Toast.makeText(this,
                        "Đã TỪ CHỐI khiếu nại – Giữ nguyên toàn bộ hình phạt!",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            });
        });
    }

    private void acceptAppeal() {
        long now = System.currentTimeMillis();
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reportRef = FirebaseDatabase.getInstance()
                .getReference("commentReports")
                .child(reportId);

        DatabaseReference appealRef = reportRef.child("appeal");

        // Lấy thông tin report để biết violationId & punishment
        reportRef.get().addOnSuccessListener(snapshot -> {

            CommentReport report = snapshot.getValue(CommentReport.class);
            if (report == null) return;

            String punishedUserId = report.getCommentOwnerId();  // người bị xử phạt
            String violationId = report.getViolationId();
            String commentId = report.getCommentId();
            String storyId = report.getStoryId();

            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(punishedUserId);

            // 1) GỠ PHẠT USER
            userRef.get().addOnSuccessListener(userSnap -> {

                User user = userSnap.getValue(User.class);
                if (user == null) return;

                Map<String, Object> userUpdate = new HashMap<>();

                // Gỡ mute bình luận
                userUpdate.put("muted", false);
                userUpdate.put("muteUntil", 0);

                // Gỡ ban đăng truyện
                userUpdate.put("canPost", true);
                userUpdate.put("postBanUntil", 0);

                // Giảm số lần vi phạm (không để âm)
                int vc = user.getViolationCount() - 1;
                userUpdate.put("violationCount", Math.max(vc, 0));

                userRef.updateChildren(userUpdate);

                // 2) KHÔI PHỤC COMMENT (nếu đã bị deleted)
                FirebaseDatabase.getInstance()
                        .getReference("comments")
                        .child(commentId)
                        .child("deleted")
                        .setValue(false);

                // 4) CẬP NHẬT REPORT → reverted (đã hủy phạt)
                Map<String, Object> reportUpdate = new HashMap<>();
                reportUpdate.put("status", "reverted");
                reportUpdate.put("punishment", null);
                reportUpdate.put("violationId", null);
                reportUpdate.put("adminId", adminId);
                reportUpdate.put("adminDecisionTime", now);

                reportRef.updateChildren(reportUpdate);

                // 5) CẬP NHẬT APPEAL
                Map<String, Object> appealUpdate = new HashMap<>();
                appealUpdate.put("appealStatus", "accepted");
                appealUpdate.put("appealDecisionTime", now);
                appealUpdate.put("appealAdminId", adminId);

                appealRef.updateChildren(appealUpdate);
                String reportType = "comment";
                String message = "Đã CHẤP NHẬN khiếu nại – Hủy toàn bộ hình phạt!";

                // 6) Gửi thông báo kết quả khiếu nại
                sendPunishNotification(punishedUserId, reportId, commentId, storyId, reportType, message);

                Toast.makeText(this,
                        "Đã CHẤP NHẬN khiếu nại – Hủy toàn bộ hình phạt!",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            });
        });
    }

    private void rejectReport() {
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long now = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("adminId", adminId);
        updates.put("adminDecisionTime", now);
        updates.put("adminNote", "Không vi phạm");

        reportsRef.child(report.getReportId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {

                    // Gửi thông báo cho người gửi report
                    sendNotificationToReporter(report.getReportId(),report.getStoryId(), report.getCommentId(),
                            report.getReporterId(),
                            "Báo cáo không vi phạm",
                            "Báo cáo của bạn đã được xem xét nhưng không có dấu hiệu vi phạm."
                    );

                    Toast.makeText(this, "Đã xác nhận KHÔNG VI PHẠM", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void sendNotificationToReporter(String reportId,String storyId,String commentId, String reporterId, String title, String message) {

        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(reporterId);
        String notifId = notifRef.push().getKey();

        Notification noti = new Notification(
                notifId,
                reportId,
                reporterId,
                "comment",
                storyId,
                null,    // có thể null nếu comment ở review
                commentId,
                null,         // reviewId null
                System.currentTimeMillis(),
                false,
                message
        );

        notifRef.child(notifId).setValue(noti)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Đã gửi thông báo cho người báo cáo", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gửi thông báo thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadReportData() {
        reportsRef.child(reportId).get().addOnSuccessListener(snapshot -> {
            report = snapshot.getValue(CommentReport.class);
            if (report == null) {
                Toast.makeText(this, "Báo cáo không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            tvReason.setText(report.getReason());
            tvTime.setText(getTimeAgo(report.getTimestamp()));

            // Load reporter info
            usersRef.child(report.getReporterId()).get().addOnSuccessListener(userSnap -> {
                User reporter = userSnap.getValue(User.class);
                if (reporter != null) {
                    tvReporter.setText(reporter.getUsername());
                    Glide.with(this).load(reporter.getAvatarUrl()).circleCrop().into(imgReporter);
                }
            });

            // Load comment info
            commentsRef.child(report.getCommentId()).get().addOnSuccessListener(commentSnap -> {
                if (commentSnap.exists()) {
                    String commentContent = commentSnap.child("content").getValue(String.class);
                    String userId = commentSnap.child("userId").getValue(String.class);

                    tvCommentContent.setText(commentContent);

                    // Load comment author
                    usersRef.child(userId).get().addOnSuccessListener(authorSnap -> {
                        User author = authorSnap.getValue(User.class);
                        if (author != null) {
                            tvCommentAuthor.setText(author.getUsername());
                            Glide.with(this).load(author.getAvatarUrl()).circleCrop().into(imgCommentAuthor);
                        }
                    });
                } else {
                    tvCommentContent.setText("Comment đã bị xóa");
                    btnConfirmViolation.setEnabled(false);
                }
            });
        });
    }

    private void confirmViolation() {
        if (report == null) return;

        // Lấy danh sách appRules từ Firebase
        FirebaseDatabase.getInstance().getReference("appRules").get()
                .addOnSuccessListener(snapshot -> {
                    List<AppRule> rules = new ArrayList<>();
                    for (var child : snapshot.getChildren()) {
                        AppRule rule = child.getValue(AppRule.class);
                        if (rule != null) rules.add(rule);
                    }

                    if (rules.isEmpty()) {
                        Toast.makeText(this, "Chưa có quy định nào để chọn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Tạo popup để chọn quy định
                    String[] ruleTitles = new String[rules.size()];
                    for (int i = 0; i < rules.size(); i++) {
                        ruleTitles[i] = rules.get(i).getTitle();
                    }

                    final int[] selectedIndex = {-1}; // lưu index quy định được chọn
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                    builder.setTitle("Chọn quy định bị vi phạm")
                            .setSingleChoiceItems(ruleTitles, -1, (dialog, which) -> selectedIndex[0] = which)
                            .setPositiveButton("Xác nhận", (dialog, which) -> {
                                if (selectedIndex[0] == -1) {
                                    Toast.makeText(this, "Vui lòng chọn một quy định", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                AppRule violatedRule = rules.get(selectedIndex[0]);
                                applyPenalty(violatedRule); // phương thức xử lý phạt user
                                dialog.dismiss();
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                            .show();
                });
    }

    private void applyPenalty(AppRule rule) {
        String punishedUserId = report.getCommentOwnerId();   // người bị phạt
        long now = System.currentTimeMillis();
        // 1. Đánh dấu comment là đã bị xoá (ẩn khỏi người dùng)
        commentsRef.child(report.getCommentId()).child("deleted").setValue(true);
        // 2. Phạt user commentOwner
        usersRef.child(report.getCommentOwnerId()).get().addOnSuccessListener(snapshot -> {
            User author = snapshot.getValue(User.class);
            if (author == null) return;
            Map<String, Object> updates = new HashMap<>();
            // ----------------------------
            // Xử phạt COMMENT (cấm bình luận)
            // ----------------------------
            if (rule.getActionType().equals("comment_review")) {
                long currentMuteUntil = (author.getMuteUntil() != null) ? author.getMuteUntil() : 0;
                long newMuteUntil;
                if (currentMuteUntil > now) {
                    // user còn đang bị mute → cộng dồn
                    long remaining = currentMuteUntil - now;
                    newMuteUntil = now + remaining + rule.getDurationMillis();
                } else {
                    // user chưa hoặc đã hết bị mute
                    newMuteUntil = now + rule.getDurationMillis();
                }
                updates.put("muted", true);
                updates.put("muteUntil", newMuteUntil);
            }
            // ----------------------------
            // Xử phạt STORY (cấm đăng truyện)
            // ----------------------------
            else if (rule.getActionType().equals("story")) {
                long currentPostBanUntil = (author.getPostBanUntil() != null) ? author.getPostBanUntil() : 0;
                long newPostBanUntil;
                if (currentPostBanUntil > now) {
                    // user còn đang bị ban → cộng dồn
                    long remaining = currentPostBanUntil - now;
                    newPostBanUntil = now + remaining + rule.getDurationMillis();
                } else {
                    newPostBanUntil = now + rule.getDurationMillis();
                }
                updates.put("canPost", false);
                updates.put("postBanUntil", newPostBanUntil);
            }
            // ----------------------------
            // Cập nhật số lần vi phạm
            // ----------------------------
            updates.put("violationCount", author.getViolationCount() + 1);
            // ----------------------------
            // Update thông tin xử phạt vào User
            // ----------------------------
            usersRef.child(author.getUserId()).updateChildren(updates);
        });
        // 3. Cập nhật trạng thái report
        String adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> reportUpdate = new HashMap<>();
        reportUpdate.put("status", "accepted");
        reportUpdate.put("adminId", adminId);
        reportUpdate.put("adminDecisionTime", now);
        reportUpdate.put("ruleId", rule.getRuleId());
        reportsRef.child(reportId).updateChildren(reportUpdate)
                .addOnSuccessListener(aVoid -> {

                    // Gửi thông báo cho người gửi report
                    sendNotificationToReporter(report.getReportId(),
                            report.getStoryId(),
                            report.getCommentId(),
                            report.getReporterId(),
                            "Xác nhận vi phạm",
                            "Báo cáo của bạn đã được xem xét và có dấu hiệu vi phạm."
                    );

                    Toast.makeText(this, "Đã xác nhận VI PHẠM", Toast.LENGTH_SHORT).show();
                    finish();

                    //Gửi thông báo đến người bị phạt
                    sendPunishNotification(
                            punishedUserId,
                            report.getReportId(),
                            report.getCommentId(),
                            report.getStoryId(),
                            "comment",
                            "Bạn đã bị xử phạt do vi phạm quy định của hệ thống."
                    );

                    Toast.makeText(this, "Đã xử lý vi phạm & gửi thông báo!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void sendPunishNotification(String punishedUserId, String reportId, String commentId, String storyId, String reportType, String message) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(punishedUserId);

        String notifId = notifRef.push().getKey();

        Notification noti = new Notification(
                notifId,
                reportId,
                punishedUserId,
                reportType,     // comment / review / story
                storyId,
                null,           // chapterId nếu có
                commentId,
                null,           // reviewId nếu report review
                System.currentTimeMillis(),
                false,          // chưa đọc
                message
        );

        notifRef.child(notifId).setValue(noti);
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