package com.example.literatureuniverse.activity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private Story currentStory;
    private boolean isNewStory;
    private DatabaseReference storyRef, chaptersRef, tagStoriesRef;
    private static final int PICK_TEXT_FILE = 1;
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
        setupHeader(); // bắt buộc gọi sau setContentView

        edtChapterTitle = findViewById(R.id.edtChapterTitle);
        edtChapterContent = findViewById(R.id.edtChapterContent);
        btnSubmitChapter = findViewById(R.id.btnAddChapter);
        btnSelectFile = findViewById(R.id.btnChooseFile);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        chaptersRef = FirebaseDatabase.getInstance().getReference("chapters");
        tagStoriesRef = FirebaseDatabase.getInstance().getReference("storyTags");

        currentStory = (Story) getIntent().getSerializableExtra("story");
        isNewStory = getIntent().getBooleanExtra("isNewStory", false);


        if (currentStory == null) {
            Toast.makeText(this, "Không nhận được thông tin truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
            Intent fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileIntent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(fileIntent, PICK_DOCX_FILE);
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
        String chapterId = "chapter_" + System.currentTimeMillis();

        Chapter chapter = new Chapter();
        chapter.setChapterId(chapterId);
        chapter.setTitle(title);
        chapter.setContent(content);
        chapter.setStoryId(currentStory.getStoryId());
        chapter.setDeleted(false);
        long now = System.currentTimeMillis();
        chapter.setCreatedAt(now);
        chapter.setUpdatedAt(now);

        chaptersRef.child(currentStory.getStoryId())  // 👈 sửa tại đây!
                .child(chapterId)
                .setValue(chapter)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm chương", Toast.LENGTH_SHORT).show();
                    edtChapterTitle.setText("");
                    edtChapterContent.setText("");
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

            String content = readDocxFromUri(uri);
            List<Chapter> chapters = splitChapters(content);

            if(isNewStory){
                saveStoryAndChapters(chapters);
            }
            else{
                for (Chapter chapter : chapters) {
                    saveChapter(chapter.getTitle(), chapter.getContent());
                }
            }

            Toast.makeText(this, "Đã thêm " + chapters.size() + " chương từ file", Toast.LENGTH_LONG).show();
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
            chapter.setChapterId("chapter_" + System.currentTimeMillis());
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