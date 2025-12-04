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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    TextView txtRegisterLogin, txtError;
    EditText edtEmail, edtPassword;
    Button btnLogin;
    DatabaseReference usersRef;
    String storyId, chapterId, source;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.editEmailLogin);
        edtPassword = findViewById(R.id.editPasswordLogin);
        btnLogin = findViewById(R.id.btnLoginLogin);
        txtError = findViewById(R.id.txtErrorLogin);
        txtRegisterLogin = findViewById(R.id.txtRegisterLogin);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        storyId = getIntent().getStringExtra("isStoryId");
        chapterId = getIntent().getStringExtra("isChapterId");
        source = getIntent().getStringExtra("source");


        txtRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmpty();
            }
        });
    }

    private void checkEmpty() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if(email.isEmpty() || password.isEmpty()){
            txtError.setText("Vui lòng nhập đầy đủ thông tin");
            txtError.setVisibility(View.VISIBLE);
        }
        else{
            txtError.setVisibility(View.GONE);
            checkAccount(email, password);
        }
    }

    private void checkAccount(String email, String password) {
        long startTime = System.currentTimeMillis();
        Log.d("LoginDebug", "Bắt đầu đăng nhập: " + email);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    long authTime = System.currentTimeMillis();
                    Log.d("LoginDebug", "Auth hoàn thành sau " + (authTime - startTime) + " ms");
                    if (task.isSuccessful()) {
                        Log.d("LoginDebug", "Đăng nhập thành công");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Log.d("LoginDebug", "UID người dùng: " + uid);

                            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d("LoginDebug", "onDataChange được gọi");
                                    if (snapshot.exists()) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        Log.d("LoginDebug", "Vai trò: " + role);
                                        if ("admin_super".equals(role)) {
                                            startActivity(new Intent(Login.this, HomeAdminSuper.class));
                                            finish();
                                        }
                                        if ("admin".equals(role)) {
                                            startActivity(new Intent(Login.this, HomeAdmin.class));
                                            finish();
                                        }else if("reader".equals(role) || "author".equals(role)){
                                            if(storyId != null && chapterId == null){
                                                Intent intent = new Intent(Login.this, HomeStory.class);
                                                intent.putExtra("storyId", storyId);
                                                Log.d("LoginHomeStory", "StoryId = " + storyId);
                                                startActivity(intent);
                                                finish();
                                            } else if(storyId != null && chapterId != null && "ChapterDetail".equals(source)){
                                                Intent intent = new Intent(Login.this, ChapterDetail.class);
                                                intent.putExtra("storyId", storyId);
                                                intent.putExtra("chapterId", chapterId);
                                                Log.d("LoginHomeStory", "chapterId = " + chapterId);
                                                startActivity(intent);
                                                finish();
                                            } else if(storyId != null && chapterId != null && "HomeStory".equals(source)){
                                                Intent intent = new Intent(Login.this, HomeStory.class);
                                                intent.putExtra("storyId", storyId);
                                                startActivity(intent);
                                                finish();
                                            }
                                            if(storyId == null){
                                                startActivity(new Intent(Login.this, MainActivity.class));
                                                finish();
                                            }

                                        }

                                    } else {
                                        txtError.setText("Chưa có tài khoản trong Realtime Database");
                                        txtError.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Login.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        txtError.setText("Tài khoản chưa đăng ký");
                        txtError.setVisibility(View.VISIBLE);
                    }
                });
    }
}