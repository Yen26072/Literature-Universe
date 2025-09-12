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
import android.widget.EditText;
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
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.ChapterAdapterForHomeStory;
import com.example.literatureuniverse.adapter.CommentAdapter;
import com.example.literatureuniverse.adapter.StoryAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Chapter;
import com.example.literatureuniverse.model.Comment;
import com.example.literatureuniverse.model.CommentReply;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.User;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeStory extends BaseActivity {
    private String authorName, authorId;
    private String storyId, commentnotificationId;
    private DatabaseReference storyRef, chapterRef, tagRef, userRef, commentRef, replyRef, userRef2;
    private TextView txtStoryName, txtAuthorName, txtEyes, txtLike, txtComments, txtStatus, txtNewChapter, txtLatestUpdate, txtDes, txtGoiY;
    private ImageView imgCover, imgCover2, imgAvatarComment;
    private RecyclerView recyclerViewChapter, recyclerViewGoiY, recyclerComment;
    LinearLayout tabContainerTop, tabContainerBottom;
    HorizontalScrollView paginationScrollChapterTop, paginationScrollChapterBottom;
    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private ChapterAdapterForHomeStory chapterAdapter;
    private StoryAdapter storyAdapter;
    private List<Chapter> chapterList;
    private List<Story> storyList;
    private List<TextView> allPageTabs = new ArrayList<>();
    private EditText edtComment;
    private Button btnSendComment;
    private List<Comment> commentList = new ArrayList<>();
    private Map<String, List<CommentReply>> replyMap = new HashMap<>();
    private CommentAdapter commentAdapter;
    private FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserId = null;
    private String chapterId = null;
    private HashMap<String, User> userMap = new HashMap<>();

    private List<Comment> allComments = new ArrayList<>();
    private List<TextView> allCommentTabs = new ArrayList<>();
    private int currentCommentPage = 1;
    private int totalCommentPages = 1;
    private int commentsPerPage = 3; // hoặc số dòng bạn muốn hiển thị
    private LinearLayout tabContainerComment;
    private HorizontalScrollView paginationScrollComment;
    private NestedScrollView nestedScroll;

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
        imgCover2 = findViewById(R.id.imageView5);
        imgAvatarComment = findViewById(R.id.imgAvatarComment);
        txtStatus = findViewById(R.id.txtStatus);
        txtNewChapter = findViewById(R.id.txtNewChapter);
        txtLatestUpdate = findViewById(R.id.txtLastestUpdate);
        txtDes = findViewById(R.id.txtDes);
        txtGoiY = findViewById(R.id.txtGoiY);
        recyclerViewChapter = findViewById(R.id.recyclerHomeStory_Chapter);
        recyclerViewGoiY = findViewById(R.id.rcvGoiY);
        tabContainerTop = findViewById(R.id.tabContainerChapter);
        tabContainerBottom = findViewById(R.id.tabContainerChapterBottom);
        paginationScrollChapterTop = findViewById(R.id.tabScrollChapter);
        paginationScrollChapterBottom = findViewById(R.id.tabScrollChapterBottom);
        edtComment = findViewById(R.id.edtComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        recyclerComment = findViewById(R.id.recyclerComment);
        tabContainerComment = findViewById(R.id.tabContainerComment);
        paginationScrollComment = findViewById(R.id.tabScrollComment);
        nestedScroll = findViewById(R.id.main);

        recyclerViewChapter.setLayoutManager(new LinearLayoutManager(this));
        chapterList = new ArrayList<>();
        chapterAdapter = new ChapterAdapterForHomeStory(new ArrayList<>(), this);
        recyclerViewChapter.setAdapter(chapterAdapter);

        recyclerViewGoiY.setLayoutManager(new LinearLayoutManager(this));
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this, new ArrayList<>());
        recyclerViewGoiY.setAdapter(storyAdapter);

        recyclerComment.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, new ArrayList<>(), replyMap, userMap);
        recyclerComment.setAdapter(commentAdapter);

        Log.d("HomeStory", "homestory opened");

        storyId = getIntent().getStringExtra("storyId");
        commentnotificationId = getIntent().getStringExtra("commentnotificationId");
        if (storyId == null) {
            Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        storyRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
        chapterRef = FirebaseDatabase.getInstance().getReference("chapters").child(storyId);
        tagRef = FirebaseDatabase.getInstance().getReference("tags");
        if(currentUserId != null){
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        }
        userRef2 = FirebaseDatabase.getInstance().getReference("users");
        commentRef = FirebaseDatabase.getInstance().getReference("comments");
        replyRef = FirebaseDatabase.getInstance().getReference("commentReplies");

        // Gửi bình luận
        btnSendComment.setOnClickListener(v -> sendComment());

        loadStory();
        loadChapters();
        loadTags();
        loadComments();
    }

    private void sendComment() {
        if(currentUserId == null || currentUserId.isEmpty()){
            Intent intent = new Intent(HomeStory.this, Login.class);
            intent.putExtra("isStoryId", storyId);
            intent.putExtra("source", "HomeStory");
            startActivity(intent);
        }
        else{
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Boolean isMuted = snapshot.child("isMuted").getValue(Boolean.class);
                        Long muteUntil = snapshot.child("muteUntil").getValue(Long.class);
                        long now = System.currentTimeMillis();
                        if (Boolean.TRUE.equals(isMuted) && muteUntil != null && muteUntil > now) {
                            Toast.makeText(HomeStory.this, "Bạn đang bị chặn bình luận", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    String content = edtComment.getText().toString().trim();
                    if (content.isEmpty()) {
                        edtComment.setError("Vui lòng nhập nội dung");
                        return;
                    }

                    String commentId = commentRef.push().getKey();
                    long timestamp = System.currentTimeMillis();

                    Comment comment = new Comment(
                            commentId,
                            currentUserId,
                            storyId,
                            null,
                            null,
                            content,
                            timestamp,
                            false, null, null,
                            0,
                            false, null
                    );

                    commentRef.child(commentId).setValue(comment)
                            .addOnSuccessListener(unused -> {
                                DatabaseReference targetRef;
                                if (chapterId == null || chapterId.isEmpty()) {
                                    targetRef = FirebaseDatabase.getInstance().getReference("stories").child(storyId);
                                } else {
                                    targetRef = FirebaseDatabase.getInstance().getReference("chapters").child(storyId).child(chapterId);
                                }

                                targetRef.child("commentsCount").runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                        Long count = currentData.getValue(Long.class);
                                        currentData.setValue((count == null ? 0 : count + 1));
                                        return Transaction.success(currentData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                        if (error != null) {
                                            Log.e("CommentCountUpdate", "Transaction failed: " + error.getMessage());
                                        }
                                    }
                                });

                                // ✅ Thêm comment mới vào list gốc rồi cập nhật phân trang
                                recyclerComment.post(() -> {
                                    allComments.add(0, comment);
                                    totalCommentPages = (int) Math.ceil((double) allComments.size() / commentsPerPage);

                                    // Nếu đang ở trang 1 thì hiển thị ngay
                                    if (currentCommentPage == 1) {
                                        displayCurrentCommentPage();
                                        recyclerComment.scrollToPosition(0);
                                    } else {
                                        // Nếu không ở trang 1 thì có thể load lại tab hoặc thông báo
                                        updateCommentPagination();
                                    }
                                });

                                edtComment.setText("");
                            });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeStory.this, "Lỗi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void loadComments() {
        commentRef.orderByChild("storyId").equalTo(storyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Comment c = snap.getValue(Comment.class);
                            if (c != null && !c.isDeleted()) {
                                commentList.add(0, c); // mới nhất trên cùng
                            }
                        }
                        allComments.clear();
                        allComments.addAll(commentList);
                        currentCommentPage = 1;
                        updateCommentPagination();

                        // Sau khi load comment xong, load reply
                        replyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                replyMap.clear();
                                for (DataSnapshot replyGroup : snapshot.getChildren()) {
                                    String commentId = replyGroup.getKey();
                                    List<CommentReply> replies = new ArrayList<>();
                                    for (DataSnapshot replySnap : replyGroup.getChildren()) {
                                        CommentReply reply = replySnap.getValue(CommentReply.class);
                                        if (reply != null && !reply.isDeleted()) {
                                            replies.add(reply);
                                        }
                                    }
                                    replyMap.put(commentId, replies);
                                }

                                // Cập nhật adapter
                                displayCurrentCommentPage();
                                updateCommentTabStyles();
                                if(commentnotificationId!=null){
                                    scrollToComment();
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

    private void updateCommentPagination() {
        totalCommentPages = (int) Math.ceil((double) allComments.size() / commentsPerPage);
        tabContainerComment.removeAllViews();
        allCommentTabs.clear();

        paginationScrollComment.setVisibility(View.VISIBLE);

        for (int i = 1; i <= totalCommentPages; i++) {
            final int pageNum = i;
            TextView tab = createCommentTabTextView(pageNum);
            tabContainerComment.addView(tab);
            allCommentTabs.add(tab);
        }

        displayCurrentCommentPage();
    }

    private TextView createCommentTabTextView(int pageNum) {
        TextView tab = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 2, 16, 2);
        tab.setLayoutParams(params);
        tab.setText(String.valueOf(pageNum));
        tab.setTextSize(16);
        tab.setPadding(40, 20, 40, 20);

        if (pageNum == currentCommentPage) {
            tab.setTextColor(getResources().getColor(R.color.white));
            tab.setBackgroundResource(R.drawable.page_selected_bg);
        } else {
            tab.setTextColor(getResources().getColor(R.color.black));
            tab.setBackgroundResource(R.drawable.page_unselected_bg);
        }

        tab.setOnClickListener(v -> {
            currentCommentPage = pageNum;
            updateCommentTabStyles();
            displayCurrentCommentPage();
        });

        return tab;
    }

    private void updateCommentTabStyles() {
        for (TextView tab : allCommentTabs) {
            int tabNum = Integer.parseInt(tab.getText().toString());
            if (tabNum == currentCommentPage) {
                tab.setTextColor(getResources().getColor(R.color.white));
                tab.setBackgroundResource(R.drawable.page_selected_bg);
            } else {
                tab.setTextColor(getResources().getColor(R.color.black));
                tab.setBackgroundResource(R.drawable.page_unselected_bg);
            }
        }
    }

    private void displayCurrentCommentPage() {
        int start = (currentCommentPage - 1) * commentsPerPage;
        int end = Math.min(start + commentsPerPage, allComments.size());

        List<Comment> subList = allComments.subList(start, end);
        commentAdapter.setData(subList, replyMap);
        recyclerComment.scrollToPosition(0);
    }

    private void scrollToComment() {
        int globalIndex = -1;
        for (int i = 0; i < allComments.size(); i++) {
            if (allComments.get(i).getCommentId().equals(commentnotificationId)) {
                globalIndex = i;
                break;
            }
        }

        if (globalIndex != -1) {
            // Tính trang chứa comment đó
            int targetPage = (globalIndex / commentsPerPage) + 1;
            currentCommentPage = targetPage;
            // Chuyển tab & render lại page
            updateCommentTabStyles();
            displayCurrentCommentPage();

            // Sau khi load lại subList thì tìm index trong subList
            int start = (currentCommentPage - 1) * commentsPerPage;
            int localIndex = globalIndex - start;

            // Đợi layout xong rồi mới cuộn cha (NestedScrollView/ScrollView)
            recyclerComment.postDelayed(() -> {
                // thử lấy ViewHolder của item trong page
                RecyclerView.ViewHolder vh = recyclerComment.findViewHolderForAdapterPosition(localIndex);
                if (vh == null) {
                    // ép RV bind item trước
                    recyclerComment.scrollToPosition(localIndex);
                    recyclerComment.postDelayed(() -> {
                        RecyclerView.ViewHolder vh2 = recyclerComment.findViewHolderForAdapterPosition(localIndex);
                        if (vh2 != null) {
                            smoothScrollParentToChild(vh2.itemView);
                        }
                        // highlight
                        commentAdapter.highlightComment(commentnotificationId);
                        commentnotificationId = null;
                    }, 50);
                } else {
                    smoothScrollParentToChild(vh.itemView);
                    commentAdapter.highlightComment(commentnotificationId);
                    commentnotificationId = null;
                }
            }, 50);
        }
    }

    private void smoothScrollParentToChild(View child) {
        // vị trí tuyệt đối theo cha cuộn
        int y = (int) (recyclerComment.getTop() + child.getTop());
        if (nestedScroll != null) {
            nestedScroll.smoothScrollTo(0, y);
        }
    }

    private void loadChapters() {
        chapterRef.orderByChild("createdAt") // ✅ Sắp xếp theo thời gian
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chapterList.clear();
                        for (DataSnapshot chapterSnap : snapshot.getChildren()) {
                            Chapter chapter = chapterSnap.getValue(Chapter.class);
                            if (chapter != null) {
                                chapterList.add(chapter); // mặc định là từ cũ -> mới
                                Log.d("ChapterDebug", "✔ Thêm chương: " + chapter.getTitle() + " | createdAt: " + chapter.getCreatedAt());
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
        tabContainerTop.removeAllViews();
        tabContainerBottom.removeAllViews();
        allPageTabs.clear();

        paginationScrollChapterTop.setVisibility(View.VISIBLE);
        paginationScrollChapterBottom.setVisibility(View.VISIBLE);

        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;

            TextView topTab = createTabTextView(pageNum);
            TextView bottomTab = createTabTextView(pageNum);

            tabContainerTop.addView(topTab);
            tabContainerBottom.addView(bottomTab);

            allPageTabs.add(topTab);
            allPageTabs.add(bottomTab);
        }

        displayCurrentPage();
    }

    private TextView createTabTextView(int pageNum) {
        TextView tab = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 2, 16, 2);
        tab.setLayoutParams(params);
        tab.setText(String.valueOf(pageNum));
        tab.setTextSize(16);
        tab.setPadding(40, 20, 40, 20);

        // Đặt màu và nền theo tab đang chọn
        if (pageNum == currentPage) {
            tab.setTextColor(getResources().getColor(R.color.white));
            tab.setBackgroundResource(R.drawable.page_selected_bg);
        } else {
            tab.setTextColor(getResources().getColor(R.color.black));
            tab.setBackgroundResource(R.drawable.page_unselected_bg);
        }

        tab.setOnClickListener(v -> {
            currentPage = pageNum;
            updateTabStyles();  // Cập nhật tất cả các tab
            displayCurrentPage();
        });

        return tab;
    }

    private void updateTabStyles() {
        for (TextView tab : allPageTabs) {
            int tabNum = Integer.parseInt(tab.getText().toString());
            if (tabNum == currentPage) {
                tab.setTextColor(getResources().getColor(R.color.white));
                tab.setBackgroundResource(R.drawable.page_selected_bg);
            } else {
                tab.setTextColor(getResources().getColor(R.color.black));
                tab.setBackgroundResource(R.drawable.page_unselected_bg);
            }
        }
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
                authorId = story.getAuthorId();
                userRef2.orderByChild("userId").equalTo(authorId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    authorName = userSnap.child("username").getValue(String.class);
                                    txtAuthorName.setText(authorName);
                                    Log.d("HomeStory", "author name: "+ authorName);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date(story.getUpdatedAt()));

                // Gán dữ liệu vào giao diện
                txtStoryName.setText(story.getTitle());
//                txtAuthorName.setText(authorName);
                txtEyes.setText(String.valueOf(story.getViewsCount()));
                txtLike.setText(String.valueOf(story.getLikesCount()));
                txtComments.setText(String.valueOf(story.getCommentsCount()));
                txtDes.setText("Giới thiệu: " + story.getDescription());
                txtStatus.setText("Trạng thái: " + story.getStatus());
                txtLatestUpdate.setText("Cập nhật: " + date);
                txtNewChapter.setText("Chương mới: " + story.getLatestChapter().getTitle());
                Glide.with(HomeStory.this).load(story.getCoverUrl()).into(imgCover);
                Glide.with(HomeStory.this).load(story.getCoverUrl()).into(imgCover2);

                if(currentUserId == null){
                    imgAvatarComment.setVisibility(View.GONE);
                }
                else {
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(getApplicationContext())
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.ic_launcher_background)
                                        .circleCrop()
                                        .into(imgAvatarComment);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }


                loadOtherStoriesByAuthor(authorId, storyId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadOtherStoriesByAuthor(String authorId, String currentStoryId) {
        DatabaseReference storiesRef = FirebaseDatabase.getInstance().getReference("stories");

        storiesRef.orderByChild("authorId").equalTo(authorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Story> otherStories = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Story story = snap.getValue(Story.class);
                            if (story != null && !story.getStoryId().equals(currentStoryId)) {
                                otherStories.add(story);
                                if (otherStories.size() >= 4) break;
                            }
                        }

                        if (!otherStories.isEmpty()) {
                            txtGoiY.setVisibility(View.VISIBLE);
                            recyclerViewGoiY.setVisibility(View.VISIBLE);
                            storyAdapter.setData(otherStories);
                        } else {
                            txtGoiY.setVisibility(View.GONE);
                            recyclerViewGoiY.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
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