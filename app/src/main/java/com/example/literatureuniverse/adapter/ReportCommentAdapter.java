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
import com.example.literatureuniverse.model.CommentReport;
import com.example.literatureuniverse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportCommentAdapter extends RecyclerView.Adapter <ReportCommentAdapter.ReportCommentViewHolder>{
    private Context context;
    private List<CommentReport> commentReportList;
    private Map<String, User> userCache = new HashMap<>();

    public interface OnReportClickListener {
        void onReportClick(CommentReport report);
    }

    private OnReportClickListener listener;

    public ReportCommentAdapter(Context context, List<CommentReport> list, OnReportClickListener listener) {
        this.context = context;
        this.commentReportList = list;
        this.listener = listener;
    }

    public ReportCommentAdapter(Context context, List<CommentReport> commentReportList) {
        this.context = context;
        this.commentReportList = commentReportList;
    }

    public void setData(List<CommentReport> newList) {
        commentReportList.clear();
        commentReportList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportCommentAdapter.ReportCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_review_report, parent, false);
        return new ReportCommentAdapter.ReportCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportCommentAdapter.ReportCommentViewHolder holder, int position) {
        CommentReport commentReport = commentReportList.get(position);

        // Load user info (avatar + name)
        loadUser(commentReport.getReporterId(), holder);

        // Thời gian
        holder.tvTime.setText(getTimeAgo(commentReport.getTimestamp()));

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("DebugClick", commentReport.getReportId());
//                Intent intent = new Intent(v.getContext(), DetailCommentReport.class);
//                intent.putExtra("reportId", commentReport.getReportId());
//                v.getContext().startActivity(intent);
//            }
//        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReportClick(commentReport);
        });
    }

    private void loadUser(String userId, ReportCommentViewHolder holder) {
        if (userCache.containsKey(userId)) {
            User u = userCache.get(userId);
            holder.tvContent.setText(u.getUsername() + " " + "đã báo cáo 1 bình luận");
            Glide.with(context).load(u.getAvatarUrl()).into(holder.imgAvatar);
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User u = snapshot.getValue(User.class);
                    if (u != null) {
                        userCache.put(userId, u);
                        holder.tvContent.setText(u.getUsername() + " " + "đã báo cáo 1 bình luận");
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

    @Override
    public int getItemCount() {
        return commentReportList.size();
    }

    public class ReportCommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvContent, tvTime;
        public ReportCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
