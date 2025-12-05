package com.example.literatureuniverse.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

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

public class ManageAppRules extends BaseActivity {
    private EditText edtTitle, edtDescription, edtDuration;
    private RadioGroup rgActionType;
    private RadioButton rbCommentReview, rbStory;
    private Button btnSaveRule, btnAddNewRule;
    private RecyclerView rvRules;

    private DatabaseReference rulesRef;
    private List<AppRule> ruleList;
    private AppRuleAdapter adapter;

    private AppRule editingRule = null; // nếu đang sửa

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_app_rules);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupHeader();

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtDuration = findViewById(R.id.edtDuration);
        rgActionType = findViewById(R.id.rgActionType);
        rbCommentReview = findViewById(R.id.rbCommentReview);
        rbStory = findViewById(R.id.rbStory);
        btnAddNewRule = findViewById(R.id.btnAddRule);
        rvRules = findViewById(R.id.rvAppRules);

        rulesRef = FirebaseDatabase.getInstance().getReference("appRules");

        ruleList = new ArrayList<>();
        adapter = new AppRuleAdapter(this, ruleList, new AppRuleAdapter.RuleActionListener() {
            @Override
            public void onEdit(AppRule rule) {
                showRuleDialog(rule);
            }

            @Override
            public void onDelete(AppRule rule) {
                // Hiển thị popup xác nhận
                new androidx.appcompat.app.AlertDialog.Builder(ManageAppRules.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa quy định này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Xóa thật
                            rulesRef.child(rule.getRuleId()).removeValue();
                            ruleList.remove(rule);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ManageAppRules.this, "Đã xóa quy định", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        rvRules.setLayoutManager(new LinearLayoutManager(this));
        rvRules.setAdapter(adapter);

        btnAddNewRule.setOnClickListener(v -> showRuleDialog(null));

        loadRulesFromFirebase();
    }

    private void loadRulesFromFirebase() {
        rulesRef.get().addOnSuccessListener(snapshot -> {
            ruleList.clear();
            for (var child : snapshot.getChildren()) {
                AppRule rule = child.getValue(AppRule.class);
                if (rule != null) ruleList.add(rule);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showRuleDialog(AppRule ruleToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_app_rule, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtTitle = view.findViewById(R.id.edtTitle);
        EditText edtDescription = view.findViewById(R.id.edtDescription);
        EditText edtDuration = view.findViewById(R.id.edtDuration);
        RadioGroup rgActionType = view.findViewById(R.id.rgActionType);
        RadioButton rbCommentReview = view.findViewById(R.id.rbCommentReview);
        RadioButton rbStory = view.findViewById(R.id.rbStory);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Nếu đang sửa
        if (ruleToEdit != null) {
            edtTitle.setText(ruleToEdit.getTitle());
            edtDescription.setText(ruleToEdit.getDescription());
            edtDuration.setText(String.valueOf(ruleToEdit.getDurationMillis() / (30L*24*60*60*1000)));
            if ("comment_review".equals(ruleToEdit.getActionType())) rbCommentReview.setChecked(true);
            else rbStory.setChecked(true);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();
            String durationStr = edtDuration.getText().toString().trim();
            String actionType = rbCommentReview.isChecked() ? "comment_review" : "story";

            if (title.isEmpty() || desc.isEmpty() || durationStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            long months;
            try {
                months = Long.parseLong(durationStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            long durationMillis = months * 30L*24*60*60*1000;

            String ruleId = ruleToEdit != null ? ruleToEdit.getRuleId() : rulesRef.push().getKey();
            AppRule newRule = new AppRule(ruleId, title, desc, actionType, durationMillis);

            if (ruleId != null) {
                rulesRef.child(ruleId).setValue(newRule).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                    loadRulesFromFirebase();
                    dialog.dismiss();
                });
            }
        });
    }
}