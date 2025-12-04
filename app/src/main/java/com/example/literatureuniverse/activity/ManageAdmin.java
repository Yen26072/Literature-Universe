package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.AdminAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageAdmin extends BaseActivity {
    Button btnAdd;
    RecyclerView rvAdmin;
    DatabaseReference userRef;
    List<User> userList;
    AdminAdapter adminAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        btnAdd = findViewById(R.id.btnAdd);
        rvAdmin = findViewById(R.id.rvAdmin);
        btnAdd = findViewById(R.id.btnAdd);

        userRef = FirebaseDatabase.getInstance().getReference("users");

        userList = new ArrayList<>();
        rvAdmin.setLayoutManager(new LinearLayoutManager(this));
        adminAdapter = new AdminAdapter(this, new ArrayList<>());
        rvAdmin.setAdapter(adminAdapter);

        btnAdd.setOnClickListener(v -> showAddAdminDialog());

        loadAdmin();
    }

    private void showAddAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_admin, null);
        builder.setView(view);

        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            createAdminAccount(name, email, password, dialog);
        });
    }

    private void createAdminAccount(String name, String email, String password, AlertDialog dialog) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String userId = result.getUser().getUid();
                    String avatarUrl = "https://i.postimg.cc/kGtqkS3w/icons8-avatar-96.png";
                    long createdAt = System.currentTimeMillis();

                    User newAdmin = new User(userId, name, email, avatarUrl, "admin", false, null, true, null, 0, createdAt, null, null);

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userId)
                            .setValue(newAdmin)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Thêm admin thành công!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi database: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


    private void loadAdmin() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot snap : snapshot.getChildren()){
                    User user = snap.getValue(User.class);
                    if(user == null) continue;

                    if("admin".equals(user.getRole())){
                        userList.add(user);
                    }
                }
                adminAdapter.setData(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}