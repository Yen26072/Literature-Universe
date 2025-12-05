package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.adapter.AppRuleAdapter;
import com.example.literatureuniverse.base.BaseActivity;
import com.example.literatureuniverse.model.AppRule;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FullAppRule extends BaseActivity {
    RecyclerView rvAppRules;
    private DatabaseReference rulesRef;
    private List<AppRule> ruleList;
    private AppRuleAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_app_rule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        rvAppRules = findViewById(R.id.rvAppRules);

        rulesRef = FirebaseDatabase.getInstance().getReference("appRules");

        ruleList = new ArrayList<>();
        rvAppRules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppRuleAdapter(this, new ArrayList<>());
        rvAppRules.setAdapter(adapter);

        loadRulesFromFirebase();
    }

    private void loadRulesFromFirebase() {
        rulesRef.get().addOnSuccessListener(snapshot -> {
            ruleList.clear();
            for (var child : snapshot.getChildren()) {
                AppRule rule = child.getValue(AppRule.class);
                if (rule != null) ruleList.add(rule);
            }
            adapter.setData(ruleList);
        });
    }
}