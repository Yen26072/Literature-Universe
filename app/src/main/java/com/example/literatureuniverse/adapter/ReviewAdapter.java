package com.example.literatureuniverse.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.activity.HomeStory;
import com.example.literatureuniverse.activity.Login;
import com.example.literatureuniverse.model.Appeal;
import com.example.literatureuniverse.model.Comment;
import com.example.literatureuniverse.model.CommentNotification;
import com.example.literatureuniverse.model.CommentReply;
import com.example.literatureuniverse.model.Review;
import com.example.literatureuniverse.model.ReviewReport;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ReviewAdapter  extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Review> reviewList;
    private HashMap<String, User> userCache;
    private DatabaseReference userRef;
    private String highlightedReviewId;
    private HashMap<String, Story> storyCache = new HashMap<>();
    private HashMap<String, String> authorCache = new HashMap<>();


    public ReviewAdapter(Context context, List<Review> reviewList, HashMap<String, User> userCache) {
        this.context = context;
        this.reviewList = reviewList;
        this.userCache = new HashMap<>();
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void highlightReview(String reviewId) {
        highlightedReviewId = reviewId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        String storyId = review.getStoryId();

        // Nếu đã có sẵn trong cache → dùng luôn
        if (storyCache.containsKey(storyId)) {
            Story story = storyCache.get(storyId);
            holder.txtStoryName.setText(story.getTitle());

            String authorId = story.getAuthorId();
            if (authorCache.containsKey(authorId)) {
                holder.txtAuthorName.setText(authorCache.get(authorId));
            } else {
                loadAuthor(authorId, holder);
            }
        } else {
            loadStory(storyId, holder);
        }

        holder.tvCommentContent.setText(review.getContent());
        holder.txtCommentTime.setText(formatTime(review.getCreatedAt()));
        loadUser(review.getUserId(), holder.txtUserName, holder.imgAvatarComment);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUid != null && currentUid.equals(review.getUserId())) {
            holder.txtReportButton.setVisibility(View.GONE);
        } else {
            holder.txtReportButton.setVisibility(View.VISIBLE);
        }

        // Nếu là comment cần highlight
        if (highlightedReviewId != null && highlightedReviewId.equals(review.getReviewId())) {
            holder.itemView.setBackgroundColor(Color.YELLOW);

            int adapterPos = holder.getAdapterPosition(); // cache tại thời điểm bind
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                highlightedReviewId = null;
                if (adapterPos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(adapterPos);
                }
            }, 2000);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.txtReportButton.setOnClickListener(v -> {
            // Nếu chưa đăng nhập → chuyển Login
            if (currentUid == null) {
                Intent intent = new Intent(context, Login.class);
                context.startActivity(intent);
                return;
            }

            // Nếu đăng nhập rồi → mở popup báo cáo
            showReportDialog(review);
        });

    }

    //Tìm vị trí comment theo ID
    private int findCommentPositionById(String commentId) {
        for (int i = 0; i < reviewList.size(); i++) {
            if (reviewList.get(i).getReviewId().equals(commentId)) {
                return i;
            }
        }
        return -1;
    }

    public void setUserMap(HashMap<String, User> userMap) {
        this.userCache = userMap;
    }

    public String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public void loadUser(String userId, TextView tvName, ImageView avatarView) {
        if (userCache == null) userCache = new HashMap<>();  // ✅ tránh null
        if (userCache.containsKey(userId)) {
            User user = userCache.get(userId);
            tvName.setText(user.getUsername());
            Glide.with(context)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(avatarView);
        } else {
            // Tải từ Firebase nếu chưa có và cache lại
            userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userCache.put(userId, user);
                        tvName.setText(user.getUsername());
                        Glide.with(context)
                                .load(user.getAvatarUrl())
                                .placeholder(R.drawable.ic_launcher_background)
                                .circleCrop()
                                .into(avatarView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void loadStory(String storyId, ReviewViewHolder holder) {
        DatabaseReference storyRef = FirebaseDatabase.getInstance()
                .getReference("stories")
                .child(storyId);

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Story story = snapshot.getValue(Story.class);
                storyCache.put(storyId, story);

                holder.txtStoryName.setText(story.getTitle());
                Glide.with(context).load(story.getCoverUrl()).into(holder.imgCover3);

                // lấy tác giả
                loadAuthor(story.getAuthorId(), holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAuthor(String authorId, ReviewViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(authorId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String authorName = snapshot.child("name").getValue(String.class);
                authorCache.put(authorId, authorName);

                holder.txtAuthorName.setText(authorName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void showReportDialog(Review review) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_report_review, null);
        builder.setView(view);

        EditText edtContent = view.findViewById(R.id.edtReportContent);
        Button btnSend = view.findViewById(R.id.btnSendReport);
        Button btnCancelReport = view.findViewById(R.id.btnCancelReport);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnSend.setOnClickListener(v -> {
            String content = edtContent.getText().toString().trim();
            if (content.isEmpty()) {
                edtContent.setError("Vui lòng nhập nội dung!");
                return;
            }

            sendReportToFirebase(review, content, dialog);
        });

        btnCancelReport.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void sendReportToFirebase(Review review, String content, AlertDialog dialog) {

        String reporterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String reportId = FirebaseDatabase.getInstance().getReference("reviewReports").push().getKey();
        long now = System.currentTimeMillis();

        // Tạo đối tượng Appeal mặc định
        Appeal appeal = new Appeal();
        appeal.setHasAppeal(false);
        appeal.setAppealReason("");
        appeal.setAppealTime(0);
        appeal.setAppealStatus("none");
        appeal.setAppealAdminId("");
        appeal.setAppealDecisionTime(0);

        // Tạo object ReviewReport
        ReviewReport report = new ReviewReport(
                reportId,
                review.getReviewId(),
                review.getStoryId(),
                reporterId,
                review.getUserId(),
                content,
                now,
                "pending",     // status
                "",            // adminId
                0,             // adminDecisionTime
                "",            // adminNote
                "",            // punishment
                "",            // violationId
                appeal
        );

        // Lưu lên Firebase
        FirebaseDatabase.getInstance().getReference("reviewReports")
                .child(reportId)
                .setValue(report)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Đã gửi báo cáo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void setData(List<Review> reviews) {
        this.reviewList = reviews;
        notifyDataSetChanged();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentContent, txtUserName, txtCommentTime, txtReportButton, txtStoryName, txtAuthorName;
        LinearLayout repliesContainer;
        ImageView imgAvatarComment, imgCover3;
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentContent = itemView.findViewById(R.id.txtCommentContent);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtCommentTime = itemView.findViewById(R.id.txtCommentTime);
            repliesContainer = itemView.findViewById(R.id.layoutReplies);
            imgAvatarComment = itemView.findViewById(R.id.imageView6);
            txtReportButton = itemView.findViewById(R.id.txtReportButton);
            txtStoryName = itemView.findViewById(R.id.txtStoryName);
            txtAuthorName = itemView.findViewById(R.id.txtAuthorName);
            imgCover3 = itemView.findViewById(R.id.imgCover3);
        }
    }
}
