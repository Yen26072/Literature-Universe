package com.example.literatureuniverse.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.ChapterAdapterForHomeStory;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Chapter;
import com.example.literatureuniverse.model.Story;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeStory extends BaseActivity {
    private String authorName, authorId;
    private Story currentStory;
    private String storyId;
    private DatabaseReference storyRef, chapterRef, tagRef, userRef;
    private TextView txtStoryName, txtAuthorName, txtEyes, txtLike, txtComments, txtStatus, txtNewChapter, txtLatestUpdate, txtTags, txtDes;
    private ImageView imgCover;
    private RecyclerView recyclerViewChapter;
    LinearLayout tabContainerTop, tabContainerBottom;
    HorizontalScrollView paginationScrollChapterTop, paginationScrollChapterBottom;
    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private ChapterAdapterForHomeStory chapterAdapter;
    private List<Chapter> chapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        txtStoryName = findViewById(R.id.txtStoryName);
        txtAuthorName = findViewById(R.id.txtAuthorName);
        txtEyes = findViewById(R.id.txtEyes);
        txtLike = findViewById(R.id.txtLike);
        txtComments = findViewById(R.id.txtComments);
        imgCover = findViewById(R.id.imgCover2);
        txtStatus = findViewById(R.id.txtStatus);
        txtNewChapter = findViewById(R.id.txtNewChapter);
        txtLatestUpdate = findViewById(R.id.txtLastestUpdate);
        txtTags = findViewById(R.id.txtTags);
        txtDes = findViewById(R.id.txtDes);
        recyclerViewChapter = findViewById(R.id.recyclerHomeStory_Chapter);
        tabContainerTop = findViewById(R.id.tabContainerChapter);
        tabContainerBottom = findViewById(R.id.tabContainerChapterBottom);
        paginationScrollChapterTop = findViewById(R.id.tabScrollChapter);
        paginationScrollChapterBottom = findViewById(R.id.tabScrollChapterBottom);

        recyclerViewChapter.setLayoutManager(new LinearLayoutManager(this));
        chapterList = new ArrayList<>();
        chapterAdapter = new ChapterAdapterForHomeStory(new ArrayList<>(), this);
        recyclerViewChapter.setAdapter(chapterAdapter);

        storyId = getIntent().getStringExtra("storyId");
        if (storyId == null) {
            Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storyRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
        chapterRef = FirebaseDatabase.getInstance().getReference("chapters").child(storyId);
        tagRef = FirebaseDatabase.getInstance().getReference("tags");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        loadStory();
        loadChapters();
        loadTags();
    }

    private void loadChapters() {
        chapterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();
                for (DataSnapshot chapterSnap : snapshot.getChildren()) {
                    Chapter chapter = chapterSnap.getValue(Chapter.class);
                    if (chapter != null) {
                        chapterList.add(chapter);
                        Log.d("ChapterDebug", "✔ Thêm chương: " + chapter.getTitle());
                    } else {
                        Log.w("ChapterDebug", "Chương bị null: " + chapterSnap.getKey());
                    }
                }
                currentPage = 1;
                updatePagination();
                Log.d("ChapterDebug", "Tổng số chương load được: " + chapterList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) chapterList.size() / itemsPerPage);
        Log.d("HomeStoryDebug", "Tổng số trang: " + totalPages);
        Log.d("HomeStoryDebug", "Tổng số trang: " + totalPages);
        tabContainerTop.removeAllViews();

        paginationScrollChapterTop.setVisibility(View.VISIBLE);
        Log.d("HomeStoryDebug", "Đã hiển thị tabScroll");
        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;

            // TẠO TEXTVIEW VỚI MARGIN, PADDING ĐẦY ĐỦ
            TextView tab = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 2, 16, 2);
            tab.setLayoutParams(params);

            tab.setText(String.valueOf(i));
            tab.setTextSize(16);
            tab.setPadding(40, 20, 40, 20); // padding giúp tab dễ bấm và dễ nhìn
            tab.setTextColor(i == currentPage ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
            tab.setBackgroundResource(i == currentPage ? R.drawable.page_selected_bg : R.drawable.page_unselected_bg);

            tab.setOnClickListener(v -> {
                currentPage = pageNum;
                updatePagination(); // Cập nhật tab và trang hiện tại
            });
            Log.d("HomeStoryDebug", "Tạo tab trang " + i);
            tabContainerTop.addView(tab);
            Log.d("HomeStoryDebug", "Tổng số tab con: " + tabContainerTop.getChildCount());
        }
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, chapterList.size());

        Log.d("HomeStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<Chapter> subList = chapterList.subList(start, end);
        chapterAdapter.setData(subList);
        recyclerViewChapter.post(() -> {
            setRecyclerViewHeightOneLinePerItem(recyclerViewChapter, subList.size());
        });
    }

    private void setRecyclerViewHeightOneLinePerItem(RecyclerView recyclerView, int itemCount) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null || itemCount == 0) return;

        View itemView = LayoutInflater.from(recyclerView.getContext())
                .inflate(R.layout.item_chapter_home_story, recyclerView, false);

        itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
        );

        int itemHeight = itemView.getMeasuredHeight();
        int totalHeight = itemHeight * itemCount;

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = totalHeight;
        recyclerView.setLayoutParams(params);
        Log.d("DebugHeight", "Mỗi item cao: " + itemHeight + ", Tổng: " + totalHeight);
    }



    private void loadStory() {

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Story story = snapshot.getValue(Story.class);
                if (story == null) return;
                currentStory = story;
                authorId = story.getAuthorId();
                userRef.orderByChild("userId").equalTo(authorId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    authorName = userSnap.child("username").getValue(String.class);
                                    txtAuthorName.setText(authorName);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date(story.getUpdatedAt()));

                // Gán dữ liệu vào giao diện
                txtStoryName.setText(story.getTitle());
                txtAuthorName.setText(authorName);
                txtEyes.setText(String.valueOf(story.getViewsCount()));
                txtLike.setText(String.valueOf(story.getLikesCount()));
                txtComments.setText(String.valueOf(story.getCommentsCount()));
                txtDes.setText("Giới thiệu: " + story.getDescription());
                txtStatus.setText("Trạng thái: " + story.getStatus());
                txtLatestUpdate.setText("Cập nhật: " + date);
                txtNewChapter.setText("Chương mới: " + story.getLatestChapter().getTitle());
                Glide.with(HomeStory.this).load(story.getCoverUrl()).into(imgCover);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTags() {
        FlexboxLayout flexboxLayout = findViewById(R.id.flexboxTags);
        flexboxLayout.removeAllViews();

        storyRef.child("tags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tagIdsSnapshot) {
                if (!tagIdsSnapshot.exists()) return;

                List<String> tagIds = new ArrayList<>();
                for (DataSnapshot tagSnap : tagIdsSnapshot.getChildren()) {
                    String tagId = tagSnap.getValue(String.class);
                    if (tagId != null) tagIds.add(tagId);
                }

                tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot allTagsSnapshot) {
                        for (String tagId : tagIds) {
                            if (allTagsSnapshot.hasChild(tagId)) {
                                String label = allTagsSnapshot.child(tagId).child("label").getValue(String.class);
                                if (label != null) {
                                    TextView tagView = new TextView(HomeStory.this);
                                    tagView.setText(label);
                                    tagView.setTextSize(14);
                                    tagView.setTextColor(Color.WHITE);
                                    tagView.setPadding(32, 12, 32, 12);
                                    tagView.setBackgroundResource(R.drawable.tag_background);

                                    FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    );
                                    params.setMargins(16, 8, 16, 8);
                                    tagView.setLayoutParams(params);

                                    flexboxLayout.addView(tagView);

                                    tagView.setOnClickListener(v -> {
                                        Intent intent = new Intent(HomeStory.this, TagStories.class);
                                        intent.putExtra("tagId", tagId);       // ví dụ: "am_nhac"
                                        intent.putExtra("tagLabel", label);    // ví dụ: "Âm nhạc"
                                        startActivity(intent);
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}