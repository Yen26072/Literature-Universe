package com.example.literatureuniverse.activity;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.StoryAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResult extends BaseActivity {
    private RecyclerView recyclerView;
    private StoryAdapter storyAdapter;
    private List<Story> resultStories;
    private DatabaseReference storyRef, userRef;

    private String keyword;
    private TextView txtSearchTitle;

    private int itemsPerPage = 5;
    private int currentPage = 1;
    private int totalPages = 1;

    LinearLayout pageTabsLayout;
    HorizontalScrollView paginationScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        txtSearchTitle = findViewById(R.id.txtSearchResult);
        recyclerView = findViewById(R.id.recyclerSearchResultStory);
        pageTabsLayout = findViewById(R.id.tabContainerStory);
        paginationScroll = findViewById(R.id.tabScrollStory);

        resultStories = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storyAdapter = new StoryAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(storyAdapter);

        keyword = getIntent().getStringExtra("keyword");
        txtSearchTitle.setText("Kết quả tìm kiếm: " + keyword);

        storyRef = FirebaseDatabase.getInstance().getReference("stories");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        loadSearchResults(keyword);
    }

    private void loadSearchResults(String keyword) {
        final String searchKey = keyword.toLowerCase();

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                List<Story> tempStories = new ArrayList<>();

                for (DataSnapshot ds1 : snapshot1.getChildren()) {
                    Story story = ds1.getValue(Story.class);
                    if (story != null) {
                        tempStories.add(story);
                    }
                }

                // B2: lấy user list để map authorId -> username
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                        Map<String, String> authorMap = new HashMap<>();
                        for (DataSnapshot ds2 : snapshot2.getChildren()) {
                            User user = ds2.getValue(User.class);
                            if (user != null) {
                                authorMap.put(user.getUserId(),
                                        user.getUsername() != null ? user.getUsername().toLowerCase() : "");
                            }
                        }

                        // Chia exact / fuzzy
                        List<Story> exactMatches = new ArrayList<>();
                        List<Story> fuzzyMatches = new ArrayList<>();

                        for (Story s : tempStories) {
                            String title = (s.getTitle() != null) ? s.getTitle().toLowerCase() : "";
                            String authorName = authorMap.getOrDefault(s.getAuthorId(), "");

                            if (title.contains(searchKey) || authorName.contains(searchKey)) {
                                exactMatches.add(s);
                            } else {
                                fuzzyMatches.add(s);
                            }
                        }

                        // Sắp xếp exactMatches
                        Collections.sort(exactMatches, (s1, s2) -> {
                            int d1 = levenshtein(searchKey, s1.getTitle().toLowerCase());
                            int d2 = levenshtein(searchKey, s2.getTitle().toLowerCase());
                            return Integer.compare(d1, d2);
                        });

                        // Sắp xếp fuzzyMatches
                        Collections.sort(fuzzyMatches, (s1, s2) -> {
                            int d1 = levenshtein(searchKey, s1.getTitle().toLowerCase());
                            int d2 = levenshtein(searchKey, s2.getTitle().toLowerCase());
                            return Integer.compare(d1, d2);
                        });

                        // Gộp kết quả
                        resultStories.clear();
                        resultStories.addAll(exactMatches);

                        // Giới hạn 10 fuzzy
                        int limit = Math.min(2, fuzzyMatches.size());
                        resultStories.addAll(fuzzyMatches.subList(0, limit));
                        currentPage = 1;
                        updatePagination();

//                        Log.d("SearchDebug", "Kết quả: " + resultStories.size());
//                        storyAdapter.setData(resultStories); // dùng setData() như đã fix
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) resultStories.size() / itemsPerPage);
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
        int end = Math.min(start + itemsPerPage, resultStories.size());

        Log.d("MyStoryDebug", "Hiển thị từ index " + start + " đến " + (end - 1));

        List<Story> subList = resultStories.subList(start, end);
        storyAdapter.setData(subList);
    }

    // Hàm đo độ giống chuỗi (Levenshtein Distance đơn giản)
    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost
                    );
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}