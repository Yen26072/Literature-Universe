package com.example.literatureuniverse.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.literatureuniverse.model.Chapter;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class AddChapter extends BaseActivity {
    private EditText edtChapterTitle, edtChapterContent;
    private Button btnSubmitChapter, btnSelectFile;
    private TextView tvChapterHeader;
    private LinearLayout chapterListLayout;
    private Story currentStory;
    private String storyId;
    private boolean isNewStory;
    private DatabaseReference storyRef, chaptersRef, tagStoriesRef;
    private static final int PICK_DOCX_FILE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_chapter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        edtChapterTitle = findViewById(R.id.edtChapterTitle);
        edtChapterContent = findViewById(R.id.edtChapterContent);
        btnSubmitChapter = findViewById(R.id.btnAddChapter);
        btnSelectFile = findViewById(R.id.btnChooseFile);
        tvChapterHeader = findViewById(R.id.tvChapterHeader);
        chapterListLayout = findViewById(R.id.chapterListLayout);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        chaptersRef = FirebaseDatabase.getInstance().getReference("chapters");
        tagStoriesRef = FirebaseDatabase.getInstance().getReference("storyTags");

        storyId = getIntent().getStringExtra("storyId");

        isNewStory = getIntent().getBooleanExtra("isNewStory", false);


        if (storyId == null) {
            currentStory = (Story) getIntent().getSerializableExtra("story");
            Toast.makeText(this, "currentStory=story",Toast.LENGTH_SHORT).show();
        }
        else{
            storyRef.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) return;

                    Story story = snapshot.getValue(Story.class);
                    if (story == null) return;
                    currentStory = story;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        btnSubmitChapter.setOnClickListener(v -> {
            String title = edtChapterTitle.getText().toString().trim();
            String content = edtChapterContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Tiêu đề và nội dung không được bỏ trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isNewStory) {
                saveStoryAndChapter(title, content);
            } else {
                saveChapter(title, content);
            }
        });

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            });
            startActivityForResult(intent, PICK_DOCX_FILE);
        });
    }

    private void saveStoryAndChapter(String chapterTitle, String chapterContent) {
        // Lưu truyện mới
        storyRef.child(currentStory.getStoryId()).setValue(currentStory)
                .addOnSuccessListener(aVoid -> {
                    for (String tag : currentStory.getTags()) {
                        tagStoriesRef.child(tag).child(currentStory.getStoryId()).setValue(true);
                    }
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        DatabaseReference currentUserRef = userRef.child(uid);

                        currentUserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String role = snapshot.getValue(String.class);
                                if ("reader".equals(role)) {
                                    currentUserRef.child("role").setValue("author");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    saveChapter(chapterTitle, chapterContent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu truyện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveChapter(String title, String content) {
        String chapterId = chaptersRef.push().getKey();

        Chapter chapter = new Chapter();
        chapter.setChapterId(chapterId);
        chapter.setTitle(title);
        chapter.setContent(content);
        chapter.setStoryId(currentStory.getStoryId());
        chapter.setDeleted(false);
        long now = System.currentTimeMillis();
        chapter.setCreatedAt(now);
        chapter.setUpdatedAt(now);

        chaptersRef.child(currentStory.getStoryId())
                .child(chapterId)
                .setValue(chapter)
                .addOnSuccessListener(aVoid -> {
                    edtChapterTitle.setText("");
                    edtChapterContent.setText("");

                    // ✅ Cập nhật updatedAt cho truyện
                    storyRef.child(currentStory.getStoryId())
                            .child("updatedAt")
                            .setValue(now);

                    // ✅ Cập nhật latestChapter
                    DatabaseReference latestChapterRef = storyRef.child(currentStory.getStoryId()).child("latestChapter");
                    latestChapterRef.child("chapterId").setValue(chapterId);
                    latestChapterRef.child("title").setValue(title);
                    latestChapterRef.child("createdAt").setValue(now);

                    // ✅ Tăng chaptersCount
                    storyRef.child(currentStory.getStoryId())
                            .child("chaptersCount")
                            .setValue((currentStory.getChaptersCount() + 1));

                    // đồng bộ luôn object currentStory trong app
                    currentStory.setChaptersCount(currentStory.getChaptersCount() + 1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DOCX_FILE && resultCode == RESULT_OK && data != null) {

            Uri uri = data.getData();

            // ⚠️ Quan trọng: cần persist permission (máy thật bắt buộc)
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // Đọc file
            String content = readDocxFromUri(uri);
            if (content == null || content.trim().isEmpty()) {
                Toast.makeText(this, "Không thể đọc nội dung file!", Toast.LENGTH_LONG).show();
                return;
            }

            // Tách chương
            List<Chapter> chapters = splitChapters(content);

            // Lưu chương
            if (isNewStory) {
                saveStoryAndChapters(chapters);
            } else {
                for (Chapter chapter : chapters) {
                    saveChapter(chapter.getTitle(), chapter.getContent());
                }
            }

            // Hiển thị danh sách chương đã tách
            showChapterTitles(chapters);

            Toast.makeText(
                    this,
                    "Đã thêm " + chapters.size() + " chương từ file",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showChapterTitles(List<Chapter> chapters) {
        tvChapterHeader.setVisibility(View.VISIBLE);
        chapterListLayout.setVisibility(View.VISIBLE);
        chapterListLayout.removeAllViews();

        for (Chapter chapter : chapters) {
            TextView titleView = new TextView(this);
            titleView.setText("• " + chapter.getTitle());
            titleView.setTextSize(14);
            titleView.setPadding(0, 8, 0, 8);
            chapterListLayout.addView(titleView);
        }
    }

    private List<Chapter> splitChapters(String content) {
        List<Chapter> chapters = new ArrayList<>();

        // Tách theo từ "Chương" có thể có số hoặc chữ (Chương 1, Chương Một,...)
        String[] rawChapters = content.split("(?=Chương\\s+[^\\n]*)");

        for (String raw : rawChapters) {
            if (raw.trim().isEmpty()) continue;

            String[] lines = raw.trim().split("\n", 2);
            String title = lines[0].trim();
            String body = lines.length > 1 ? lines[1].trim() : "";

            Chapter chapter = new Chapter();
            chapter.setChapterId(chaptersRef.push().getKey());
            chapter.setTitle(title);
            chapter.setContent(body);
            chapter.setStoryId(currentStory.getStoryId());
            chapter.setCreatedAt(System.currentTimeMillis());
            chapter.setUpdatedAt(System.currentTimeMillis());
            chapter.setDeleted(false);

            chapters.add(chapter);

            try {
                Thread.sleep(1); // tránh trùng ID
            } catch (InterruptedException ignored) {}
        }

        return chapters;
    }


    private String readDocxFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            XWPFDocument document = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi đọc file DOCX: " + e.getMessage();
        }
    }

    private void saveStoryAndChapters(List<Chapter> chapters) {
        // Lưu truyện mới khi dung File
        storyRef.child(currentStory.getStoryId()).setValue(currentStory)
                .addOnSuccessListener(aVoid -> {
                    for (String tag : currentStory.getTags()) {
                        tagStoriesRef.child(tag).child(currentStory.getStoryId()).setValue(true);
                    }

                    // Cập nhật role người dùng thành author nếu là reader
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        DatabaseReference currentUserRef = userRef.child(uid);

                        currentUserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String role = snapshot.getValue(String.class);
                                if ("reader".equals(role)) {
                                    currentUserRef.child("role").setValue("author");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }

                    // Lưu từng chương
                    for (Chapter chapter : chapters) {
                        saveChapter(chapter.getTitle(), chapter.getContent());
                    }

                    Toast.makeText(this, "Đã lưu truyện và thêm " + chapters.size() + " chương", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu truyện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}