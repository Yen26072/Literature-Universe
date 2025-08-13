package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

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

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    private Context context;
    private List<Story> storyList;
    private DatabaseReference userRef;

    public StoryAdapter(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    public void setData(List<Story> newList) {
        Log.d("AdapterDEBUG", "Set data called with size: " + newList.size());
        storyList.clear();
        storyList.addAll(newList);
        Log.d("AdapterDEBUG", "Story list size after addAll: " + storyList.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryAdapter.StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new StoryAdapter.StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.StoryViewHolder holder, int position) {
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

        holder.tvEyes.setText(String.valueOf(story.getViewsCount()));
        holder.tvLike.setText(String.valueOf(story.getLikesCount()));
        holder.tvComments.setText(String.valueOf(story.getCommentsCount()));

        // Ảnh bìa
        Glide.with(context).load(story.getCoverUrl()).into(holder.imgCover);

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

    public static class StoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCover;
        TextView tvTitle, tvAuthor, tvStatus, tvWarning, tvEyes, tvLike, tvComments;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWarning = itemView.findViewById(R.id.tvWarning); // cảnh báo bị xóa
            tvEyes = itemView.findViewById(R.id.txtEyes);
            tvLike = itemView.findViewById(R.id.txtLike);
            tvComments = itemView.findViewById(R.id.txtComments);
        }
    }
}
