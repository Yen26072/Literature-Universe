package com.example.literatureuniverse.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.ExpandableHeightGridView;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.ChapterAdapterForDetail;
import com.example.literatureuniverse.adapter.TagCheckboxAdapterForDetail;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Chapter;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.Tag;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class MyStoryDetail extends BaseActivity {

    private EditText edtTitle, edtDescription;
    private ImageView imgCover, btnDeleteStory, btnAddChapter, btnRestoreStory;
    private Button btnSave, btnToHome;
    private GridView gridTags;
    private RecyclerView recyclerChapterList;
    private Spinner spinnerStatus;
    private String[] statusOptions = {"Còn tiếp", "Hoàn thành", "Tạm ngưng"};

    private Story currentStory;
    private String storyId;
    private DatabaseReference storyRef, chapterRef, tagRef, userRef;
    private TagCheckboxAdapterForDetail tagAdapter;
    private ChapterAdapterForDetail chapterAdapter;
    private List<Chapter> chapterList;
    private List<Tag> allTags = new ArrayList<>();

    private boolean isDeletedByAuthor = false;
    private boolean isDeletedByAdmin = false;
    private boolean isDeleted = false;

    private int itemsPerPage = 15;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_story_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        imgCover = findViewById(R.id.imgCover);
        btnSave = findViewById(R.id.btnSave);
        btnToHome = findViewById(R.id.btnToHome);
        btnAddChapter = findViewById(R.id.imgAddChapter);
        btnDeleteStory = findViewById(R.id.imgDeleteStory);
        btnRestoreStory = findViewById(R.id.imgReStoreStory);
        gridTags = findViewById(R.id.gridTags);
        recyclerChapterList = findViewById(R.id.recyclerChapters);
        spinnerStatus = findViewById(R.id.spnStatus);
        pageTabsLayout = findViewById(R.id.tabContainer);
        paginationScroll = findViewById(R.id.tabScroll);

        chapterList = new ArrayList<>();
        recyclerChapterList.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new ChapterAdapterForDetail(this, new ArrayList<>(), true);
        recyclerChapterList.setAdapter(chapterAdapter);

        // Thiết lập Adapter cho Spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

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

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadStory();
        loadChapters();

        btnToHome.setOnClickListener(v -> {
            Intent intent = new Intent(MyStoryDetail.this, HomeStory.class);
            intent.putExtra("storyId", storyId);
            startActivity(intent);
        });

        btnAddChapter.setOnClickListener(v -> {
            Intent intent = new Intent(MyStoryDetail.this, AddChapter.class);
            intent.putExtra("storyId", storyId);
            startActivity(intent);
        });

        btnDeleteStory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa truyện")
                    .setMessage("Bạn có chắc muốn xóa truyện này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        storyRef.child("deleted").setValue(true);
                        storyRef.child("deletedBy").setValue(userId);
                        Intent intent = new Intent(MyStoryDetail.this, MyStory.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnSave.setOnClickListener(v -> saveStory());

        chapterAdapter.setOnChapterActionListener(new ChapterAdapterForDetail.OnChapterActionListener() {
            @Override
            public void onEdit(Chapter chapter) {
                if (isDeletedByAdmin) {
                    Toast.makeText(MyStoryDetail.this, "Không thể sửa chương vì truyện đã bị admin xóa", Toast.LENGTH_SHORT).show();
                    return;
                }
                showEditChapterDialog(chapter);
            }

            @Override
            public void onDelete(Chapter chapter) {
                new AlertDialog.Builder(MyStoryDetail.this)
                        .setTitle("Xóa chương")
                        .setMessage("Xóa chương \"" + chapter.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            chapterRef.child(chapter.getChapterId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        backupChapter(chapter, "deleted");
                                        checkAndUpdateLatestChapterAfterDelete(chapter);

                                        // đồng bộ luôn object currentStory trong app
                                        currentStory.setChaptersCount(currentStory.getChaptersCount() - 1);
                                        // ✅ Tăng chaptersCount
                                        storyRef.child("chaptersCount")
                                                .setValue((currentStory.getChaptersCount()));

                                        Log.d("MYSTORYDETAIL", "currentStory.getChaptersCount = " + currentStory.getChaptersCount());
                                        Toast.makeText(MyStoryDetail.this, "Đã xóa chương", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        btnRestoreStory.setOnClickListener(v -> {
            if (isDeletedByAuthor) {
                storyRef.child("deleted").setValue(false);
                storyRef.child("deletedBy").removeValue();

                Toast.makeText(this, "Đã khôi phục truyện", Toast.LENGTH_SHORT).show();
                btnRestoreStory.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnAddChapter.setVisibility(View.VISIBLE);
                btnDeleteStory.setVisibility(View.VISIBLE);
            }
        });

    }

    private void checkAndUpdateLatestChapterAfterDelete(Chapter deletedChapter) {
        if (currentStory.getLatestChapter() != null &&
                deletedChapter.getChapterId().equals(currentStory.getLatestChapter().getChapterId())) {

            chapterRef.orderByChild("createdAt").limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    Chapter latestChapter = snap.getValue(Chapter.class);
                                    if (latestChapter != null) {
                                        Story.LatestChapter latest = convertToLatestChapter(latestChapter);
                                        currentStory.setLatestChapter(latest);
                                        storyRef.child("latestChapter").setValue(latest);
                                        updateStoryTimestamp();
                                    }
                                }
                            } else {
                                // Không còn chương nào
                                currentStory.setLatestChapter(null);
                                storyRef.child("latestChapter").removeValue();
                                updateStoryTimestamp();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void loadStory() {
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Story story = snapshot.getValue(Story.class);
                if (story == null) return;
                currentStory = story;

                // Gán dữ liệu vào giao diện
                edtTitle.setText(story.getTitle());
                edtDescription.setText(story.getDescription());
                Glide.with(MyStoryDetail.this).load(story.getCoverUrl()).into(imgCover);

                String currentStatus = story.getStatus();
                int index = Arrays.asList(statusOptions).indexOf(currentStatus);
                if (index != -1) {
                    spinnerStatus.setSelection(index);
                }

                isDeleted = story.isDeleted();

                String deletedBy = snapshot.child("deletedBy").getValue(String.class);
                if (isDeleted) {
                    if (deletedBy != null && deletedBy.equals(story.getAuthorId())) {
                        isDeletedByAuthor = true;
                    } else {
                        isDeletedByAdmin = true;
                    }
                }

                handleUIBasedOnDeleteState();
                loadTags(); // ✅ Gọi loadTags sau khi đã có currentStory đầy đủ
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    //Hàm xử lý UI theo trạng thái xóa
    private void handleUIBasedOnDeleteState() {
        if (!isDeleted) {
            // Bình thường
            btnSave.setEnabled(true);
            btnAddChapter.setVisibility(View.VISIBLE);
            btnDeleteStory.setVisibility(View.VISIBLE);
            btnRestoreStory.setVisibility(View.GONE);
        } else if (isDeletedByAuthor) {
            // Tác giả xóa → có thể khôi phục
            btnSave.setEnabled(false);
            btnAddChapter.setVisibility(View.GONE);
            btnDeleteStory.setVisibility(View.GONE);
            btnRestoreStory.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Truyện đang bị ẩn. Bạn có thể khôi phục.", Toast.LENGTH_LONG).show();
        } else if (isDeletedByAdmin) {
            // Admin xóa → không được sửa
            btnSave.setEnabled(false);
            btnAddChapter.setVisibility(View.GONE);
            btnDeleteStory.setVisibility(View.GONE);
            btnRestoreStory.setVisibility(View.GONE);
            Toast.makeText(this, "Truyện đã bị admin xóa. Bạn chỉ có thể xem.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadTags() {
        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTags.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String label = child.child("label").getValue(String.class);
                    Integer priority = child.child("priority").getValue(Integer.class);
                    String keyword = child.child("unsplashKeyword").getValue(String.class);

                    if (id != null && label != null && priority != null && keyword != null) {
                        allTags.add(new Tag(id, label, priority, keyword));
                    }
                }

                Collections.sort(allTags, Comparator.comparingInt(Tag::getPriority));

                // Duyệt qua allTags để tick các tag đã chọn
                List<String> selectedTagIds = currentStory.getTags() != null ? currentStory.getTags() : new ArrayList<>();

                Log.d("TAG_GRID", "Số lượng tag hiển thị: " + allTags.size());
                Log.d("TAG_SELECTED", "Tag của truyện: " + selectedTagIds);

                tagAdapter = new TagCheckboxAdapterForDetail(MyStoryDetail.this, allTags, selectedTagIds);
                gridTags.setAdapter(tagAdapter);
                ((ExpandableHeightGridView) gridTags).setExpanded(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG_LOAD", "Lỗi tải tag: " + error.getMessage());
            }
        });
    }

    private void loadChapters() {
        chapterRef.orderByChild("createdAt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Chapter chapter = snap.getValue(Chapter.class);
                    if (chapter != null) chapterList.add(chapter);
                }
                currentPage = 1;
                updatePagination(); // GỌI 1 CHỖ DUY NHẤT
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) chapterList.size() / itemsPerPage);
        Log.d("MyStoryDebug", "Tổng số trang: " + totalPages);
        Log.d("MyStoryDebug", "Tổng số trang: " + totalPages);
        pageTabsLayout.removeAllViews();

        paginationScroll.setVisibility(View.VISIBLE);
        Log.d("MyStoryDebug", "Đã hiển thị tabScroll");
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
            Log.d("MyStoryDebug", "Tạo tab trang " + i);
            pageTabsLayout.addView(tab);
            Log.d("MyStoryDebug", "Tổng số tab con: " + pageTabsLayout.getChildCount());
        }

//        }

        displayCurrentPage(); // chỉ gọi ở đây
    }

    private void displayCurrentPage() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, chapterList.size());

        Log.d("MyStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<Chapter> subList = chapterList.subList(start, end);
        chapterAdapter.setData(subList);
    }

    private void saveStory() {
        if (currentStory == null) return;

        if (tagAdapter == null) {
            Toast.makeText(this, "Chưa tải xong danh sách tag", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Tag> selectedTagObjects = tagAdapter.getCheckedTags();
        List<String> selectedTags = new ArrayList<>();
        for (Tag tag : selectedTagObjects) {
            selectedTags.add(tag.getId()); // Hoặc tag.getLabel() tùy bạn lưu cái gì trong DB
        }

        Log.d("SAVE_TAGS", "Tags chọn: " + selectedTags);

        String title = edtTitle.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String selectedStatus = spinnerStatus.getSelectedItem().toString();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin truyện", Toast.LENGTH_SHORT).show();
            return;
        }

        //List<String> selectedTagIds = tagAdapter.getCheckedTags();

        currentStory.setTitle(title);
        currentStory.setDescription(desc);
        currentStory.setStatus(selectedStatus);
        currentStory.setTags(selectedTags);
        updateStoryTags(currentStory.getStoryId(), currentStory.getTags());

        storyRef.setValue(currentStory)
                .addOnSuccessListener(unused -> {
                    updateStoryTimestamp();
                    Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditChapterDialog(Chapter chapter) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_chapter, null);
        EditText edtTitle = dialogView.findViewById(R.id.edtChapterTitle);
        EditText edtContent = dialogView.findViewById(R.id.edtChapterContent);

        edtTitle.setText(chapter.getTitle());
        edtContent.setText(chapter.getContent());

        new AlertDialog.Builder(this)
                .setTitle("Sửa chương")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    backupChapter(chapter, "edited");
                    chapter.setTitle(edtTitle.getText().toString().trim());
                    chapter.setContent(edtContent.getText().toString().trim());
                    chapter.setUpdatedAt(System.currentTimeMillis());

                    DatabaseReference chapterRefUpdate = FirebaseDatabase.getInstance()
                            .getReference("chapters")
                            .child(storyId)
                            .child(chapter.getChapterId());

                    chapterRefUpdate.setValue(chapter)
                            .addOnSuccessListener(aVoid -> {
                                if (currentStory.getLatestChapter() != null &&
                                        chapter.getChapterId().equals(currentStory.getLatestChapter().getChapterId())) {
                                    Story.LatestChapter latest = convertToLatestChapter(chapter);
                                    currentStory.setLatestChapter(latest);
                                    storyRef.child("latestChapter").setValue(latest);
                                }
                                updateStoryTimestamp();
                                Toast.makeText(this, "Đã lưu chương", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateStoryTimestamp() {
        storyRef.child("updatedAt").setValue(System.currentTimeMillis());
    }

    private Story.LatestChapter convertToLatestChapter(Chapter chapter) {
        return new Story.LatestChapter(
                chapter.getChapterId(),
                chapter.getTitle(),
                chapter.getCreatedAt()
        );
    }

    private void backupChapter(Chapter chapter, String type) {
        DatabaseReference backupRef = FirebaseDatabase.getInstance()
                .getReference("chapterBackups")
                .child(storyId)
                .child(chapter.getChapterId())
                .push(); // push → lưu nhiều phiên bản

        Map<String, Object> backupData = new HashMap<>();
        backupData.put("title", chapter.getTitle());
        backupData.put("content", chapter.getContent());
        backupData.put("chapterId", chapter.getChapterId());
        backupData.put("storyId", storyId);
        backupData.put("backupTime", System.currentTimeMillis());
        backupData.put("type", type); // "edited" hoặc "deleted"

        backupRef.setValue(backupData)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Đã lưu bản backup chương", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi backup chương: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateStoryTags(String storyId, List<String> newTagIds) {
        DatabaseReference storyTagsRef = FirebaseDatabase.getInstance().getReference("storyTags");

        // Bước 1: Xóa storyId khỏi tất cả các tag cũ
        storyTagsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot tagSnap : snapshot.getChildren()) {
                    String tagId = tagSnap.getKey();
                    if (tagSnap.hasChild(storyId)) {
                        storyTagsRef.child(tagId).child(storyId).removeValue();
                    }
                }

                // Bước 2: Thêm lại storyId vào các tag mới
                for (String tagId : newTagIds) {
                    if (tagId != null && !tagId.trim().isEmpty()) {
                        storyTagsRef.child(tagId).child(storyId).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyStoryDetail.this, "Lỗi cập nhật tags: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
