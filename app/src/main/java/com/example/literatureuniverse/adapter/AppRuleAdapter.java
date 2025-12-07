package com.example.literatureuniverse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.AppRule;
import com.example.literatureuniverse.model.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppRuleAdapter extends RecyclerView.Adapter<AppRuleAdapter.RuleViewHolder> {

    public interface RuleActionListener {
        void onEdit(AppRule rule);
        void onDelete(AppRule rule);
    }

    private Context context;
    private List<AppRule> ruleList = new ArrayList<>();
    private RuleActionListener listener;
    private DatabaseReference userRef;

    public AppRuleAdapter(Context context, List<AppRule> ruleList, RuleActionListener listener) {
        this.context = context;
        this.ruleList = ruleList != null ? ruleList : new ArrayList<>();
        this.listener = listener;
    }

    public AppRuleAdapter(Context context, List<AppRule> ruleList) {
        this.context = context;
        this.ruleList = ruleList;
    }
    public void setData(List<AppRule> newList) {
        ruleList.clear();
        ruleList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_rule, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        AppRule rule = ruleList.get(position);
        holder.tvTitle.setText(rule.getTitle());
        holder.tvDescription.setText("Miêu tả: " + rule.getDescription());

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) return;
                    String role = snapshot.child("role").getValue(String.class);
                    if(role.equals("admin_super")){
                        holder.btnDelete.setVisibility(View.VISIBLE);
                        holder.btnEdit.setVisibility(View.VISIBLE);
                    } else {
                        holder.btnDelete.setVisibility(View.GONE);
                        holder.btnEdit.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
        }

        if(rule.getActionType().equals("comment_review")){
            holder.tvActionType.setText("Loại: Bình luận/Đánh giá");
        }
        else{
            holder.tvActionType.setText("Loại: Truyện");
        }
        holder.tvDuration.setText("Thời gian phạt: " + (rule.getDurationMillis() / (30L*24*60*60*1000)) + " tháng");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(rule));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(rule));
    }

    @Override
    public int getItemCount() {
        return ruleList.size();
    }

    static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvActionType, tvDuration;
        Button btnEdit, btnDelete;

        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRuleTitle);
            tvDescription = itemView.findViewById(R.id.tvRuleDescription);
            tvActionType = itemView.findViewById(R.id.tvRuleActionType);
            tvDuration = itemView.findViewById(R.id.tvRuleDuration);
            btnEdit = itemView.findViewById(R.id.btnEditRule);
            btnDelete = itemView.findViewById(R.id.btnDeleteRule);
        }
    }
}
