package com.example.literatureuniverse.activity;

import static com.example.literatureuniverse.R.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.User;
import com.example.literatureuniverse.model.cloudinary.CloudinaryService;
import com.example.literatureuniverse.model.cloudinary.UploadResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyProfile extends BaseActivity {
    ImageView imgAvatar, imgEdit;
    TextView txtUsername;
    DatabaseReference userRef;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private Uri selectedImageUri = null;


    private AlertDialog editDialog;
    private ImageView imgAvatarEdit_InDialog;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final String CLOUD_NAME = "dzuljozzy";
    private final String UPLOAD_PRESET = "unsigned_preset";
    private String uploadedImageUrl = null;
    private String currentAvatarUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        imgAvatar = findViewById(R.id.imgAvatar2);
        txtUsername = findViewById(R.id.txtUserName);
        imgEdit = findViewById(R.id.imgEdit);

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        imgEdit.setOnClickListener(v -> {
            if (currentAvatarUrl == null) {
                // Chỉ mở dialog sau khi dữ liệu đã load
                Toast.makeText(MyProfile.this, "Đang tải dữ liệu hồ sơ...", Toast.LENGTH_SHORT).show();
            } else {
                showEditProfileDialog();
            }
        });

        // Load dữ liệu khi Activity khởi tạo
        showMyProfile();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
        View view = LayoutInflater.from(MyProfile.this)
                .inflate(R.layout.dialog_edit_profile, null);

        builder.setView(view);

        editDialog = builder.create();
        editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        editDialog.show();

        imgAvatarEdit_InDialog = view.findViewById(R.id.imgAvatarEdit);
        EditText edtUsernameEdit = view.findViewById(R.id.edtUsernameEdit);
        Button btnSaveEdit = view.findViewById(R.id.btnSaveEdit); // <-- Lấy tham chiếu
        TextView txtButton = view.findViewById(R.id.txtButton);
        Button btnReturn = view.findViewById(R.id.btnReturn); // Giả định id.btnReturn nằm trong R.id

        // Thiết lập biến trạng thái ảnh đã chọn về null khi mở dialog
        selectedImageUri = null;
        uploadedImageUrl = null;

        // Load dữ liệu hiện tại (SỬA LỖI TẢI ẢNH BẰNG URL)
        edtUsernameEdit.setText(txtUsername.getText().toString());
        if (currentAvatarUrl != null) {
            Glide.with(MyProfile.this)
                    .load(currentAvatarUrl)
                    .circleCrop()
                    .into(imgAvatarEdit_InDialog);
        }

        // Nhấn chọn ảnh
        txtButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Xử lý nút LƯU
        btnSaveEdit.setOnClickListener(v -> {
            String newName = edtUsernameEdit.getText().toString().trim();

            if (newName.isEmpty()) {
                edtUsernameEdit.setError("Tên không được để trống");
                return;
            }

            String finalAvatarUrl = null;

            if (uploadedImageUrl != null) {
                // Trường hợp 1: Người dùng đã chọn ảnh và upload đã HOÀN THÀNH
                finalAvatarUrl = uploadedImageUrl;
            } else if (selectedImageUri != null) {
                // Trường hợp 2: Người dùng đã chọn ảnh nhưng upload CHƯA HOÀN THÀNH
                Toast.makeText(MyProfile.this, "Vui lòng đợi ảnh tải lên hoàn tất...", Toast.LENGTH_SHORT).show();
                return;
            }

            // Trường hợp 3: selectedImageUri == null VÀ uploadedImageUrl == null
            // -> Người dùng chỉ đổi tên, finalAvatarUrl = null (updateUserToFirebase sẽ bỏ qua avatar)

            updateUserToFirebase(newName, finalAvatarUrl);
        });

        // Xử lý nút Hủy/Trở về
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialog.dismiss();
            }
        });
    }

    // ====== CẬP NHẬT ẢNH TRONG DIALOG KHI CHỌN TỪ GALERY ======
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadedImageUrl = null; // Reset URL đã upload

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imgAvatarEdit_InDialog.setImageBitmap(bitmap);

                // Upload ngay khi chọn ảnh (PASSING null vì không muốn truy cập lại Button)
                // Thay vì vô hiệu hóa/kích hoạt, ta sẽ kiểm tra trạng thái trong nút Lưu
                uploadImageToCloudinary(selectedImageUri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ===== CẬP NHẬT USER LÊN FIREBASE =====
    private void updateUserToFirebase(String newName, @Nullable String newAvatarUrl) {
        userRef.child("username").setValue(newName);

        // Chỉ cập nhật avatarUrl nếu có URL mới được cung cấp (tức là đã upload ảnh mới)
        if (newAvatarUrl != null) {
            userRef.child("avatarUrl").setValue(newAvatarUrl);
            // Cập nhật currentAvatarUrl để lần mở dialog sau hiển thị đúng
            currentAvatarUrl = newAvatarUrl;
            Glide.with(MyProfile.this).load(newAvatarUrl).circleCrop().into(imgAvatar);
        }

        txtUsername.setText(newName);
        Toast.makeText(MyProfile.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
        showMyProfile();


        if (editDialog != null) editDialog.dismiss();
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        // ... (Giữ nguyên phần khởi tạo Retrofit và lấy imageBytes)
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
                                    Toast.makeText(MyProfile.this, "Upload ảnh thành công! Bạn có thể nhấn Lưu.", Toast.LENGTH_LONG).show();
                                    // Không cần load lại ảnh ở đây vì nó đã được load tạm thời trong onActivityResult
                                }
                        );
                    } else {
                        uploadedImageUrl = null; // Đảm bảo null nếu thất bại
                        runOnUiThread(() ->
                                Toast.makeText(MyProfile.this, "Upload ảnh thất bại! Vui lòng thử lại.", Toast.LENGTH_LONG).show()
                        );
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    uploadedImageUrl = null; // Đảm bảo null nếu thất bại
                    runOnUiThread(() ->
                            Toast.makeText(MyProfile.this, "Lỗi upload: " + t.getMessage(), Toast.LENGTH_LONG).show()
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

    // SỬA: Lưu currentAvatarUrl
    private void showMyProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    txtUsername.setText(user.getUsername());

                    // LƯU URL AVATAR VÀO BIẾN TOÀN CỤC
                    currentAvatarUrl = user.getAvatarUrl();
                    Log.d("MyProfile2222", "currentAvatarUrl" + currentAvatarUrl);

                    Glide.with(MyProfile.this)
                            .load(currentAvatarUrl)
                            //.placeholder(R.drawable.ic_launcher_background)
                            .circleCrop()
                            .into(imgAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}