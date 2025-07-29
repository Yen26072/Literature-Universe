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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    private Context context;
    private List<Story> storyList;

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

        // Ngày cập nhật
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(story.getUpdatedAt()));
        holder.tvUpdatedAt.setText("Cập nhật: " + date);

        // Chương mới nhất
        if (story.getLatestChapter() != null) {
            holder.tvLatestChapter.setText("Chương mới: " + story.getLatestChapter().getTitle());
        } else {
            holder.tvLatestChapter.setText("Chưa có chương");
        }

        holder.tvEyes.setText(String.valueOf(story.getViewsCount()));
        holder.tvLike.setText(String.valueOf(story.getLikesCount()));
        holder.tvComments.setText(String.valueOf(story.getCommentsCount()));

        // Ảnh bìa
        Glide.with(context).load(story.getCoverUrl()).into(holder.imgCover);

        // Nếu truyện bị xóa thì hiển thị cảnh báo, vẫn cho nhấn
        if (story.isDeleted()) {
            holder.tvWarning.setVisibility(View.VISIBLE);
            holder.tvWarning.setText("⚠ Truyện đã bị xóa");
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.tvWarning.setVisibility(View.GONE);
            holder.itemView.setAlpha(1f);
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
        TextView tvTitle, tvLatestChapter, tvUpdatedAt, tvStatus, tvWarning, tvEyes, tvLike, tvComments;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLatestChapter = itemView.findViewById(R.id.tvLatestChapter);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvWarning = itemView.findViewById(R.id.tvWarning); // cảnh báo bị xóa
            tvEyes = itemView.findViewById(R.id.txtEyes);
            tvLike = itemView.findViewById(R.id.txtLike);
            tvComments = itemView.findViewById(R.id.txtComments);
        }
    }
}
