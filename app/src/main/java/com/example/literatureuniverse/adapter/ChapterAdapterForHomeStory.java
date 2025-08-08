package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.activity.ChapterDetail;
import com.example.literatureuniverse.model.Chapter;

import java.util.List;

public class ChapterAdapterForHomeStory extends RecyclerView.Adapter<ChapterAdapterForHomeStory.ChapterViewHolder>{
    private Context context;
    private List<Chapter> chapterList;

    public ChapterAdapterForHomeStory(List<Chapter> chapterList, Context context) {
        this.chapterList = chapterList;
        this.context = context;
    }

    public void setData(List<Chapter> list) {
        this.chapterList.clear();          // ← Xóa dữ liệu cũ
        this.chapterList.addAll(list);    // ← Thêm dữ liệu mới
        notifyDataSetChanged();           // ← Cập nhật lại RecyclerView
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter_home_story, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.txtChapterTitle.setText(chapter.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChapterDetail.class);
            intent.putExtra("chapterTitle", chapter.getTitle());
            intent.putExtra("storyId", chapter.getStoryId());
            intent.putExtra("chapterContent", chapter.getContent());
            intent.putExtra("chapterId", chapter.getChapterId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView txtChapterTitle;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
        }
    }
}
