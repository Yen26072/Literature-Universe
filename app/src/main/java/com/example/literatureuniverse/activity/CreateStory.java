package com.example.literatureuniverse.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.ImageGridAdapter;
import com.example.literatureuniverse.adapter.TagCheckboxAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Tag;
import com.example.literatureuniverse.model.unsplash.UnsplashPhoto;
import com.example.literatureuniverse.model.unsplash.UnsplashResponse;
import com.example.literatureuniverse.network.UnsplashService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateStory extends BaseActivity {
    private GridView gridView;
    private TagCheckboxAdapter adapter;
    private List<Tag> tagList = new ArrayList<>();
    private DatabaseReference tagsRef;
    String selectedImageUrl = "";
    private Button btn1, btn2;
    private ImageView imgCoverStory;
    private final String ACCESS_KEY = "CexBhEFhEwwBJIN7qLyq6kTg-LevOCi6aBX0VH66Ifk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        gridView = findViewById(R.id.gridView);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        imgCoverStory = findViewById(R.id.imgCoverStory);
        tagsRef = FirebaseDatabase.getInstance().getReference("tags");

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
                adapter = new TagCheckboxAdapter(CreateStory.this, tagList);
                gridView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateStory.this, "Lỗi tải tag", Toast.LENGTH_SHORT).show();
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Tag> selected = adapter.getCheckedTags();

                if (selected.isEmpty()) {
                    Toast.makeText(CreateStory.this, "Chọn ít nhất 1 thể loại trước", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ưu tiên theo tag đầu tiên đã chọn
                Collections.sort(selected, Comparator.comparingInt(Tag::getPriority));
                String keyword = selected.get(0).getUnsplashKeyword();

                fetchUnsplashImages(keyword);
            }
        });
    }

    private void fetchUnsplashImages(String keyword) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UnsplashService service = retrofit.create(UnsplashService.class);

        service.searchPhotos(keyword, 6, ACCESS_KEY).enqueue(new Callback<UnsplashResponse>() {
            @Override
            public void onResponse(Call<UnsplashResponse> call, Response<UnsplashResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> urls = new ArrayList<>();
                    for (UnsplashPhoto photo : response.body().results) {
                        urls.add(photo.urls.regular);
                    }
                    showImageDialog(urls);
                } else {
                    Toast.makeText(CreateStory.this, "Không tìm được ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UnsplashResponse> call, Throwable t) {
                Toast.makeText(CreateStory.this, "Lỗi kết nối ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImageDialog(List<String> imageUrls) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_image_picker, null);

        GridView grid = view.findViewById(R.id.gridImages);

        AlertDialog dialog = builder.setView(view)
                .setTitle("Chọn ảnh minh họa")
                .setNegativeButton("Đóng", null)
                .create();

        ImageGridAdapter adapter1 = new ImageGridAdapter(this, imageUrls, url -> {
            selectedImageUrl = url;
            Log.d("SelectedImage", "URL: " + url);
            Glide.with(this).load(url).into(imgCoverStory);
            dialog.dismiss(); // Đóng dialog sau khi chọn
            imgCoverStory.setVisibility(View.VISIBLE);
        });

        grid.setAdapter(adapter1);
        dialog.show();

    }
}