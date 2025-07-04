package com.example.literatureuniverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;

public class MyStory extends BaseActivity {
    Button btnStart;
    ImageView imgAdd;
    TextView textView7;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader(); // bắt buộc gọi sau setContentView

        btnStart = findViewById(R.id.btnStart);
        imgAdd = findViewById(R.id.imgAdd);
        textView7 = findViewById(R.id.textView7);
        recyclerView = findViewById(R.id.recyclerMyStory);

    }

    @Override
    protected void onRoleLoaded(String role) {
        if ("reader".equals(role)) {
            btnStart.setVisibility(View.VISIBLE);
            textView7.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}