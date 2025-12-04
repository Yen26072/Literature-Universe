package com.example.literatureuniverse.adapter;

import android.annotation.SuppressLint;
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
import com.example.literatureuniverse.model.CommentReply;
import com.example.literatureuniverse.model.CommentNotification;
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.Review;
import com.example.literatureuniverse.model.ReviewReport;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{
    private Context context;
    private List<Comment> commentList;
    private Map<String, List<CommentReply>> replyMap; // commentId -> List<Reply>
    private HashMap<String, User> userCache;
    private HashMap<String, String> chapterTitleCache;
    private DatabaseReference userRef;
    private String highlightedCommentId;

    public void highlightComment(String commentId) {
        highlightedCommentId = commentId;
        notifyDataSetChanged();
    }

    public CommentAdapter(Context context, List<Comment> commentList, Map<String, List<CommentReply>> replyMap, HashMap<String, User> userCache, DatabaseReference userRef) {
        this.context = context;
        this.commentList = commentList;
        this.replyMap = replyMap;
        this.userCache = userCache;
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public CommentAdapter(Context context, List<Comment> commentList, Map<String, List<CommentReply>> replyMap, HashMap<String, User> userCache) {
        this.context = context;
        this.commentList = commentList;
        this.replyMap = replyMap;
        this.userCache = new HashMap<>(); // ✅ tránh null
        this.chapterTitleCache = new HashMap<>();
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public CommentAdapter(Context context, List<Comment> commentList, Map<String, List<CommentReply>> replyMap) {
        this.context = context;
        this.commentList = commentList;
        this.replyMap = replyMap;
        this.userCache = new HashMap<>(); // ✅ tránh null
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvCommentContent.setText(comment.getContent());
        holder.txtCommentTime.setText(formatTime(comment.getCreatedAt()));
        loadUser(comment.getUserId(), holder.txtUserName, holder.imgAvatarComment);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUid != null && currentUid.equals(comment.getUserId())) {
            holder.txtReportButton.setVisibility(View.GONE);
        } else {
            holder.txtReportButton.setVisibility(View.VISIBLE);
        }

        holder.txtReportButton.setOnClickListener(v -> {
            // Nếu chưa đăng nhập → chuyển Login
            if (currentUid == null) {
                Intent intent = new Intent(context, Login.class);
                context.startActivity(intent);
                return;
            }

            // Nếu đăng nhập rồi → mở popup báo cáo
            showReportDialog(comment);
        });

        if (comment.getChapterId() != null) {
            holder.txtChapterTitle.setVisibility(View.VISIBLE);
            String chapterId = comment.getChapterId();
            String storyId = comment.getStoryId();

            if (chapterTitleCache.containsKey(chapterId)) {
                holder.txtChapterTitle.setText(chapterTitleCache.get(chapterId));
            } else {
                DatabaseReference chapterRef = FirebaseDatabase.getInstance()
                        .getReference("chapters")
                        .child(storyId)
                        .child(chapterId);

                chapterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Chương không tồn tại (đã bị xóa)
                            chapterTitleCache.put(chapterId, "-");
                            holder.txtChapterTitle.setText("-");
                            notifyItemChanged(holder.getAdapterPosition());
                            return;
                        }
                        String title = snapshot.child("title").getValue(String.class);
                        String content = snapshot.child("content").getValue(String.class);
                        if (title != null) {
                            chapterTitleCache.put(chapterId, title); // ✅ Lưu cache
                            holder.txtChapterTitle.setText(title);
                            // ✅ Gọi notifyItemChanged để các reply được cập nhật luôn tên chương
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        } else {
            holder.txtChapterTitle.setVisibility(View.GONE);
        }

        // Hiển thị replies nếu có
        holder.repliesContainer.removeAllViews();
        List<CommentReply> replies = replyMap.get(comment.getCommentId());
        if (replies != null) {
            for (CommentReply reply : replies) {
                View replyView = LayoutInflater.from(context).inflate(R.layout.item_comment_reply, holder.repliesContainer, false);
                TextView tvReplyUsername = replyView.findViewById(R.id.txtUserNameReply);
                TextView tvReplyContent = replyView.findViewById(R.id.txtCommentContentReply);
                TextView tvReplyTime = replyView.findViewById(R.id.txtCommentTimeReply);
                ImageView imgReplyAvatar = replyView.findViewById(R.id.imgAvatarReply);
                TextView txtChapterTitleReply = replyView.findViewById(R.id.txtChapterTitleReply);
                TextView txtReplyButtonReply = replyView.findViewById(R.id.txtReplyButtonReply);
                TextView txtReportButtonReply = replyView.findViewById(R.id.txtReportButtonReply);

                tvReplyContent.setText(reply.getContent());
                tvReplyTime.setText(formatTime(reply.getCreatedAt()));
                loadUser(reply.getUserId(), tvReplyUsername, imgReplyAvatar);

                // Ẩn nút Báo cáo nếu user hiện tại chính là chủ của reply
                if (currentUid != null && currentUid.equals(reply.getUserId())) {
                    txtReportButtonReply.setVisibility(View.GONE);
                } else {
                    txtReportButtonReply.setVisibility(View.VISIBLE);
                }

                // ✅ Dùng lại tên chương đã có từ comment cha
                String chapterId = comment.getChapterId();
                if (chapterId != null) {
                    txtChapterTitleReply.setVisibility(View.VISIBLE);
                    String chapterTitle = chapterTitleCache.get(chapterId);
                    if (chapterTitle != null) {
                        txtChapterTitleReply.setText("Chương: " + chapterTitle);
                    } else {
                        txtChapterTitleReply.setText("Chương: ...");
                    }
                } else {
                    txtChapterTitleReply.setVisibility(View.GONE);
                }

                txtReplyButtonReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.replyInputLayout.setVisibility(View.VISIBLE);
                        // Cuộn đến ô nhập:
                        holder.editTextReply.requestFocus();
                    }
                });

                holder.repliesContainer.addView(replyView);
            }
        }

        holder.txtReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    // Chưa đăng nhập → chuyển sang LoginActivity
                    Intent intent = new Intent(context, Login.class);
                    intent.putExtra("isStoryId", comment.getStoryId());
                    intent.putExtra("isChapterId", comment.getChapterId());
                    Log.d("CommentAdapter", "storyId = " + comment.getStoryId() + ", chapterId = " + comment.getChapterId());
                    // ✅ Truyền thêm thông tin đang ở đâu: HomeStory hay ChapterDetail
                    if (context instanceof com.example.literatureuniverse.activity.HomeStory) {
                        intent.putExtra("source", "HomeStory");
                    } else if (context instanceof com.example.literatureuniverse.activity.ChapterDetail) {
                        intent.putExtra("source", "ChapterDetail");
                    }
                    context.startActivity(intent);
                } else {
//                    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    checkMuteStatus(currentUid);  // ⬅️ auto reset nếu hết phạt
                    // Đã đăng nhập → hiện khung reply
                    holder.replyInputLayout.setVisibility(View.VISIBLE);
                    holder.editTextReply.requestFocus();
                }
            }
        });
        // ✅ XỬ LÝ SỰ KIỆN GỬI REPLY
        holder.buttonSendReply.setOnClickListener(v2 -> {
            String replyContent = holder.editTextReply.getText().toString().trim();
            if (!replyContent.isEmpty()) {
//                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                checkMuteStatus(currentUid);
                DatabaseReference userRef2 = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
                userRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Boolean isMuted = snapshot.child("muted").getValue(Boolean.class);
                            Long muteUntil = snapshot.child("muteUntil").getValue(Long.class);
                            long now = System.currentTimeMillis();
                            if (Boolean.TRUE.equals(isMuted) && muteUntil != null && muteUntil > now) {
                                Toast.makeText(context, "Bạn đang bị chặn bình luận", Toast.LENGTH_LONG).show();
                                return;
                            }
                            else{
                                sendReply(comment.getCommentId(), replyContent, comment.getStoryId());
                                holder.editTextReply.setText(""); // xóa nội dung sau khi gửi
                                holder.replyInputLayout.setVisibility(View.GONE); // ẩn khung reply
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(context, "Lỗi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Nếu là comment cần highlight
        if (highlightedCommentId != null && highlightedCommentId.equals(comment.getCommentId())) {
            holder.itemView.setBackgroundColor(Color.YELLOW);

            int adapterPos = holder.getAdapterPosition(); // cache tại thời điểm bind
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                highlightedCommentId = null;
                if (adapterPos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(adapterPos);
                }
            }, 2000);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void checkMuteStatus(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Boolean isMuted = snapshot.child("muted").getValue(Boolean.class);
                Long muteUntil = snapshot.child("muteUntil").getValue(Long.class);

                long now = System.currentTimeMillis();

                // Nếu user đang bị phạt nhưng thời gian đã hết
                if (Boolean.TRUE.equals(isMuted) && muteUntil != null && muteUntil <= now) {

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("muted", false);
                    updates.put("muteUntil", 0);

                    ref.updateChildren(updates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void sendReply(String parentCommentId, String content, String storyId) {
        DatabaseReference replyRef = FirebaseDatabase.getInstance()
                .getReference("commentReplies")
                .child(parentCommentId)
                .push();

        String replyId = replyRef.getKey();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long createdAt = System.currentTimeMillis();

        CommentReply reply = new CommentReply(
                replyId, userId, content, createdAt,
                false, null, null,
                false, null
        );

        // Lưu reply
        replyRef.setValue(reply).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Cập nhật danh sách reply trong bộ nhớ
                List<CommentReply> replies2 = replyMap.get(parentCommentId);
                if (replies2 == null) {
                    replies2 = new ArrayList<>();
                    replyMap.put(parentCommentId, replies2);
                }
                replies2.add(reply);

                // Refresh lại comment gốc
                int position = findCommentPositionById(parentCommentId);
                if (position != -1) {
                    notifyItemChanged(position);
                }
            }
        });

        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("comments")
                .child(parentCommentId);
        DatabaseReference repliesRef = FirebaseDatabase.getInstance()
                .getReference("commentReplies")
                .child(parentCommentId);
        DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("commentnotifications");

        // Lấy thông tin comment gốc + replies để xác định người cần thông báo
        commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cmtSnap) {
                Set<String> notifyUsers = new HashSet<>();

                Comment cmt = cmtSnap.getValue(Comment.class);
                if (cmt != null) notifyUsers.add(cmt.getUserId());

                // Sau đó load replies
                repliesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot rSnap : snapshot.getChildren()) {
                            CommentReply r = rSnap.getValue(CommentReply.class);
                            if (r != null) notifyUsers.add(r.getUserId());
                        }

                        // Loại bỏ người vừa reply
                        notifyUsers.remove(userId);

                        // Gửi thông báo
                        for (String uidTarget : notifyUsers) {
                            String notiId = notiRef.child(uidTarget).push().getKey();

                            CommentNotification notification = new CommentNotification(
                                    notiId,
                                    storyId,
                                    parentCommentId,
                                    replyId,
                                    userId,
                                    "đã trả lời bình luận mà bạn theo dõi",
                                    createdAt,
                                    false
                            );

                            notiRef.child(uidTarget).child(notiId).setValue(notification);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Tăng tổng số comments trong story
        DatabaseReference storyRef = FirebaseDatabase.getInstance()
                .getReference("stories")
                .child(storyId);
        storyRef.child("commentsCount").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long count = currentData.getValue(Long.class);
                currentData.setValue((count == null ? 0 : count + 1));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
        });

        // Tăng replyCount trong comment gốc
        DatabaseReference commentCountRef = FirebaseDatabase.getInstance()
                .getReference("comments")
                .child(parentCommentId)
                .child("replyCount");

        commentCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(current + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {}
        });
    }

    private void showReportDialog(Comment comment) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_report_comment, null);
        builder.setView(view);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText edtContent = view.findViewById(R.id.edtReportContent);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnSend = view.findViewById(R.id.btnSendReport);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btnCancelReport = view.findViewById(R.id.btnCancelReport);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnSend.setOnClickListener(v -> {
            String content = edtContent.getText().toString().trim();
            if (content.isEmpty()) {
                edtContent.setError("Vui lòng nhập nội dung!");
                return;
            }

            sendReportToFirebase(comment, content, dialog);
        });

        btnCancelReport.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void sendReportToFirebase(Comment comment, String content, android.app.AlertDialog dialog) {

        String reporterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String reportId = FirebaseDatabase.getInstance().getReference("commentReports").push().getKey();
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
        CommentReport report = new CommentReport(
                reportId,
                comment.getCommentId(),
                comment.getStoryId(),
                reporterId,
                comment.getUserId(),
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
        FirebaseDatabase.getInstance().getReference("commentReports")
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

    //Tìm vị trí comment theo ID
    private int findCommentPositionById(String commentId) {
        for (int i = 0; i < commentList.size(); i++) {
            if (commentList.get(i).getCommentId().equals(commentId)) {
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

    public void setData(List<Comment> comments, Map<String, List<CommentReply>> replyMap) {
        this.commentList = comments;
        this.replyMap = replyMap;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentContent, txtReplyButton, txtUserName, txtCommentTime, txtChapterTitle, txtReportButton;
        LinearLayout repliesContainer, replyInputLayout;
        ImageView imgAvatarComment;
        Button buttonSendReply;
        EditText editTextReply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentContent = itemView.findViewById(R.id.txtCommentContent);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtCommentTime = itemView.findViewById(R.id.txtCommentTime);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
            repliesContainer = itemView.findViewById(R.id.layoutReplies);
            imgAvatarComment = itemView.findViewById(R.id.imageView6);
            txtReplyButton = itemView.findViewById(R.id.txtReplyButton);
            replyInputLayout = itemView.findViewById(R.id.replyInputLayout);
            buttonSendReply = itemView.findViewById(R.id.buttonSendReply);
            editTextReply = itemView.findViewById(R.id.editTextReply);
            txtReportButton = itemView.findViewById(R.id.txtReportButton);
        }
    }
}
