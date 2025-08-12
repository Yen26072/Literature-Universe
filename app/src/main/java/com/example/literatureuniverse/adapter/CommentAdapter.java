package com.example.literatureuniverse.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.Comment;
import com.example.literatureuniverse.model.CommentReply;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{
    private Context context;
    private List<Comment> commentList;
    private Map<String, List<CommentReply>> replyMap; // commentId -> List<Reply>
    private HashMap<String, User> userCache;
    private HashMap<String, String> chapterTitleCache;
    private DatabaseReference userRef;

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
                        .child(chapterId)
                        .child("title");

                chapterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = snapshot.getValue(String.class);
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

                tvReplyContent.setText(reply.getContent());
                tvReplyTime.setText(formatTime(reply.getCreatedAt()));
                loadUser(reply.getUserId(), tvReplyUsername, imgReplyAvatar);

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
                holder.replyInputLayout.setVisibility(View.VISIBLE);
                holder.editTextReply.requestFocus();
            }
        });
        // ✅ XỬ LÝ SỰ KIỆN GỬI REPLY
        holder.buttonSendReply.setOnClickListener(v2 -> {
            String replyContent = holder.editTextReply.getText().toString().trim();
            if (!replyContent.isEmpty()) {
                sendReply(comment.getCommentId(), replyContent, comment.getStoryId());
                holder.editTextReply.setText(""); // xóa nội dung sau khi gửi
                holder.replyInputLayout.setVisibility(View.GONE); // ẩn khung reply
            }
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

        // Lưu reply và hiển thị
        replyRef.setValue(reply).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // ✅ Thêm vào danh sách replies trong bộ nhớ
                List<CommentReply> replies2 = replyMap.get(parentCommentId);
                if (replies2 == null) {
                    replies2 = new ArrayList<>();
                    replyMap.put(parentCommentId, replies2);
                }
                replies2.add(reply);

                // ✅ Cập nhật giao diện (refresh lại comment đang reply)
                int position = findCommentPositionById(parentCommentId);
                if (position != -1) {
                    notifyItemChanged(position);
                }
            }
        });

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
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                // Xử lý nếu cần
            }
        });
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
        TextView tvCommentContent, txtReplyButton, txtUserName, txtCommentTime, txtChapterTitle;
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
        }
    }
}
