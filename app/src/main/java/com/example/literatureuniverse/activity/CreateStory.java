package com.example.literatureuniverse.activity;

import static org.apache.commons.compress.archivers.zip.ZipShort.getBytes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.ExpandableHeightGridView;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.ImageGridAdapter;
import com.example.literatureuniverse.adapter.TagCheckboxAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.Tag;
import com.example.literatureuniverse.model.cloudinary.CloudinaryService;
import com.example.literatureuniverse.model.cloudinary.UploadResponse;
import com.example.literatureuniverse.model.unsplash.UnsplashPhoto;
import com.example.literatureuniverse.model.unsplash.UnsplashResponse;
import com.example.literatureuniverse.network.UnsplashService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateStory extends BaseActivity {
    private TagCheckboxAdapter adapter;
    private List<Tag> tagList = new ArrayList<>();
    private DatabaseReference tagsRef;
    String selectedImageUrl = "";
    private Button btn1, btn2, btnSubmit;
    EditText edtTitle, edtTextMultiline;
    private ImageView imgCoverStory;
    ExpandableHeightGridView gridView;
    private final String ACCESS_KEY = "CexBhEFhEwwBJIN7qLyq6kTg-LevOCi6aBX0VH66Ifk";
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private String uploadedImageUrl = null;
    private final String CLOUD_NAME = "dzuljozzy"; // thay bằng cloud_name của bạn
    private final String UPLOAD_PRESET = "unsigned_preset"; // thay bằng upload_preset bạn đã tạo

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
        gridView.setExpanded(true);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btnSubmit = findViewById(R.id.btnSubmit);
        edtTitle = findViewById(R.id.edtTitle);
        edtTextMultiline = findViewById(R.id.edtTextMultiline);
        imgCoverStory = findViewById(R.id.imgCoverStory);
        tagsRef = FirebaseDatabase.getInstance().getReference("tags");

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String description = edtTextMultiline.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(CreateStory.this, "Nhập tiêu đề truyện", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Tag> selectedTagObjects = adapter.getCheckedTags();
            if (selectedTagObjects.isEmpty()) {
                Toast.makeText(CreateStory.this, "Chọn ít nhất 1 thể loại", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> selectedTags = new ArrayList<>();
            for (Tag tag : selectedTagObjects) {
                selectedTags.add(tag.getId());
            }

            String storyId = "story_" + System.currentTimeMillis();

            // TH1: Chọn ảnh từ điện thoại
            if (uploadedImageUrl  != null) {
                saveStoryAndGoNext(storyId, title, description, currentUserId, uploadedImageUrl, selectedTags);
            }
            // TH2: Chọn ảnh từ Unsplash
            else if (selectedImageUrl != null && !selectedImageUrl.isEmpty()) {
                saveStoryAndGoNext(storyId, title, description, currentUserId, selectedImageUrl, selectedTags);
            }
            // TH3: Không chọn ảnh
            else {
                Toast.makeText(CreateStory.this, "Vui lòng chọn ảnh bìa!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveStoryAndGoNext(String storyId, String title, String description,
                                    String currentUserId, String coverUrl, List<String> selectedTags) {
        Story story = new Story();
        story.setStoryId(storyId);
        story.setTitle(title);
        story.setDescription(description);
        story.setAuthorId(currentUserId);
        story.setCoverUrl(coverUrl);
        story.setTags(new ArrayList<>(selectedTags));
        story.setStatus("Còn tiếp");
        story.setCreatedAt(System.currentTimeMillis());
        story.setUpdatedAt(System.currentTimeMillis());
        story.setLikesCount(0);
        story.setViewsCount(0);
        story.setCommentsCount(0);
        story.setFollowersCount(0);
        story.setDeleted(false);

        Log.d("DEBUG", "storyId=" + storyId + " title=" + title + " coverUrl=" + coverUrl);

        Intent intent = new Intent(CreateStory.this, AddChapter.class);
        intent.putExtra("story", story);
        intent.putExtra("isNewStory", true);
        startActivity(intent);
    }


    //Nhận ảnh người dùng chọn từ điện thoại
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imgCoverStory.setImageBitmap(bitmap);

                // Upload ngay khi chọn ảnh
                uploadImageToCloudinary(selectedImageUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = getBytes(inputStream);

            // Tạo Retrofit client
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            CloudinaryService service = retrofit.create(CloudinaryService.class);

            // Chuẩn bị multipart
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
            RequestBody preset = RequestBody.create(MediaType.parse("text/plain"), UPLOAD_PRESET);

            // Gọi API
            service.uploadImage(body, preset).enqueue(new retrofit2.Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, retrofit2.Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        uploadedImageUrl = response.body().getSecureUrl();
                        Log.d("Cloudinary", "Image URL: " + uploadedImageUrl);

                        runOnUiThread(() ->{
                                Toast.makeText(CreateStory.this, "Upload ảnh thành công", Toast.LENGTH_SHORT).show();
                                Glide.with(CreateStory.this).load(uploadedImageUrl).into(imgCoverStory);
                                imgCoverStory.setVisibility(View.VISIBLE);
                                }
                        );
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(CreateStory.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show()
                        );
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    runOnUiThread(() ->
                            Toast.makeText(CreateStory.this, "Lỗi upload: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void fetchUnsplashImages(String keyword) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UnsplashService service = retrofit.create(UnsplashService.class);

        service.searchPhotos(keyword, 24, ACCESS_KEY).enqueue(new Callback<UnsplashResponse>() {
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