package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.activity.HomeStory;
import com.example.literatureuniverse.model.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FollowStoryAdapter extends RecyclerView.Adapter<FollowStoryAdapter.FollowStoryViewHolder>{
    private Context context;
    private List<Story> storyList;
    private DatabaseReference userRef;

    public FollowStoryAdapter(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    public void setData(List<Story> newList) {
        storyList.clear();
        storyList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FollowStoryAdapter.FollowStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follow_story, parent, false);
        return new FollowStoryAdapter.FollowStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowStoryAdapter.FollowStoryViewHolder holder, int position) {
        Story story = storyList.get(position);

        holder.tvTitle.setText(story.getTitle());
        holder.tvStatus.setText("Trạng thái: " + story.getStatus());

        Log.d("TagStoryDEBUG", "Binding: " + story.getTitle());

        userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("userId").equalTo(story.getAuthorId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String authorName = userSnap.child("username").getValue(String.class);
                            holder.tvAuthor.setText(authorName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Ảnh bìa
        Glide.with(context).load(story.getCoverUrl()).into(holder.imgCover);

        // Ngày cập nhật
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(story.getUpdatedAt()));
        holder.tvUpdate.setText("Cập nhật: " + date);

        String lastestChapterId=story.getLatestChapter().getTitle();
        holder.tvNewChapter.setText("Chương mới: " + lastestChapterId);

        // Nếu truyện bị xóa thì hiển thị cảnh báo
        if (story.isDeleted()) {
            holder.tvWarning.setVisibility(View.VISIBLE);
            holder.tvWarning.setText("⚠ Truyện đã bị xóa");
            holder.itemView.setAlpha(0.6f);
            holder.itemView.setEnabled(false); // Không cho click
        } else {
            holder.tvWarning.setVisibility(View.GONE);
            holder.itemView.setAlpha(1f);
            holder.itemView.setEnabled(true); // Cho click lại
        }

        // Nhấn vào để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HomeStory.class);
            intent.putExtra("storyId", story.getStoryId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        Log.d("AdapterDEBUG", "Item count: " + storyList.size());
        return storyList.size();
    }

    public static class FollowStoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvAuthor, tvStatus, tvWarning, tvNewChapter, tvUpdate;

        public FollowStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWarning = itemView.findViewById(R.id.tvWarning); // cảnh báo bị xóa
            tvNewChapter = itemView.findViewById(R.id.tvNewChapter);
            tvUpdate = itemView.findViewById(R.id.tvUpdate);
        }
    }
}
