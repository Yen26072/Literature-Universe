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
import com.example.literatureuniverse.model.StoryReport;
import com.example.literatureuniverse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppealStoryAdapter extends RecyclerView.Adapter <AppealStoryAdapter.AppealStoryViewHolder>{
    private Context context;
    private List<StoryReport> storyReportList;
    private Map<String, User> userCache = new HashMap<>();

    public interface OnReportClickListener {
        void onReportClick(StoryReport report);
    }

    private AppealStoryAdapter.OnReportClickListener listener;

    public AppealStoryAdapter(Context context, List<StoryReport> list, AppealStoryAdapter.OnReportClickListener listener) {
        this.context = context;
        this.storyReportList = list;
        this.listener = listener;
    }

    public AppealStoryAdapter(Context context, List<StoryReport> storyReportList) {
        this.context = context;
        this.storyReportList = storyReportList;
    }

    public void setData(List<StoryReport> newList) {
        storyReportList.clear();
        storyReportList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppealStoryAdapter.AppealStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_review_report, parent, false);
        return new AppealStoryAdapter.AppealStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppealStoryAdapter.AppealStoryViewHolder holder, int position) {
        StoryReport commentReport = storyReportList.get(position);

        // Load user info (avatar + name)
        loadUser(commentReport.getStoryOwnerId(), holder);

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

    private void loadUser(String userId, AppealStoryAdapter.AppealStoryViewHolder holder) {
        if (userCache.containsKey(userId)) {
            User u = userCache.get(userId);
            holder.tvContent.setText(u.getUsername() + " " + "gửi 1 khiếu nại");
            Glide.with(context).load(u.getAvatarUrl()).circleCrop().into(holder.imgAvatar);
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User u = snapshot.getValue(User.class);
                    if (u != null) {
                        userCache.put(userId, u);
                        holder.tvContent.setText(u.getUsername() + " " + "gửi 1 khiếu nại");
                        Glide.with(context).load(u.getAvatarUrl()).circleCrop().into(holder.imgAvatar);
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
        return storyReportList.size();
    }

    public class AppealStoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvContent, tvTime;
        public AppealStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
