package com.example.literatureuniverse.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.literatureuniverse.R;
import com.example.literatureuniverse.model.AppRule;
import com.example.literatureuniverse.model.Story;
import com.example.literatureuniverse.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {
    Context context;
    List<User> userList;

    public AdminAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setData(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminAdapter.AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAdapter.AdminViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvAdminName.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());
        Glide.with(context)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .circleCrop()
                .into(holder.imgAvatarAdmin);
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa Admin này không?\n\nTên: "
                                + user.getUsername() + "\nEmail: " + user.getEmail())
                        .setCancelable(true)

                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Khi nhấn nút Xóa -> thực hiện xóa admin
                            FirebaseDatabase.getInstance().getReference("users").child(user.getUserId()).removeValue()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(context, "Đã xóa admin!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })

                        .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    public class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatarAdmin;
        TextView tvAdminName, tvEmail;
        Button btnDelete;
        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarAdmin = itemView.findViewById(R.id.imgAvatarAdmin);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
