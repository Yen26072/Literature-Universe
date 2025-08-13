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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReadingStoryAdapter  extends RecyclerView.Adapter<ReadingStoryAdapter.ReadingStoryViewHolder>{
    private Context context;
    private List<Story> storyList;
    private DatabaseReference userRef, bookmarkRef, chapterRef;

    public ReadingStoryAdapter(Context context, List<Story> storyList) {
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
    public ReadingStoryAdapter.ReadingStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reading_story, parent, false);
        return new ReadingStoryAdapter.ReadingStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingStoryAdapter.ReadingStoryViewHolder holder, int position) {
        Story story = storyList.get(position);

        holder.tvTitle.setText(story.getTitle());

        Log.d("TagStoryDEBUG", "Binding: " + story.getTitle());

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookmarkRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(userId).child(story.getStoryId());
        userRef = FirebaseDatabase.getInstance().getReference("users").child(story.getAuthorId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String authorName = snapshot.child("username").getValue(String.class);
                if (authorName != null) {
                    holder.tvAuthor.setText(authorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        bookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String chapterId = snapshot.child("chapterId").getValue(String.class);
                    if (chapterId != null && !chapterId.isEmpty()) {
                        DatabaseReference chapterRef = FirebaseDatabase.getInstance()
                                .getReference("chapters")
                                .child(story.getStoryId())
                                .child(chapterId);

                        chapterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snap) {
                                String chapterTitle = snap.child("title").getValue(String.class);
                                if (chapterTitle != null) {
                                    holder.tvReadingChapter.setText("Đang đọc: " + chapterTitle);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    } else {
                        holder.tvReadingChapter.setText("Chưa đọc");
                    }
                } else {
                    holder.tvReadingChapter.setText("Chưa đọc");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Ảnh bìa
        Glide.with(context).load(story.getCoverUrl()).into(holder.imgCover);

        String lastestChapter=story.getLatestChapter().getTitle();
        holder.tvNewChapter.setText("Chương mới: " + lastestChapter);

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

    public class ReadingStoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvAuthor, tvWarning, tvNewChapter, tvReadingChapter;

        public ReadingStoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvWarning = itemView.findViewById(R.id.tvWarning); // cảnh báo bị xóa
            tvNewChapter = itemView.findViewById(R.id.tvLatestChapter);
            tvReadingChapter = itemView.findViewById(R.id.tvReadingChapter);
        }
    }
}
