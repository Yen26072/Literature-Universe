package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import com.example.literatureuniverse.ExpandableHeightGridView;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.StoryAdapter;
import com.example.literatureuniverse.adapter.TagCheckboxAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.Tag;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilterStory extends BaseActivity {
    StoryAdapter storyAdapter;
    RecyclerView recyclerView;
    List<Story> allStories;
    List<Story> filteredStories;

    private int itemsPerPage = 5;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    private DatabaseReference storiesRef;
    private Button btnFilter;
    private TagCheckboxAdapter adapter;
    private List<Tag> tagList = new ArrayList<>();
    private DatabaseReference tagsRef;
    ExpandableHeightGridView gridView;
    private Spinner spnChapter;
    private String[] spnOptions = {" ", "1 - 20", "21 - 50", "51 - 100", "101 - 200", "200 +"};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        gridView = findViewById(R.id.gridTags);
        gridView.setExpanded(true);
        spnChapter = findViewById(R.id.spnChapter);
        btnFilter = findViewById(R.id.btnFilter);
        recyclerView = findViewById(R.id.recyclerCompletedStory);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        allStories = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storyAdapter = new StoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(storyAdapter);

        ArrayAdapter<String> quataChapterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spnOptions);
        quataChapterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChapter.setAdapter(quataChapterAdapter);

        tagsRef = FirebaseDatabase.getInstance().getReference("tags");
        storiesRef = FirebaseDatabase.getInstance().getReference("stories");

        tagsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tagList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String label = child.child("label").getValue(String.class);
                    Integer priority = child.child("priority").getValue(Integer.class);
                    String keyword = child.child("unsplashKeyword").getValue(String.class);

                    if (id != null && label != null && priority != null && keyword != null) {
                        tagList.add(new Tag(id, label, priority, keyword));
                    }
                }

                // Sắp xếp theo priority tăng dần
                Collections.sort(tagList, Comparator.comparingInt(Tag::getPriority));

                // Gán vào GridView adapter (giống như adapter trước)
                adapter = new TagCheckboxAdapter(FilterStory.this, tagList);
                gridView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FilterStory.this, "Lỗi tải tag", Toast.LENGTH_SHORT).show();
            }
        });

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allStories.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Story story = child.getValue(Story.class);
                            if (story != null) {
                                allStories.add(story);
                            }
                        }

                        // Lấy danh sách tag người dùng đã chọn
                        List<Tag> selectedTagObjects = adapter.getCheckedTags();
                        List<String> selectedTags = new ArrayList<>();
                        for (Tag tag : selectedTagObjects) {
                            selectedTags.add(tag.getId());
                        }

                        // Lấy option từ spinner
                        String chapterOption = spnChapter.getSelectedItem().toString();

                        // Gọi filterStories
                        List<Story> filtered = filterStories(allStories, selectedTags, chapterOption);

                        filteredStories = filtered;
                        currentPage = 1;
                        updatePagination();

                        if (filtered.isEmpty()) {
                            Toast.makeText(FilterStory.this, "Không tìm thấy truyện phù hợp", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private List<Story> filterStories(List<Story> allStories, List<String> selectedTags, String chapterOption) {
        List<Story> result = new ArrayList<>();

        for (Story story : allStories) {
            if (story == null) continue;

            // ----------------
            // 1. Kiểm tra tag (tag là bắt buộc)
            boolean matchTag = true;
            if (!selectedTags.isEmpty()) {
                for (String tag : selectedTags) {
                    if (!story.getTags().contains(tag)) {
                        matchTag = false;
                        break;
                    }
                }
            } else {
                // ❌ Nếu không chọn tag thì bỏ qua truyện
                matchTag = false;
            }

            if (!matchTag) continue;

            // 2. Kiểm tra số chương
            int count = story.getChaptersCount();
            boolean matchChapter = true;

            if (!chapterOption.equals(" ")) { // Nếu có chọn spinner
                switch (chapterOption) {
                    case "1 - 20":
                        matchChapter = (count >= 1 && count <= 20);
                        break;
                    case "21 - 50":
                        matchChapter = (count >= 21 && count <= 50);
                        break;
                    case "51 - 100":
                        matchChapter = (count >= 51 && count <= 100);
                        break;
                    case "101 - 200":
                        matchChapter = (count >= 101 && count <= 200);
                        break;
                    case "200 +":
                        matchChapter = (count > 200);
                        break;
                }
            }

            // 3. Nếu match cả 2 thì thêm vào kết quả
            if (matchChapter) {
                result.add(story);
            }
        }

        return result;
    }

    private void updatePagination() {
        if (filteredStories == null) filteredStories = new ArrayList<>();

        totalPages = (int) Math.ceil((double) filteredStories.size() / itemsPerPage);
        pageTabsLayout.removeAllViews();

        paginationScroll.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);

        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;
            TextView tab = new TextView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 2, 16, 2);
            tab.setLayoutParams(params);

            tab.setText(String.valueOf(i));
            tab.setTextSize(16);
            tab.setPadding(40, 20, 40, 20);
            tab.setTextColor(i == currentPage ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
            tab.setBackgroundResource(i == currentPage ? R.drawable.page_selected_bg : R.drawable.page_unselected_bg);

            tab.setOnClickListener(v -> {
                currentPage = pageNum;
                updatePagination(); // reload
            });

            pageTabsLayout.addView(tab);
        }

        displayCurrentPage();
    }

    private void displayCurrentPage() {
        if (filteredStories == null) return;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredStories.size());

        List<Story> subList = filteredStories.subList(start, end);
        storyAdapter.setData(subList);
    }
}