package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.CommentNotification;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentNotificationAdapter extends RecyclerView.Adapter <CommentNotificationAdapter.CommentNotificationViewHolder>{
    private Context context;
    private List<CommentNotification> notificationList;
    private Map<String, User> userCache = new HashMap<>();

    public interface OnNotificationClickListener {
        void onNotificationClick(String storyId, String commentId);
    }

    private OnNotificationClickListener listener;

    public CommentNotificationAdapter(Context context, List<CommentNotification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentNotificationAdapter.CommentNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_notifications, parent, false);
        return new CommentNotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentNotificationAdapter.CommentNotificationViewHolder holder, int position) {
        CommentNotification notification = notificationList.get(position);

        // Load user info (avatar + name)
        loadUser(notification.getFromUserId(), holder, notification.getMessage());

        // Nội dung thông báo
        holder.tvContent.setText(notification.getMessage());

        // Thời gian
        holder.tvTime.setText(getTimeAgo(notification.getCreatedAt()));

        // Hiển thị chấm xanh nếu chưa đọc
        holder.imgUnread.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);


        // Click
        holder.itemView.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference commentnotificationsRef = FirebaseDatabase.getInstance().getReference("commentnotifications").child(currentUserId).child(notification.getNotificationId());
            if (listener != null) {
                listener.onNotificationClick(notification.getStoryId(), notification.getCommentId());
                notification.setRead(true);
                notifyItemChanged(position); // update lại dấu chấm
                commentnotificationsRef.child("read").setValue(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class CommentNotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgUnread;
        TextView tvContent, tvTime;

        public CommentNotificationViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgUnread = itemView.findViewById(R.id.imgUnread);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
    // Load user info
    private void loadUser(String userId, CommentNotificationViewHolder holder, String message) {
        if (userCache.containsKey(userId)) {
            User u = userCache.get(userId);
            holder.tvContent.setText(u.getUsername() + " " + message);
            Glide.with(context).load(u.getAvatarUrl()).into(holder.imgAvatar);
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User u = snapshot.getValue(User.class);
                    if (u != null) {
                        userCache.put(userId, u);
                        holder.tvContent.setText(u.getUsername() + " " + message);
                        Glide.with(context).load(u.getAvatarUrl()).into(holder.imgAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // Format thời gian: 2 giờ trước, 3 ngày trước
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
