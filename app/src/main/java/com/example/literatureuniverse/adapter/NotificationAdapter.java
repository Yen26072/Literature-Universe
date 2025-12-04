package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.activity.DetailCommentReport;
import com.example.literatureuniverse.activity.DetailReport;
import com.example.literatureuniverse.model.Comment;
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.Notification;
import com.example.literatureuniverse.model.ReviewReport;
import com.example.literatureuniverse.model.StoryReport;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private Context context;
    private List<Notification> list;
    private CommentReport commentReport;
    private ReviewReport reviewReport;
    private StoryReport storyReport;
    private String userId;

    public NotificationAdapter(Context context, List<Notification> list) {
        this.context = context;
        this.list = list;
    }

    public void setData(List<Notification> newData) {
        this.list = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Notification noti = list.get(position);
        holder.txtMessage.setText(noti.getMessage());

        String time = DateFormat.format("dd/MM/yyyy • HH:mm", noti.getTimestamp()).toString();
        holder.txtTime.setText(time);

        // Màu nền nếu chưa đọc
        if (!noti.isRead()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#EEF3FF"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }


        // CLICK → mở ReportDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Boolean isAppealOK = false;
            if(noti.getMessage().equals("Bạn đã bị xử phạt do vi phạm quy định của hệ thống.") || noti.getMessage().equals("Đã TỪ CHỐI khiếu nại – Giữ nguyên toàn bộ hình phạt!") || noti.getMessage().equals("Đã CHẤP NHẬN khiếu nại – Hủy toàn bộ hình phạt!")){
                switch (noti.getReportType()){
                    case "comment":
                        loadUser(noti.getReportId(), 1, noti.getNotificationId());
                        break;
                    case "review":
                        loadUser(noti.getReportId(), 2, noti.getNotificationId());
                        break;
                    case "story":
                        loadUser(noti.getReportId(), 3, noti.getNotificationId());
                        break;
                }
                isAppealOK = true;
            }
            else  markAsRead(noti.getReporterId(), noti.getNotificationId());

            Intent intent = new Intent(context, DetailReport.class);

            intent.putExtra("reportId", noti.getReportId());
            intent.putExtra("reportedId", noti.getReporterId());
            intent.putExtra("reportType", noti.getReportType());            // comment / story / review / chapter
            intent.putExtra("storyId", noti.getStoryId());
            intent.putExtra("chapterId", noti.getChapterId());
            intent.putExtra("commentId", noti.getCommentId());
            intent.putExtra("reviewId", noti.getReviewId());
            intent.putExtra("message", noti.getMessage());
            intent.putExtra("isAppealOK", isAppealOK);

            context.startActivity(intent);
        });
    }

    private void loadUser(String reportId, int i, String notiId) {
        DatabaseReference reportRef;
        if(i == 1){
            reportRef = FirebaseDatabase.getInstance().getReference("commentReports").child(reportId);
            reportRef.get().addOnSuccessListener(comment ->{
                commentReport = comment.getValue(CommentReport.class);
                userId = commentReport.getCommentOwnerId();
                markAsRead(userId, notiId);
            });
        } else if(i == 2){
            reportRef = FirebaseDatabase.getInstance().getReference("reviewReports").child(reportId);
            reportRef.get().addOnSuccessListener(review ->{
                reviewReport = review.getValue(ReviewReport.class);
                userId = reviewReport.getReviewOwnerId();
                markAsRead(userId, notiId);
            });
        } else if(i == 3){
            reportRef = FirebaseDatabase.getInstance().getReference("storyReports").child(reportId);
            reportRef.get().addOnSuccessListener(story ->{
                storyReport = story.getValue(StoryReport.class);
                userId = storyReport.getStoryOwnerId();
                markAsRead(userId, notiId);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void markAsRead(String userId, String notifId) {
        Log.e("DEBUGADAPTER", "userId=" + userId + ", notificationId=" + notifId);
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(userId)
                .child(notifId);

        ref.child("read").setValue(true);
    }

    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtMessage, txtTime;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}

