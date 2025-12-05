package com.example.literatureuniverse.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.base.BaseActivity;

public class HomeAdmin extends BaseActivity {
    LinearLayout linearCommentReport, linearStoryReport, linearReviewReport, linearAppeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        linearCommentReport = findViewById(R.id.linearCommentReport);
        linearStoryReport = findViewById(R.id.linearStoryReport);
        linearReviewReport = findViewById(R.id.linearReviewReport);
        linearAppeal = findViewById(R.id.linearAppeal);

        linearCommentReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Comment Report");
                Intent intent = new Intent(HomeAdmin.this, FullPendingCommentReport.class);
                startActivity(intent);
            }
        });
        linearStoryReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Story Report");
                Intent intent = new Intent(HomeAdmin.this, FullPendingStoryReport.class);
                startActivity(intent);
            }
        });
        linearReviewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Review Report");
                Intent intent = new Intent(HomeAdmin.this, FullPendingReviewReport.class);
                startActivity(intent);
            }
        });
        linearAppeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG_CLICK", "Clicked Review Appeal");
                Intent intent = new Intent(HomeAdmin.this, FullAppeal.class);
                startActivity(intent);
            }
        });
    }
}