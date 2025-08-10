package com.example.literatureuniverse.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChapterDetail extends BaseActivity {
    ImageView imgStar, imgPin, imgAdd, imgFont;
    TextView txtPreviousChapter, txtNextChapter, txtTitle, txtContent, txtStoryName, txtAuthorName, txtPreviousChapter2, txtNextChapter2, txtStoryHome;
    private DatabaseReference storyRef, likesRef, userRef2, chapterRef, bookmarkRef, followRef, libraryRef;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String storyId, currentChapterId;
    private List<Chapter> chapterList = new ArrayList<>();
    private int currentIndex = -1;
    ScrollView scrollView;
    private boolean isFollowing = false; // trạng thái hiện tại

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
        txtPreviousChapter2 = findViewById(R.id.txtPreviousChapter2);
        txtNextChapter2 = findViewById(R.id.txtNextChapter2);
        txtStoryHome = findViewById(R.id.txtHomeStory);
        scrollView = findViewById(R.id.main);

        storyId = getIntent().getStringExtra("storyId");
        currentChapterId = getIntent().getStringExtra("chapterId");

        storyRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
        likesRef = FirebaseDatabase.getInstance().getReference("likes").child(storyId);
        userRef2 = FirebaseDatabase.getInstance().getReference("users");
        chapterRef = FirebaseDatabase.getInstance().getReference("chapters").child(storyId);
        bookmarkRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(userId).child(storyId);
        followRef = FirebaseDatabase.getInstance().getReference("follows").child(userId).child(storyId);
        libraryRef = FirebaseDatabase.getInstance().getReference("libraries").child(userId).child(storyId);

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

        // Khi bấm icon ghim
        imgPin.setOnClickListener(v -> {
            bookmarkRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists() && currentChapterId.equals(snapshot.getValue(String.class))) {
                    // Nếu đang ghim chính nó → bỏ ghim
                    bookmarkRef.removeValue().addOnSuccessListener(aVoid -> {
                        imgPin.setColorFilter(ContextCompat.getColor(this, R.color.gray));
                    });
                } else {
                    // Ghim chương mới (ghi đè chương cũ)
                    bookmarkRef.setValue(currentChapterId).addOnSuccessListener(aVoid -> {
                        imgPin.setColorFilter(ContextCompat.getColor(this, R.color.red));
                    });
                }
            });
        });

        loadStory();
        loadChaptersFromFirebase();
        txtPreviousChapter.setOnClickListener(v -> showChapter(currentIndex - 1));
        txtNextChapter.setOnClickListener(v -> showChapter(currentIndex + 1));
        txtPreviousChapter2.setOnClickListener(v -> showChapter(currentIndex - 1));
        txtNextChapter2.setOnClickListener(v -> showChapter(currentIndex + 1));

        // 🔹 Xử lý khi nhấn vào imgAdd
        imgAdd.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add, popupMenu.getMenu());

        // Lấy trạng thái từ Firebase (song song)
        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popupMenu.getMenu().findItem(R.id.menu_follow).setChecked(snapshot.exists());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                popupMenu.getMenu().findItem(R.id.menu_library).setChecked(snapshot.exists());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Sự kiện click
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_follow) {
                if (item.isChecked()) {
                    followRef.removeValue().addOnSuccessListener(aVoid -> {
                        item.setChecked(false);
                        Toast.makeText(getApplicationContext(), "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("followedAt", System.currentTimeMillis());
                    followRef.setValue(data).addOnSuccessListener(aVoid -> {
                        item.setChecked(true);
                        Toast.makeText(getApplicationContext(), "Đã theo dõi truyện", Toast.LENGTH_SHORT).show();
                    });
                }
                return true;
            }

            if (id == R.id.menu_library) {
                if (item.isChecked()) {
                    libraryRef.removeValue().addOnSuccessListener(aVoid -> {
                        item.setChecked(false);
                        Toast.makeText(getApplicationContext(), "Đã xóa khỏi thư viện", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("addedAt", System.currentTimeMillis());
                    libraryRef.setValue(data).addOnSuccessListener(aVoid -> {
                        item.setChecked(true);
                        Toast.makeText(getApplicationContext(), "Đã thêm vào thư viện", Toast.LENGTH_SHORT).show();
                    });
                }
                return true;
            }

            return false;
        });

        popupMenu.show();
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

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
        currentIndex = index;
        Chapter chapter = chapterList.get(index);
        currentChapterId = chapter.getChapterId();

        txtTitle.setText(chapter.getTitle());
        txtContent.setText(chapter.getContent());
        txtPreviousChapter.setText("< Chương trước");
        txtNextChapter.setText("Chương sau >");
        txtPreviousChapter2.setText("< Chương trước");
        txtNextChapter2.setText("Chương sau >");

        loadImgPinStatus(currentChapterId);

        // Ẩn hiện nút
        txtPreviousChapter.setVisibility(currentIndex == 0 ? View.GONE : View.VISIBLE);
        txtNextChapter.setVisibility(currentIndex == chapterList.size() - 1 ? View.GONE : View.VISIBLE);
        txtPreviousChapter2.setVisibility(currentIndex == 0 ? View.GONE : View.VISIBLE);
        txtNextChapter2.setVisibility(currentIndex == chapterList.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private void loadImgPinStatus(String currentChapterId) {
        bookmarkRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String savedChapterId = snapshot.getValue(String.class);
                if (currentChapterId.equals(savedChapterId)) {
                    imgPin.setColorFilter(ContextCompat.getColor(this, R.color.red));
                } else {
                    imgPin.setColorFilter(ContextCompat.getColor(this, R.color.gray));
                }
            } else {
                imgPin.setColorFilter(ContextCompat.getColor(this, R.color.gray));
            }
        });
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