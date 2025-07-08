package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.Chapter;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private Context context;
    private List<Chapter> chapterList;
    private boolean isEditable;

    private OnChapterActionListener listener;

    public interface OnChapterActionListener {
        void onEdit(Chapter chapter);
        void onDelete(Chapter chapter);
    }

    public ChapterAdapter(Context context, List<Chapter> chapterList, boolean isEditable) {
        this.context = context;
        this.chapterList = chapterList;
        this.isEditable = isEditable;
    }

    public void setOnChapterActionListener(OnChapterActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Chapter> chapters) {
        this.chapterList = chapters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.txtChapterTitle.setText(chapter.getTitle());

        if (isEditable) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(chapter);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(chapter);
            });

        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chapterList != null ? chapterList.size() : 0;
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView txtChapterTitle;
        ImageView btnEdit, btnDelete;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
            btnEdit = itemView.findViewById(R.id.btnFixChapter);
            btnDelete = itemView.findViewById(R.id.btnDeleteChapter);
        }
    }
}
