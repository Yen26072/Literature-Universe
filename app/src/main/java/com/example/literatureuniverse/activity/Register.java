package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    EditText edtEmail, edtPassword, edtName, edtPasswordAgain;
    Button btnRegister;
    TextView txtError;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.editEmailRegister);
        edtPassword = findViewById(R.id.editPasswordRegister);
        edtName = findViewById(R.id.editName);
        btnRegister = findViewById(R.id.btnRegisterRegister);
        edtPasswordAgain = findViewById(R.id.editPasswordAgain);
        txtError = findViewById(R.id.txtErrorRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegister();
            }
        });
    }

    private void clickRegister() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String password2 = edtPasswordAgain.getText().toString().trim();
        String username1 = edtName.getText().toString().trim();

        txtError.setVisibility(View.GONE);

        if (email.isEmpty() || password.isEmpty() || password2.isEmpty() || username1.isEmpty()) {
            txtError.setText("Vui lòng điền đầy đủ thông tin");
            txtError.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(password2)) {
            txtError.setText("Mật khẩu không khớp");
            txtError.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Register", "Đã gọi onComplete");  // Bắt buộc phải hiện dòng này!
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("DEBUG", "createUserWithEmail:success");
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();
                            String email = user.getEmail();
                            String username = edtName.getText().toString();
                            Log.d("DEBUG", "ID = " +uid + "email = "+email + "username = "+username);
                            String avatarUrl = "https://i.postimg.cc/kGtqkS3w/icons8-avatar-96.png";
                            long createdAt = System.currentTimeMillis(); // 🌟 Thời gian đăng ký

                            // Bước 2: Tạo đối tượng user
                            User newUser = new User(uid, username, email, avatarUrl, "reader", false, null, false, null, 0, createdAt, null, null);

                            // Bước 3: Ghi vào Realtime Database
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            usersRef.child(uid).setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Register", "✅ Lưu user thành công");
                                        Toast.makeText(Register.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Register", "❌ Lỗi ghi Firebase", e);
                                        Toast.makeText(Register.this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(Register.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        task.addOnFailureListener(e -> {
                            Log.e("Register", "❌ Lỗi đăng ký: " + e.getMessage(), e);
                            Toast.makeText(Register.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                    }
                });

    }
}