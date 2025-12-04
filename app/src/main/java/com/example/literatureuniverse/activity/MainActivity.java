package com.example.literatureuniverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView rvNewStories, rvCompletedStories;
    private StoryAdapter newStoryAdapter, completedStoryAdapter;
    private List<Story> newStories = new ArrayList<>();
    private List<Story> completedStories = new ArrayList<>();
    private DatabaseReference storiesRef;
    private TextView txtSeeMore1, txtSeeMore2;
    private EditText edtSearch;
    private ImageButton btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        rvNewStories = findViewById(R.id.rvNewStories);
        rvCompletedStories = findViewById(R.id.rvCompletedStories);
        txtSeeMore1 = findViewById(R.id.tvSeeMore1);
        txtSeeMore2 = findViewById(R.id.tvSeeMore2);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);

        txtSeeMore1.setText("Xem thêm >>");
        txtSeeMore2.setText("Xem thêm >>");

        rvNewStories.setLayoutManager(new LinearLayoutManager(this));
        rvCompletedStories.setLayoutManager(new LinearLayoutManager(this));

        newStoryAdapter = new StoryAdapter(this, newStories);
        completedStoryAdapter = new StoryAdapter(this, completedStories);

        rvNewStories.setAdapter(newStoryAdapter);
        rvCompletedStories.setAdapter(completedStoryAdapter);

        storiesRef = FirebaseDatabase.getInstance().getReference("stories");

        loadNewStories();
        loadCompletedStories();

        txtSeeMore1.setOnClickListener(v -> openFullNewStories());
        txtSeeMore2.setOnClickListener(v -> openFullCompletedStories());

        btnSearch.setOnClickListener(v -> {
            String keyword = edtSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, SearchResult.class);
                intent.putExtra("keyword", keyword);
                startActivity(intent);
            }
        });
    }

    private void openFullCompletedStories() {
        Intent intent = new Intent(MainActivity.this, FullCompletedStories.class);
        startActivity(intent);
    }

    private void openFullNewStories() {
        Intent intent = new Intent(MainActivity.this, FullNewStories.class);
        startActivity(intent);
    }

    @Override
    protected void onRoleLoaded(String role) {
        if ("admin_super".equals(role)) {
            startActivity(new Intent(this, HomeAdminSuper.class));
            finishAffinity();
        } else if ("admin".equals(role)) {
            startActivity(new Intent(this, HomeAdmin.class));
            finishAffinity();
        }
    }

    private void loadNewStories() {
        storiesRef.orderByChild("createdAt").limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        newStories.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Story story = ds.getValue(Story.class);
                            if (story != null) {
                                newStories.add(story);
                            }
                        }
                        // Firebase trả ngược (cũ trước, mới sau), cần đảo ngược
                        Collections.reverse(newStories);
                        newStoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadCompletedStories() {
        storiesRef.orderByChild("updatedAt").limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        completedStories.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Story story = ds.getValue(Story.class);
                            if (story != null && "Hoàn thành".equals(story.getStatus())) {
                                completedStories.add(story);
                            }
                        }
                        Collections.reverse(completedStories);
                        Log.d("DEBUG", "Completed stories count: " + completedStories.size());
                        for (Story s : completedStories) {
                            Log.d("MAINACTIVITYDEBUG", "Completed: " + s.getTitle() + " | updatedAt: " + s.getUpdatedAt());
                        }
                        completedStoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}