package com.example.literatureuniverse.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Chapter;
import com.example.literatureuniverse.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChapterDetail extends BaseActivity {
    ImageView imgStar, imgPin, imgAdd, imgFont;
    TextView txtPreviousChapter, txtNextChapter, txtTitle, txtContent, txtStoryName, txtAuthorName;
    private DatabaseReference storyRef;
    private DatabaseReference likesRef, userRef2, chapterRef;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String storyId, chapterTitle, chapterContent, currentChapterId;
    private List<Chapter> chapterList = new ArrayList<>();
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chapter_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        imgStar = findViewById(R.id.imgStar);
        imgPin = findViewById(R.id.imgPin);
        imgAdd = findViewById(R.id.imgAdd);
        imgFont = findViewById(R.id.imgFont);
        txtPreviousChapter = findViewById(R.id.txtPreviousChapter);
        txtNextChapter = findViewById(R.id.txtNextChapter);
        txtTitle = findViewById(R.id.txtChapterName);
        txtContent = findViewById(R.id.txtChapterContent);
        txtStoryName = findViewById(R.id.txtStoryName);
        txtAuthorName = findViewById(R.id.txtAuthorName);

        storyId = getIntent().getStringExtra("storyId");
        chapterTitle = getIntent().getStringExtra("chapterTitle");
        chapterContent = getIntent().getStringExtra("chapterContent");
        currentChapterId = getIntent().getStringExtra("chapterId");

        storyRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
        likesRef = FirebaseDatabase.getInstance().getReference("likes").child(storyId);
        userRef2 = FirebaseDatabase.getInstance().getReference("users");
        chapterRef = FirebaseDatabase.getInstance().getReference("chapters").child(storyId);

        // Khi mở chương → kiểm tra đã like chưa
        likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Đã like → icon đỏ
                    imgStar.setColorFilter(ContextCompat.getColor(imgStar.getContext(), R.color.red), PorterDuff.Mode.SRC_IN);
                } else {
                    // Chưa like → icon xám
                    imgStar.setColorFilter(ContextCompat.getColor(imgStar.getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        // Khi click
        imgStar.setOnClickListener(v -> {
            likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Nếu đã like → bỏ like
                        likesRef.child(userId).removeValue();
                        storyRef.child("likesCount").setValue(ServerValue.increment(-1));
                        imgStar.setColorFilter(ContextCompat.getColor(imgStar.getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                    } else {
                        // Nếu chưa like → thêm like
                        likesRef.child(userId).setValue(true);
                        storyRef.child("likesCount").setValue(ServerValue.increment(1));
                        imgStar.setColorFilter(ContextCompat.getColor(imgStar.getContext(), R.color.red), PorterDuff.Mode.SRC_IN);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        imgPin.setOnClickListener(new View.OnClickListener() {
            private boolean isRed = false; // trạng thái ban đầu
            @Override
            public void onClick(View v) {
                if (isRed) {
                    // Đổi sang màu xám
                    imgPin.setColorFilter(
                            ContextCompat.getColor(ChapterDetail.this, R.color.gray),
                            PorterDuff.Mode.SRC_IN
                    );
                } else {
                    // Đổi sang màu đỏ
                    imgPin.setColorFilter(
                            ContextCompat.getColor(ChapterDetail.this, R.color.red),
                            PorterDuff.Mode.SRC_IN
                    );
                }
                isRed = !isRed; // đảo trạng thái
            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() {
            private boolean isRed = false; // trạng thái ban đầu
            @Override
            public void onClick(View v) {
                if (isRed) {
                    // Đổi sang màu xám
                    imgAdd.setColorFilter(
                            ContextCompat.getColor(ChapterDetail.this, R.color.gray),
                            PorterDuff.Mode.SRC_IN
                    );
                } else {
                    // Đổi sang màu đỏ
                    imgAdd.setColorFilter(
                            ContextCompat.getColor(ChapterDetail.this, R.color.red),
                            PorterDuff.Mode.SRC_IN
                    );
                }
                isRed = !isRed; // đảo trạng thái
            }
        });
        loadStory();
        loadChaptersFromFirebase();
        txtPreviousChapter.setOnClickListener(v -> showChapter(currentIndex - 1));
        txtNextChapter.setOnClickListener(v -> showChapter(currentIndex + 1));
    }

    private void loadStory() {
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Story story = snapshot.getValue(Story.class);
                if (story == null) return;
                String authorId = story.getAuthorId();
                userRef2.orderByChild("userId").equalTo(authorId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String authorName = userSnap.child("username").getValue(String.class);
                                    txtAuthorName.setText("Tác giả: " + authorName);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                // Gán dữ liệu vào giao diện
                txtStoryName.setText(story.getTitle());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showChapter(int index) {
        if (index < 0 || index >= chapterList.size()) return;

        currentIndex = index;
        Chapter chapter = chapterList.get(index);
        currentChapterId = chapter.getChapterId();

        txtTitle.setText(chapter.getTitle());
        txtContent.setText(chapter.getContent());

        // Ẩn hiện nút
        txtPreviousChapter.setVisibility(currentIndex == 0 ? View.GONE : View.VISIBLE);
        txtNextChapter.setVisibility(currentIndex == chapterList.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private void loadChaptersFromFirebase() {
        chapterRef.orderByChild("createdAt").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Chapter ch = snap.getValue(Chapter.class);
                    if (ch != null && !ch.isDeleted()) {
                        chapterList.add(ch);
                    }
                }

                // Tìm vị trí chương hiện tại
                for (int i = 0; i < chapterList.size(); i++) {
                    if (chapterList.get(i).getChapterId().equals(currentChapterId)) {
                        currentIndex = i;
                        break;
                    }
                }

                // Hiển thị chương
                if (currentIndex != -1) {
                    showChapter(currentIndex);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}